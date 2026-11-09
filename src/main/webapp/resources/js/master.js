/* 倉庫別在庫はサーバーから渡された#stock-dataの値を表示する。 */
(function($) {
	'use strict';
	var photoObjectUrl = null;
	// 新規登録時は倉庫区分「倉庫」、品目区分「商品」、入庫単価・出庫単価0を初期値とする。
	var defaults = {
		activeFlag : 'Y',
		warehouseType : '倉庫',
		itemType : '商品',
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

	function sendMaster(form, url, method, data, multipart) {
		if (form._masterPending)
			return;
		form._masterPending = true;
		var $buttons = $(form).find('button:enabled, input[type="submit"]:enabled');
		$buttons.prop('disabled', true);
		Promise.resolve().then(function() {
			return window.Hexa.request(url, method, data, multipart);
		}).then(function() {
			// 現在の検索条件とページを維持して一覧を再照会する。
			window.location.reload();
		}, function(error) {
			// 保存に失敗した場合はエラーを表示し、ポップアップの入力内容を保持する。
			notify(error.message || 'リクエストを処理できませんでした。');
		}).then(function() {
			form._masterPending = false;
			$buttons.prop('disabled', false);
		});
	}

	function masterPayload(form) {
		var data = {};
		$(form).serializeArray().forEach(function(entry) {
			if (entry.name.indexOf('return.') !== 0)
				data[entry.name] = entry.value === '' ? null : entry.value;
		});
		return data;
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
					if (!this.name || this.name.indexOf('return.') === 0
							|| this.type === 'submit' || this.type === 'reset'
							|| this.type === 'button')
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
		// 既存データの変更時はコードを変更できない。
		var isEdit = value(form, kind + 'Id') !== '';
		$(field(form, kind + 'Code')).prop('readOnly', isEdit).attr(
				'placeholder', isEdit ? '' : 'コード入力');
		if (kind === 'item') {
			releasePhoto();
			showPhoto(record.imageUrl || '');
		}
		var labels = {
			partner : '取引先',
			warehouse : '倉庫',
			item : '品目'
		};
		$('[data-master-title="' + kind + '"]').text(
				labels[kind] + (isEdit ? ' 編集' : ' 登録'));
	}

	// 新規では空欄と初期値、変更では該当行の値を設定して登録ポップアップを開く。
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

	// 品目・倉庫を選択すると、その組み合わせの現在庫を表示する。
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
				'data-stock-quantity') : (id && warehouse ? '0' : '');

		$('#stock-current').text(
				current !== '' ? Number(current).toLocaleString('ja-JP', {
					maximumFractionDigits : 3
				}) : '—');
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
						// 使用停止中のデータを含むかを切り替え、1ページ目から再照会する。
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
							// 未選択の場合は送信せず、チェックした行の使用フラグをNまたはYに変更する。
							var selectedForm = document.getElementById($target
									.attr('data-selection-form'));
							if (!$(selectedForm).find(
									'input[name="ids"]:checked').length)
								return notify('処理する行を選択してください。');
							setValue(selectedForm, 'activeFlag', $target
									.attr('data-master-active'));
							selectedForm.requestSubmit();
						} else if ($target.is('[data-master-pick]')) {
							// 担当者選択画面を開き、選択した担当者のIDと担当者名を入力欄に設定する。
							var editForm = $target.closest('form')[0];
							window.Hexa.pick($target.attr('data-master-pick'),
									function(selected) {
										setValue(editForm, 'assigneeId',
												selected.assigneeId);
										setValue(editForm, 'assigneeName',
												selected.assigneeName);
									});
						} else if ($target.is('[data-stock-open]')) {
							// 未選択なら空欄、1件選択なら該当品目で開く。2件以上または未保存の品目では開かない。
							var item;
							if ($target.attr('data-stock-open') === 'current') {
								var itemForm = $('#item-form')[0];
								if (!value(itemForm, 'itemId'))
									return notify('品目を保存すると在庫数量を入力できます。');
								item = {
									itemId : value(itemForm, 'itemId'),
									itemName : value(itemForm, 'itemName')
								};
							} else {
								var $selected = $('#item-selection input[name="ids"]:checked');
								if ($selected.length > 1)
									return notify('在庫を調整する品目を1件だけ選択してください。');
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
							// サムネイルを押下すると、元の画像を拡大表示する。
							$('#item-image-full').attr(
									{
										src : $target.attr('data-item-image'),
										alt : $target
												.attr('data-item-image-name')
												+ ' 代表画像'
									});
							window.Hexa.openModal('item-image-modal');
						}
					});

	// 担当者名を直接変更した場合は、担当者IDをクリアする。
	$(document).on('input.hexaMaster', '#partner-form [name="assigneeName"]',
			function() {
				setValue(this.form, 'assigneeId', '');
			});

	$(document).on('change.hexaMaster', '#stock-form [name="warehouseId"]',
			stockCurrent);

	$(document).on('change.hexaMaster', '[data-item-file]', function() {
		// 使用済みの画像プレビューURLを解放する。
		var file = this.files && this.files[0];
		releasePhoto();
		if (!file) {
			showPhoto('');
			return;
		}
		// 写真ファイルはJPG・JPEG・PNG・GIF形式、5MB以下に制限する。
		if (!/\.(jpe?g|png|gif)$/i.test(file.name) || file.size > 5242880) {

			$(this).val('');
			showPhoto('');

			return notify('5MB以下のJPG・PNG・GIFファイルを選択してください。');
		}
		photoObjectUrl = URL.createObjectURL(file);
		setValue(this.form, 'removeImage', 'N');
		showPhoto(photoObjectUrl);
	});

	[ 'partner', 'warehouse', 'item' ].forEach(function(kind) {
		$('#' + kind + '-form').on('submit.hexaMaster', function(event) {
			event.preventDefault();
			if (this._masterPending)
				return;
			var idName = kind + 'Id';
			var id = value(this, idName);
			var url = $(this).attr('data-master-api');
			var multipart = kind === 'item';
			var data;
			if (multipart) {
				data = new FormData(this);
				$(this).find('[name^="return."]').each(function() {
					data.delete(this.name);
				});
				data.delete(idName);
				data.delete('imagePath');
				data.delete('imageUrl');
			} else {
				data = masterPayload(this);
			}
			if (id)
				url += '/' + encodeURIComponent(id);
			// IDがない場合は新規登録し、IDがある場合は該当データを更新する。
			sendMaster(this, url, id ? 'PUT' : 'POST', data, multipart);
		});

		$('#' + kind + '-selection').on('submit.hexaMaster', function(event) {
			event.preventDefault();
			var ids = $(this).find('input[name="ids"]:checked').map(function() {
				return this.value;
			}).get();
			if (!ids.length)
				return notify('処理する行を選択してください。');
			sendMaster(this, $(this).attr('data-master-api'), 'PATCH', {
				ids : ids,
				activeFlag : value(this, 'activeFlag')
			}, false);
		});

		$('#' + kind + '-form').on('reset.hexaMaster', function() {
			var form = this;
			// リセットを押すとダイアログを開いた時点の値に戻す。
			window.setTimeout(function() {
				applyRecord(kind, form._masterInitial || {});
			}, 0);
		});
	});

	$('#stock-form').on('submit.hexaMaster', function(event) {
		if (!value(this, 'itemId')) {
			event.preventDefault();
			event.stopPropagation();
			notify('在庫を入力する品目を選択してください。');
		}
	});

	$(window).on('pagehide.hexaMaster', releasePhoto);
}(jQuery));
