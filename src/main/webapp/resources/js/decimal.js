/* UI amount calculation only. Keep raw decimal strings; do not multiply Number values.
 * DDL: supply = ROUND(quantity * unitPrice, 0), VAT = ROUND(supply * 0.1, 0).
 * Browser prerequisite: BigInt (the existing UI already targets modern browsers).
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
	// Bound malformed/extreme user input, not business values. This exceeds the
	// NUMBER(18,*) input sizes and the largest product/total used by these
	// views.
	var MAX_DIGITS = 512, MAX_EXPONENT = 256;

	// Avoid exponentiation syntax for older STS/Eclipse JavaScript validators.
	// Keep every multiplication in BigInt so decimal precision is unchanged.
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

	// HALF_UP means a halfway value rounds away from zero, including negatives.
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

	// Formatting must not convert back to Number (amounts may exceed 2^53 - 1).
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
		format : format
	};
}));
