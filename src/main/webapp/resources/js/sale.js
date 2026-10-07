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
		// 쉼표는 화면 표시에만 붙인다.
		$supplyCell.text(window.HexaDecimal.format(amounts.supplyAmount));
		$vatCell.text(window.HexaDecimal.format(amounts.vatAmount));
		$row.find('input[type="checkbox"]').prop('checked', true);
	});

	// 단가를 바꿀 방식에 따라 입력칸 안내를 바꾼다.
	$form.on('change.hexaSale', '[data-bulk-apply-mode]', function() {
		var percent = $(this).val() === 'percent';
		$form.find('[data-bulk-apply-value]').attr('placeholder',
				percent ? '예: 10 (인하는 -10)' : '변경할 단가').val('');
		$form.find('[data-bulk-apply-unit]').text(percent ? '%' : '원');
	});

	// 적용값 입력칸에서 Enter를 누르면 저장 대신 [선택 행에 적용]을 실행한다.
	$form.on('keydown.hexaSale', '[data-bulk-apply-value]', function(e) {
		if (e.key === 'Enter') {
			e.preventDefault();
			$form.find('[data-bulk-apply]').trigger('click');
		}
	});

	// 선택한 행의 단가칸에 값을 채운다. 저장은 [선택한 단가 저장]으로 따로 한다.
	$form.on('click.hexaSale', '[data-bulk-apply]', function() {
		var mode = $form.find('[data-bulk-apply-mode]').val();
		var value = $.trim($form.find('[data-bulk-apply-value]').val());
		var $checked = $form.find('[data-bulk-price-row] input[type="checkbox"]:checked');
		if (!$checked.length) {
			window.Hexa.notice('단가를 적용할 행을 선택해 주세요.');
			return;
		}
		if (!value || !isFinite(value)) {
			window.Hexa.notice(mode === 'percent' ? '조정할 비율(%)을 입력해 주세요.' : '적용할 단가를 입력해 주세요.');
			return;
		}
		if (mode === 'percent' ? Number(value) < -100 : Number(value) < 0) {
			window.Hexa.notice('단가가 0보다 작아지도록 적용할 수 없습니다.');
			return;
		}
		var skipped = 0;
		$checked.each(function() {
			var $price = $(this).closest('[data-bulk-price-row]').find('[data-bulk-unit-price]');
			// 비율 조정은 각 행의 현재 단가를 기준으로 계산한다. 단가 지정은 0% 조정으로 소수 둘째 자리까지 맞춘다.
			var price = mode === 'percent' ? window.HexaDecimal.adjustPercent($price.val(), value)
					: window.HexaDecimal.adjustPercent(value, '0');
			if (price == null) {
				skipped += 1;
				return;
			}
			// input 이벤트로 공급가액·부가세 표시도 함께 다시 계산한다.
			$price.val(price).trigger('input');
		});
		if (skipped)
			window.Hexa.notice('단가가 비어 있는 ' + skipped + '개 행은 비율을 적용하지 못했습니다.');
	});
}(jQuery));
