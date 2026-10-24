(function($) {
	'use strict';
	var $form = $('#bulk-price-form');
	if (!$form.length)
		return;

	$form.on('input.hexaSale', '[data-bulk-unit-price]', function() {
		var $row = $(this).closest('[data-bulk-price-row]');
		var quantity = $row.attr('data-quantity');
		var price = $(this).val();
		var amounts = window.HexaDecimal.lineAmounts(quantity, price);
		var $supplyCell = $row.find('[data-bulk-supply]');
		var $vatCell = $row.find('[data-bulk-vat]');
		if (!amounts) {
			$supplyCell.text('-');
			$vatCell.text('-');
			return;
		}
		// 桁区切りは画面表示にだけ付ける。
		$supplyCell.text(window.HexaDecimal.format(amounts.supplyAmount));
		$vatCell.text(window.HexaDecimal.format(amounts.vatAmount));
		$row.find('input[type="checkbox"]').prop('checked', true);
	});

	// 単価の適用方法に応じて入力欄の案内を変える。
	$form.on('change.hexaSale', '[data-bulk-apply-mode]', function() {
		var percent = $(this).val() === 'percent';
		$form.find('[data-bulk-apply-value]').attr('placeholder',
				percent ? '例: 10（値下げは-10）' : '変更後の単価').val('');
		$form.find('[data-bulk-apply-unit]').text(percent ? '%' : '円');
	});

	// 適用値の入力欄でEnterを押すと、保存せずに［選択行に適用］を実行する。
	$form.on('keydown.hexaSale', '[data-bulk-apply-value]', function(e) {
		if (e.key === 'Enter') {
			e.preventDefault();
			$form.find('[data-bulk-apply]').trigger('click');
		}
	});

	// 選択行の単価欄に値を設定する。保存は［選択した単価を保存］で別途行う。
	$form.on('click.hexaSale', '[data-bulk-apply]', function() {
		var mode = $form.find('[data-bulk-apply-mode]').val();
		var value = $.trim($form.find('[data-bulk-apply-value]').val());
		var $checked = $form.find('[data-bulk-price-row] input[type="checkbox"]:checked');
		if (!$checked.length) {
			window.Hexa.notice('単価を適用する行を選択してください。');
			return;
		}
		if (!value || !isFinite(value)) {
			window.Hexa.notice(mode === 'percent' ? '調整する比率（%）を入力してください。' : '適用する単価を入力してください。');
			return;
		}
		if (mode === 'percent' ? Number(value) < -100 : Number(value) < 0) {
			window.Hexa.notice('単価が0未満になる値は適用できません。');
			return;
		}
		var skipped = 0;
		$checked.each(function() {
			var $price = $(this).closest('[data-bulk-price-row]').find('[data-bulk-unit-price]');
			// 比率調整は各行の現在単価を基準に計算する。単価指定では0%調整として小数点以下2桁にそろえる。
			var price = mode === 'percent' ? window.HexaDecimal.adjustPercent($price.val(), value)
					: window.HexaDecimal.adjustPercent(value, '0');
			if (price == null) {
				skipped += 1;
				return;
			}
			// inputイベントで税抜金額・消費税の表示も再計算する。
			$price.val(price).trigger('input');
		});
		if (skipped)
			window.Hexa.notice('単価が未入力の' + skipped + '行には比率を適用できませんでした。');
	});
}(jQuery));
