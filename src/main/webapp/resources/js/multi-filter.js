/* 選択値は［適用］を押したときに検索条件へ反映する。 */
(function($) {
	'use strict';
	var $dialog = $('#multi-filter-modal');
	if (!$dialog.length || !window.Hexa)
		return;
	var $boxes = $('[data-multi-filter]');
	if (!$boxes.length)
		return;
	var kinds = {
		warehouse : {
			label : '倉庫',
			columns : [ [ 'warehouseCode', '倉庫コード' ],
					[ 'warehouseName', '倉庫名' ], [ 'warehouseType', '区分' ] ]
		},
		partner : {
			label : '取引先',
			columns : [ [ 'partnerCode', '取引先コード' ], [ 'partnerName', '取引先名' ] ]
		},
		item : {
			label : '品目',
			columns : [ [ 'itemCode', '品目コード' ], [ 'itemName', '品目名' ],
					[ 'specification', '規格' ] ]
		},
		assignee : {
			label : '担当者',
			columns : [ [ 'assigneeCode', '担当者コード' ], [ 'assigneeName', '担当者名' ] ]
		}
	};
	var $head = $('#multi-filter-head');
	var $body = $('#multi-filter-body');
	var $searchForm = $('#multi-filter-search');
	var $errorBox = $('#multi-filter-error');
	var $pages = $('#multi-filter-pages');
	var active = null;
	var requestNumber = 0;
	var pendingRequest = null;
	var initialFilters = [];

	function text(value) {
		return value == null ? '' : String(value);
	}
	function copy(list) {
		return list.map(function(v) {
			return {
				id : v.id,
				code : v.code,
				name : v.name
			};
		});
	}
	function indexOf(list, id) {
		return list.findIndex(function(v) {
			return v.id === id;
		});
	}
	function entry(row, kind) {
		var id = text(row[kind + 'Id']);
		return {
			id : id,
			code : text(row[kind + 'Code']),
			name : text(row[kind + 'Name']) || text(row[kind + 'Code']) || id
		};
	}
	function read(box) {
		var list = [];
		$(box).find('[data-multi-id]').each(function() {
			var $chip = $(this);
			var id = $chip.attr('data-multi-id');
			if (id && indexOf(list, id) < 0) {
				list.push({
					id : id,
					code : $chip.attr('data-multi-code') || '',
					name : $chip.attr('data-multi-label') || id
				});
			}
		});
		return list;
	}
	function chip(value, fieldName) {
		var $chip = $(Hexa.node('span', null, 'multi-chip')).attr({
			role : 'listitem',
			'data-multi-id' : value.id,
			'data-multi-code' : value.code,
			'data-multi-label' : value.name,
			title : (value.code ? value.code + ' · ' : '') + value.name
		});
		$chip.append(Hexa.node('span', value.name, 'multi-chip-label'));
		$chip.append($(Hexa.node('button', '×')).prop('type', 'button').attr({
			'data-multi-remove' : '',
			'aria-label' : value.name + ' 選択解除'
		}));
		// 各IDを同じ名前のパラメーターとして送る。
		$chip.append($(Hexa.node('input')).prop('type', 'hidden').attr('name',
				fieldName).val(value.id));
		return $chip;
	}
	function paint(box, list, announce) {
		var $box = $(box);
		var $area = $box.find('[data-multi-chips]').empty();
		list.forEach(function(value) {
			$area.append(chip(value, $box.attr('data-multi-name')));
		});
		$box.find('[data-multi-clear]').prop('hidden', list.length === 0);
		if (announce) {
			$box.find('[data-multi-live]').text(
					kinds[$box.attr('data-multi-filter')].label + ' '
							+ list.length + '件選択');
		}
	}
	function syncDraft() {
		if (!active)
			return;
		// 検索やページ移動中も適用前の選択を維持する。
		var checked = 0;
		$body.find('[data-multi-row-id]')
				.each(
						function() {
							var $row = $(this);
							var selected = indexOf(active.draft, $row
									.attr('data-multi-row-id')) >= 0;
							$row.toggleClass('is-selected', selected);
							$row.find('input[type=checkbox]').prop('checked',
									selected);
							$row.find('.multi-result-toggle').attr(
									'aria-pressed', String(selected));
							if (selected)
								checked += 1;
						});
		var count = active.rows.length;
		$('#multi-filter-page-all').prop({
			checked : count > 0 && checked === count,
			indeterminate : checked > 0 && checked < count,
			disabled : count === 0
		});
	}
	function toggle(value, selected) {
		if (!active || !value.id)
			return;
		var index = indexOf(active.draft, value.id);
		if (selected && index < 0)
			active.draft.push(value);
		if (!selected && index >= 0)
			active.draft.splice(index, 1);
	}
	function tableMessage(message) {
		var $cell = $(Hexa.node('td', message, 'empty-state')).prop('colSpan',
				kinds[active.kind].columns.length + 1);
		$body.empty().append($(Hexa.node('tr')).append($cell));
	}
	function cancelRequest() {
		// 前の検索を中止し、最新の応答だけを使う。
		requestNumber += 1;
		var previous = pendingRequest;
		pendingRequest = null;
		if (previous)
			previous.abort();
	}
	function currentRequest(state, token) {
		return active === state && token === requestNumber
				&& $dialog.prop('open');
	}
	function showLoadError() {
		active.rows = [];
		tableMessage('一覧を取得できませんでした。');
		$errorBox.text('照会APIの接続を確認して再度検索してください。選択済みの項目は保持されます。').prop(
				'hidden', false);
		syncDraft();
	}
	function load(page) {
		if (!active)
			return;
		cancelRequest();
		var state = active;
		var token = requestNumber;
		state.rows = [];
		$errorBox.prop('hidden', true);
		$body.attr('aria-busy', 'true');
		$pages.empty();
		tableMessage('照会中…');
		syncDraft();
		var request = $.ajax({
			url : Hexa.ctx + '/lookup/options/' + state.kind + '?keyword='
					+ encodeURIComponent(state.keyword) + '&page=' + page,
			type : 'GET',
			dataType : 'json',
			jsonp : false,
			headers : {
				Accept : 'application/json'
			}
		});
		pendingRequest = request;
		request
				.done(
						function(data, textStatus, xhr) {
							if (!currentRequest(state, token))
								return;
							if (xhr.status < 200 || xhr.status >= 300 || !data
									|| !Array.isArray(data.rows)) {
								showLoadError();
								return;
							}
							try {
								$body.empty();
								state.rows = [];
								data.rows
										.forEach(function(row) {
											var value = entry(row, state.kind);
											if (!value.id
													|| indexOf(state.rows,
															value.id) >= 0)
												return;
											state.rows.push(value);
											var $row = $(Hexa.node('tr')).attr(
													'data-multi-row-id',
													value.id);
											var $check = $(Hexa.node('input'))
													.prop('type', 'checkbox')
													.attr('aria-label',
															value.name + ' 選択');
											$row.append($(Hexa.node('td'))
													.append($check));
											kinds[state.kind].columns
													.forEach(function(column, i) {
														var $cell = $(Hexa
																.node('td'));
														if (i < 2) {
															$cell
																	.append($(
																			Hexa
																					.node(
																							'button',
																							text(row[column[0]])
																									|| '—',
																							'multi-result-toggle'))
																			.prop(
																					'type',
																					'button'));
														} else {
															$cell
																	.text(text(row[column[0]])
																			|| '—');
														}
														$row.append($cell);
													});
											$body.append($row);
										});
								if (!state.rows.length)
									tableMessage('検索結果がありません。');
								Hexa.pages($pages[0], data, load);
								syncDraft();
							} catch (error) {
								showLoadError();
							}
						}).fail(function(xhr, textStatus) {
					if (!currentRequest(state, token))
						return;
					if (textStatus === 'abort' || textStatus === 'canceled') {
						tableMessage('照会がキャンセルされました。再度検索してください。');
						syncDraft();
						return;
					}
					showLoadError();
				}).always(function() {
					if (!currentRequest(state, token))
						return;
					pendingRequest = null;
					$body.removeAttr('aria-busy');
				});
	}
	function open(box, opener) {
		var $box = $(box);
		var kind = $box.attr('data-multi-filter');
		if (!kinds[kind])
			return;
		active = {
			box : box,
			kind : kind,
			opener : opener,
			draft : copy(read(box)),
			rows : [],
			keyword : $box.find('[data-multi-query]').val().trim()
		};
		$('#multi-filter-title').text(kinds[kind].label + ' 選択');
		$searchForm.find('[name=keyword]').val(active.keyword);
		var $row = $(Hexa.node('tr'));
		var $all = $(Hexa.node('input')).prop('type', 'checkbox').attr({
			id : 'multi-filter-page-all',
			'aria-label' : '現在のページをすべて選択'
		});
		$row.append($(Hexa.node('th')).append($all));
		kinds[kind].columns.forEach(function(column) {
			$row.append(Hexa.node('th', column[1]));
		});
		$head.empty().append($row);
		Hexa.openModal($dialog[0]);
		load(1);
		$searchForm.find('[name=keyword]')[0].focus();
	}

	$body.on('change.hexaMultiFilter',
			'[data-multi-row-id] input[type=checkbox]', function() {
				if (!active)
					return;
				var id = $(this).closest('[data-multi-row-id]').attr(
						'data-multi-row-id');
				var index = indexOf(active.rows, id);
				if (index >= 0) {
					toggle(active.rows[index], $(this).prop('checked'));
					syncDraft();
				}
			});
	$body.on('click.hexaMultiFilter', '.multi-result-toggle', function() {
		if (!active)
			return;
		var id = $(this).closest('[data-multi-row-id]').attr(
				'data-multi-row-id');
		var index = indexOf(active.rows, id);
		if (index >= 0) {
			toggle(active.rows[index], indexOf(active.draft, id) < 0);
			syncDraft();
		}
	});
	$head.on('change.hexaMultiFilter', '#multi-filter-page-all', function() {
		if (!active)
			return;
		var checked = $(this).prop('checked');
		active.rows.forEach(function(value) {
			toggle(value, checked);
		});
		syncDraft();
	});
	$boxes.each(function() {
		var initial = copy(read(this));
		initialFilters.push({
			box : this,
			form : $(this).closest('form')[0],
			values : initial
		});
		paint(this, initial, false);
	});
	$boxes.on('click.hexaMultiFilter', '[data-multi-open]', function() {
		open($(this).closest('[data-multi-filter]')[0], this);
	});
	$boxes.on('keydown.hexaMultiFilter', '[data-multi-query]', function(event) {
		var original = event.originalEvent || event;
		if (event.key === 'Enter' && !original.isComposing
				&& original.keyCode !== 229) {
			event.preventDefault();
			open($(this).closest('[data-multi-filter]')[0], this);
		}
	});
	$boxes.on('click.hexaMultiFilter', '[data-multi-remove]', function() {
		var $box = $(this).closest('[data-multi-filter]');
		var id = $(this).closest('[data-multi-id]').attr('data-multi-id');
		paint($box[0], read($box[0]).filter(function(value) {
			return value.id !== id;
		}), true);
		$box.find('[data-multi-query]')[0].focus();
	});
	$boxes.on('click.hexaMultiFilter', '[data-multi-clear]', function() {
		var $box = $(this).closest('[data-multi-filter]');
		var $query = $box.find('[data-multi-query]').val('');
		paint($box[0], [], true);
		$query[0].focus();
	});
	$boxes.closest('form').on(
			'reset.hexaMultiFilter',
			function(event) {
				var form = this;
				var original = event.originalEvent;
				setTimeout(function() {
					// リセットを押すと初期の検索条件に戻す。
					if (event.isDefaultPrevented()
							|| (original && original.defaultPrevented))
						return;
					initialFilters.forEach(function(initial) {
						if (initial.form !== form)
							return;
						$(initial.box).find('[data-multi-query]').val('');
						paint(initial.box, copy(initial.values), true);
					});
				}, 0);
			});
	// 入力中の検索語があれば先に選択ダイアログを開く。
	$('form.report-search').on('submit.hexaMultiFilter', function(event) {
		var $pending = $(this).find('[data-multi-query]').filter(function() {
			return $(this).val().trim();
		}).first();
		if ($pending.length) {
			event.preventDefault();
			open($pending.closest('[data-multi-filter]')[0], $pending[0]);
		}
	});
	$searchForm.on('submit.hexaMultiFilter', function(event) {
		event.preventDefault();
		if (!active)
			return;
		active.keyword = $(this.elements.keyword).val().trim();
		load(1);
	});
	$('#multi-filter-apply').on('click.hexaMultiFilter', function() {
		if (!active)
			return;
		var box = active.box;
		paint(box, copy(active.draft), true);
		$(box).find('[data-multi-query]').val('');
		Hexa.closeModal($dialog[0]);
	});
	$dialog.on('close.hexaMultiFilter', function() {
		// 選択ダイアログが再び開かれた場合、前回の閉じるイベントは無視する。
		if ($dialog.prop('open'))
			return;
		var opener = active && active.opener;
		active = null;
		cancelRequest();
		$body.removeAttr('aria-busy');
		if (opener && opener.isConnected)
			opener.focus();
	});
}(jQuery));
