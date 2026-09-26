/* 창고별 재고는 서버에서 전달한 #stock-data 값을 표시한다. */
(function($) {
	'use strict';
	var photoObjectUrl = null;
	var defaults = {
		activeFlag : 'Y',
		warehouseType : '창고',
		itemType : '상품',
		inboundPrice : '0',
		outboundPrice : '0',
		removeImage : 'N'
	};

	function notify(message) {
		window.Hexa.notice(message);
	}

	function field(form, name) {
		return $(form).find('[name="' + name + '"]')[0];
	}

	function setValue(form, name, value) {
		$(field(form, name)).val(value == null ? '' : String(value));
	}

	function value(form, name) {
		var result = $(field(form, name)).val();
		return result == null ? '' : result;
	}

	function releasePhoto() {
		if (photoObjectUrl) {
			URL.revokeObjectURL(photoObjectUrl);
			photoObjectUrl = null;
		}
	}

	function showPhoto(url) {
		var $image = $('#item-photo-image');
		$image.prop('hidden', !url);
		if (url)
			$image.attr('src', url);
		else
			$image.removeAttr('src');
	}

	function applyRecord(kind, record) {
		var form = $('#' + kind + '-form')[0];
		$(form.elements).each(
				function() {
					var $input = $(this);
					if (!this.name || this.name.indexOf('return.') === 0 || this.type === 'submit'
							|| this.type === 'reset' || this.type === 'button')
						return;
					var initial = Object.prototype.hasOwnProperty.call(record,
							this.name) ? record[this.name]
							: (defaults[this.name] || '');
					if (this.type === 'radio' || this.type === 'checkbox')
						$input.prop('checked', $input.val() === initial);
					else if (this.type === 'file')
						$input.val('');
					else
						$input.val(initial == null ? '' : String(initial));
				});
		var isEdit = value(form, kind + 'Id') !== '';
		$(field(form, kind + 'Code')).prop('readOnly', isEdit).attr(
				'placeholder', isEdit ? '' : '코드 입력');
		if (kind === 'item') {
			releasePhoto();
			showPhoto(record.imageUrl || '');
		}
		var labels = {
			partner : '거래처',
			warehouse : '창고',
			item : '품목'
		};
		$('[data-master-title="' + kind + '"]').text(
				labels[kind] + (isEdit ? ' 수정' : ' 등록'));
	}

	function openForm(kind, record) {
		var form = $('#' + kind + '-form')[0];
		var initial = Object.assign({}, record || {});
		var codeName = kind + 'Code';
		if (!initial[kind + 'Id']
				&& !Object.prototype.hasOwnProperty.call(initial, codeName)) {
			var codeInput = field(form, codeName);
			initial[codeName] = codeInput ? $(codeInput).prop('defaultValue')
					: '';
		}
		form._masterInitial = initial;
		applyRecord(kind, form._masterInitial);
		window.Hexa.openModal(kind + '-form-modal');
	}

	function stockCurrent() {
		var form = $('#stock-form')[0];
		if (!form)
			return;
		var id = value(form, 'itemId');
		var warehouse = value(form, 'warehouseId');
		var $matches = $('#stock-data [data-stock-item-id]')
				.filter(
						function() {
							return $(this).attr('data-stock-item-id') === id
									&& $(this).attr('data-stock-warehouse-id') === warehouse;
						});
		var current = $matches.length ? $matches.first().attr(
				'data-stock-quantity') : '';
		$('#stock-current').text(
				current !== '' ? Number(current).toLocaleString('ko-KR', {
					maximumFractionDigits : 3
				}) : '조회값 없음');
		setValue(form, 'quantity', current);
	}

	function openStock(item) {
		var form = $('#stock-form')[0];
		form.reset();
		setValue(form, 'itemId', item && item.itemId);
		setValue(form, 'itemName', item && item.itemName);
		stockCurrent();
		window.Hexa.openModal('stock-modal');
	}

	var actions = '[data-master-inactive-toggle],[data-master-new],[data-master-edit],'
			+ '[data-master-active],[data-master-pick],[data-stock-open],[data-stock-pick-item],'
			+ '[data-item-remove-image],[data-item-image]';
	$(document)
			.on(
					'click.hexaMaster',
					actions,
					function(event) {
						var $target = $(this);
						event.preventDefault();
						if ($target.is('[data-master-inactive-toggle]')) {
							var searchForm = this.form;
							var includeInactive = value(searchForm,
									'includeInactive') !== 'Y';
							setValue(searchForm, 'includeInactive',
									includeInactive ? 'Y' : 'N');
							setValue(searchForm, 'page', '1');
							$target.attr('aria-pressed',
									String(includeInactive)).toggleClass(
									'btn-primary', includeInactive);
							searchForm.requestSubmit();
						} else if ($target.is('[data-master-new]')) {
							openForm($target.attr('data-master-new'), {});
						} else if ($target.is('[data-master-edit]')) {
							var $record = $target.closest('[data-record-type]');
							openForm($target.attr('data-master-edit'), Object
									.assign({}, $record.prop('dataset')));
						} else if ($target.is('[data-master-active]')) {
							var selectedForm = document.getElementById($target
									.attr('data-selection-form'));
							if (!$(selectedForm).find(
									'input[name="ids"]:checked').length)
								return notify('처리할 행을 선택해 주세요.');
							setValue(selectedForm, 'activeFlag', $target
									.attr('data-master-active'));
							selectedForm.requestSubmit();
						} else if ($target.is('[data-master-pick]')) {
							var editForm = $target.closest('form')[0];
							window.Hexa.pick($target.attr('data-master-pick'),
									function(selected) {
										setValue(editForm, 'assigneeId',
												selected.assigneeId);
										setValue(editForm, 'assigneeName',
												selected.assigneeName);
									});
						} else if ($target.is('[data-stock-open]')) {
							var item;
							if ($target.attr('data-stock-open') === 'current') {
								var itemForm = $('#item-form')[0];
								if (!value(itemForm, 'itemId'))
									return notify('품목을 저장한 후 재고수량을 입력할 수 있습니다. 아직 저장·재고 처리 기능은 구현되지 않았습니다.');
								item = {
									itemId : value(itemForm, 'itemId'),
									itemName : value(itemForm, 'itemName')
								};
							} else {
								var $selected = $('#item-selection input[name="ids"]:checked');
								if ($selected.length > 1)
									return notify('재고를 조정할 품목을 한 개만 선택해 주세요.');
								if ($selected.length === 1) {
									var $itemRow = $selected.first().closest(
											'[data-record-type]');
									item = {
										itemId : $itemRow.attr('data-item-id'),
										itemName : $itemRow
												.attr('data-item-name')
									};
								}
							}
							openStock(item);
						} else if ($target.is('[data-stock-pick-item]')) {
							window.Hexa.pick('item', function(selectedItem) {
								var stockForm = $('#stock-form')[0];
								setValue(stockForm, 'itemId',
										selectedItem.itemId);
								setValue(stockForm, 'itemName',
										selectedItem.itemName);
								stockCurrent();
							});
						} else if ($target.is('[data-item-remove-image]')) {
							var photoForm = $('#item-form')[0];
							setValue(photoForm, 'imageFile', '');
							setValue(photoForm, 'removeImage', 'Y');
							releasePhoto();
							showPhoto('');
						} else if ($target.is('[data-item-image]')) {
							$('#item-image-full').attr(
									{
										src : $target.attr('data-item-image'),
										alt : $target
												.attr('data-item-image-name')
												+ ' 대표 사진'
									});
							window.Hexa.openModal('item-image-modal');
						}
					});

	$(document).on('input.hexaMaster', '#partner-form [name="assigneeName"]',
			function() {
				setValue(this.form, 'assigneeId', '');
			});

	$(document).on('change.hexaMaster', '#stock-form [name="warehouseId"]',
			stockCurrent);

	$(document).on('change.hexaMaster', '[data-item-file]', function() {
		// 사용이 끝난 사진 미리보기 주소를 해제한다.
		var file = this.files && this.files[0];
		releasePhoto();
		if (!file) {
			showPhoto('');
			return;
		}
		if (!file.type || file.type.indexOf('image/') !== 0) {
			$(this).val('');
			showPhoto('');
			return notify('이미지 파일을 선택해 주세요.');
		}
		photoObjectUrl = URL.createObjectURL(file);
		setValue(this.form, 'removeImage', 'N');
		showPhoto(photoObjectUrl);
	});

	[ 'partner', 'warehouse', 'item' ].forEach(function(kind) {
		$('#' + kind + '-form').on('reset.hexaMaster', function() {
			var form = this;
			// '다시 작성'을 누르면 팝업을 열었을 때의 값으로 되돌린다.
			window.setTimeout(function() {
				applyRecord(kind, form._masterInitial || {});
			}, 0);
		});
	});

	$('#stock-form').on('submit.hexaMaster', function(event) {
		if (!value(this, 'itemId')) {
			event.preventDefault();
			event.stopPropagation();
			notify('재고를 입력할 품목을 선택해 주세요.');
		}
	});

	$(window).on('pagehide.hexaMaster', releasePhoto);
}(jQuery));
