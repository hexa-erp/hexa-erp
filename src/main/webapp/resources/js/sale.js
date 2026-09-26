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
}(jQuery));
