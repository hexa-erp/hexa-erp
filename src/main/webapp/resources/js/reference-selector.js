/* 선택한 항목의 ID·코드·이름을 함께 반영한다. */
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

	function refresh(box) {
		var fields = controls(box);
		var pending = fields.$code.val().trim() !== ''
				&& fields.$id.val().trim() === '';
		fields.$code[0]
				.setCustomValidity(pending ? '입력한 코드로 검색한 뒤 목록에서 항목을 선택해 주세요.'
						: '');
		if (pending)
			fields.$code.attr('aria-invalid', 'true');
		else
			fields.$code.removeAttr('aria-invalid');
	}

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
										.notice('선택 데이터에 ID·코드·이름이 필요합니다. 조회 API 응답을 확인해 주세요.');
								return;
							}
							apply(box, row);
							// 거래처를 직접 선택하면 연결된 담당자도 반영한다.
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
		// 코드를 다시 입력하면 이전 선택을 해제한다.
		fields.$id.val('');
		fields.$name.val('');
		refresh(box);
		[ fields.$id[0], fields.$name[0] ].forEach(function(input) {
			input.dispatchEvent(new Event('change', {
				bubbles : true
			}));
		});
	});

	$boxes.on('keydown.hexaReference', '[data-reference-code]', function(e) {
		var event = e.originalEvent || e;
		// 한글 입력을 확정하는 Enter는 검색에서 제외한다.
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
						// 이미 입력하거나 선택한 항목은 그대로 둔다.
						if (fields.$id.val() || fields.$code.val()
								|| fields.$name.val())
							return;
						apply(this, source);
					});
		}
	};
}(jQuery));
