/* 画面上の金額計算用。小数計算の誤差を抑えるため文字列とBigIntを使う。
 * 税抜金額は数量×単価、消費税は税抜金額×10%を円単位で四捨五入する（HALF_UP）。
 */
(function(root, factory) {
	'use strict';
	if (typeof module === 'object' && module.exports)
		module.exports = factory();
	else
		root.HexaDecimal = factory();
}(typeof window !== 'undefined' ? window : this, function() {
	'use strict';
	var ZERO = BigInt(0), ONE = BigInt(1), TWO = BigInt(2), TEN = BigInt(10);
	var MAX_DIGITS = 512, MAX_EXPONENT = 256;

	function power10(scale) {
		var result = ONE;
		for (var i = 0; i < scale; i += 1) {
			result *= TEN;
		}
		return result;
	}

	function parse(value) {
		if (value == null)
			return null;
		var text = String(value).trim();
		if (!text || text.length > MAX_DIGITS)
			return null;
		var match = /^([+-]?)(?:(\d+)(?:\.(\d*))?|\.(\d+))(?:[eE]([+-]?\d+))?$/
				.exec(text);
		if (!match)
			return null;
		var fraction = match[3] || match[4] || '';
		var exponent = Number(match[5] || 0);
		if (!Number.isFinite(exponent) || Math.abs(exponent) > MAX_EXPONENT)
			return null;
		var coefficient = BigInt((match[1] === '-' ? '-' : '')
				+ (match[2] || '0') + fraction);
		var scale = fraction.length - exponent;
		if (scale < 0) {
			coefficient *= power10(-scale);
			scale = 0;
		}
		return {
			coefficient : coefficient,
			scale : scale
		};
	}

	function stringify(value) {
		var n = value.coefficient;
		if (n === ZERO)
			return '0';
		var sign = n < ZERO ? '-' : '';
		var digits = (n < ZERO ? -n : n).toString();
		if (!value.scale)
			return sign + digits;
		digits = digits.padStart(value.scale + 1, '0');
		var whole = digits.slice(0, -value.scale);
		var fraction = digits.slice(-value.scale).replace(/0+$/, '');
		return sign + whole + (fraction ? '.' + fraction : '');
	}

	// HALF_UPの丸めでは1.5は2、-1.5は-2になる。
	function roundInteger(value) {
		if (!value.scale)
			return value.coefficient;
		var divisor = power10(value.scale);
		var magnitude = value.coefficient < ZERO ? -value.coefficient
				: value.coefficient;
		var whole = magnitude / divisor;
		if ((magnitude % divisor) * TWO >= divisor)
			whole += ONE;
		return value.coefficient < ZERO ? -whole : whole;
	}

	// HALF_UPで小数点以下scale桁まで丸める。
	function roundScale(value, scale) {
		// 桁数が足りなければ0で補い、超える場合は超過桁を整数演算で丸める。
		if (value.scale <= scale)
			return {
				coefficient : value.coefficient * power10(scale - value.scale),
				scale : scale
			};
		return {
			coefficient : roundInteger({
				coefficient : value.coefficient,
				scale : value.scale - scale
			}),
			scale : scale
		};
	}

	// 単価をpercent%調整し（負数なら値下げ）、小数点以下2桁に丸める。
	function adjustPercent(unitPrice, percent) {
		var p = parse(unitPrice), r = parse(percent);
		if (!p || !r)
			return null;
		// 単価 × (100 + percent) ÷ 100
		// percentの小数桁に合わせて100も同じ倍率で拡大してから加える。（2.5% → 1000 + 25）
		// ÷100はscaleを2増やすことで表す。
		var factor = BigInt(100) * power10(r.scale) + r.coefficient;
		return stringify(roundScale({
			coefficient : p.coefficient * factor,
			scale : p.scale + r.scale + 2
		}, 2));
	}

	function add(left, right) {
		var a = parse(left), b = parse(right);
		if (!a || !b)
			return null;
		var scale = Math.max(a.scale, b.scale);
		return stringify({
			coefficient : a.coefficient * power10(scale - a.scale)
					+ b.coefficient * power10(scale - b.scale),
			scale : scale
		});
	}

	function lineAmounts(quantity, unitPrice) {
		var q = parse(quantity), p = parse(unitPrice);
		if (!q || !p)
			return null;
		var supply = roundInteger({
			coefficient : q.coefficient * p.coefficient,
			scale : q.scale + p.scale
		});
		var vat = roundInteger({
			coefficient : supply,
			scale : 1
		});
		return {
			supplyAmount : supply.toString(),
			vatAmount : vat.toString(),
			totalAmount : (supply + vat).toString()
		};
	}

	// 大きな金額も正確に表示するため、文字列に桁区切りを付ける。
	function format(value) {
		var parsed = parse(value);
		if (!parsed)
			return '-';
		var parts = stringify(parsed).split('.');
		return parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
				+ (parts.length > 1 ? '.' + parts[1] : '');
	}

	return {
		lineAmounts : lineAmounts,
		add : add,
		adjustPercent : adjustPercent,
		format : format
	};
}));
