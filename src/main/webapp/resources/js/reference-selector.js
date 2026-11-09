/* 選択項目のID・コード・名称を一緒に反映する。 */
(function($) {
	'use strict';
	var $boxes = $('[data-code-selector]');
	if (!$boxes.length)
		return;

	function text(value) {
		return value == null ? '' : String(value);
	}

	function controls(box) {
		var $box = $(box);
		var type = $box.attr('data-code-selector');
		return {
			type : type,
			$id : $box.find('input[name="' + type + 'Id"]'),
			$code : $box.find('[data-reference-code]'),
			$name : $box.find('input[name="' + type + 'Name"]')
		};
	}

	// コードを入力しただけでは選択確定としない。IDが未確定なら案内を表示し、フォームの保存を禁止する。
	function refresh(box) {
		var fields = controls(box);
		var pending = fields.$code.val().trim() !== ''
				&& fields.$id.val().trim() === '';
		fields.$code[0]
				.setCustomValidity(pending ? '入力したコードで検索し、一覧から項目を選択してください。'
						: '');
		if (pending)
			fields.$code.attr('aria-invalid', 'true');
		else
			fields.$code.removeAttr('aria-invalid');
	}

	// 選択したID・コード・名称を入力欄へ設定し、IDを保存用の値として保持する。
	function apply(box, row) {
		var fields = controls(box);
		fields.$id.val(text(row[fields.type + 'Id']));
		fields.$code.val(text(row[fields.type + 'Code']));
		fields.$name.val(text(row[fields.type + 'Name']));
		refresh(box);
		[ fields.$id[0], fields.$code[0], fields.$name[0] ].forEach(function(
				input) {
			input.dispatchEvent(new Event('change', {
				bubbles : true
			}));
		});
	}

	function openPicker(box) {
		var fields = controls(box);
		var keyword = fields.$id.val() ? '' : fields.$code.val().trim();
		window.Hexa
				.pick(
						fields.type,
						function(row) {
							if (!text(row[fields.type + 'Id'])
									|| !text(row[fields.type + 'Code'])
									|| !text(row[fields.type + 'Name'])) {
								window.Hexa
										.notice('選択データにはID・コード・名称が必要です。照会APIの応答を確認してください。');
								return;
							}
							apply(box, row);
							// 取引先を選択すると関連する担当者を自動反映し、関連担当者がいない場合は既存の担当者を空にする。
							if (fields.type === 'partner') {
								var $assignee = $(box).closest('form[data-document-form]')
										.find('[data-code-selector="assignee"]');
								var hasAssignee = text(row.assigneeId) && text(row.assigneeCode)
										&& text(row.assigneeName);
								$assignee.each(function() {
									apply(this, hasAssignee ? row : {});
								});
							}
						}, keyword);
	}

	$boxes.on('click.hexaReference', '[data-pick]', function() {
		openPicker($(this).closest('[data-code-selector]')[0]);
	});

	$boxes.on('input.hexaReference', '[data-reference-code]', function() {
		var box = $(this).closest('[data-code-selector]')[0];
		var fields = controls(box);
		// コードを再入力すると前回の選択を解除する。
		fields.$id.val('');
		fields.$name.val('');
		refresh(box);
		[ fields.$id[0], fields.$name[0] ].forEach(function(input) {
			input.dispatchEvent(new Event('change', {
				bubbles : true
			}));
		});
	});

	// 取引先・担当者・倉庫のコード入力後、Enterキーで入力コードによる選択一覧を開く。
	$boxes.on('keydown.hexaReference', '[data-reference-code]', function(e) {
		var event = e.originalEvent || e;
		// IME入力の確定に使ったEnterでは検索しない。
		if (e.key !== 'Enter' || event.isComposing || event.keyCode === 229)
			return;
		e.preventDefault();
		openPicker($(this).closest('[data-code-selector]')[0]);
	});

	$boxes.each(function() {
		refresh(this);
	});

	window.HexaReference = {
		refreshForm : function(form) {
			$(form).find('[data-code-selector]').each(function() {
				refresh(this);
			});
		},
		applySource : function(form, source) {
			$(form).find('[data-code-selector]').each(
					function() {
						var fields = controls(this);
						// 入力済み・選択済みの項目は維持する。
						if (fields.$id.val() || fields.$code.val()
								|| fields.$name.val())
							return;
						apply(this, source);
					});
		}
	};
}(jQuery));
