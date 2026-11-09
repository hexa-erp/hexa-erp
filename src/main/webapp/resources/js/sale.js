(function($) {
	'use strict';
	var $form = $('#bulk-price-form');
	if (!$form.length)
		return;

	// 個別単価を直接編集すると供給価額・付加価値税を再表示し、計算可能な行を自動選択する。
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

	// 単価指定・比率調整の方式を変更すると入力値をクリアし、案内文と単位（円／%）を切り替える。
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
		// 指定単価0未満・調整比率-100%未満は適用しない。単価0と比率0%・-100%は許可する。
		if (mode === 'percent' ? Number(value) < -100 : Number(value) < 0) {
			window.Hexa.notice('単価が0未満になる値は適用できません。');
			return;
		}
		var skipped = 0;
		$checked.each(function() {
			var $price = $(this).closest('[data-bulk-price-row]').find('[data-bulk-unit-price]');
			// 比率調整は現在の画面単価×（100＋入力比率）÷100を小数2桁でHALF_UPにより丸める。
			// 再適用も変更後の画面単価が基準。単価指定では全選択行に同じ単価を設定する。
			var price = mode === 'percent' ? window.HexaDecimal.adjustPercent($price.val(), value)
					: window.HexaDecimal.adjustPercent(value, '0');
			// 比率計算できない行はスキップし、最後に対象件数を案内する。
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
