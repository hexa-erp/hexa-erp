/* ID·CODE·이름은 서로 다른 값이므로 선택과 초기화 시 함께 다룬다. */
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
		// 모든 값을 먼저 반영한다. ID와 CODE를 서로 대신 쓰거나 숫자로 변환하지 않는다.
		fields.$id.val(text(row[fields.type + 'Id']));
		fields.$code.val(text(row[fields.type + 'Code']));
		fields.$name.val(text(row[fields.type + 'Name']));
		refresh(box);
		// native·jQuery 리스너 모두에게 필드별 change를 한 번만 전달한다.
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
						}, keyword);
	}

	$boxes.on('click.hexaReference', '[data-pick]', function() {
		openPicker($(this).closest('[data-code-selector]')[0]);
	});

	$boxes.on('input.hexaReference', '[data-reference-code]', function() {
		var box = $(this).closest('[data-code-selector]')[0];
		var fields = controls(box);
		// 입력 코드와 이전 ID·이름이 섞이지 않도록 선택을 해제한다.
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
		// 한글 조합 확정 Enter는 검색/제출로 처리하지 않는다.
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
						// 확정된 선택이나 아직 검색 중인 코드가 있으면 그 세트 전체를 보존한다.
						if (fields.$id.val() || fields.$code.val()
								|| fields.$name.val())
							return;
						apply(this, source);
					});
		}
	};
}(jQuery));
