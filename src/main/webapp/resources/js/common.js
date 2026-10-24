(function($) {
  'use strict';
  var ctx = $('body').attr('data-context-path') || '';

  function openModal(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    if (el && !$(el).prop('open'))
      el.showModal();
  }

  function closeModal(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    if (el && $(el).prop('open'))
      el.close();
  }

  function notice(message) {
    $('#notice-message').text(String(message || 'この機能はまだ実装されていません。データは変更されません。'));
    openModal('notice-modal');
  }

  function node(tag, value, cls) {
    var $el = $(document.createElement(tag));
    if (value != null)
      $el.text(String(value));
    if (cls)
      $el.attr('class', cls);
    return $el[0];
  }

  function json(url) {
    return new Promise(function(resolve, reject) {
      $.ajax({
        url: ctx + url,
        type: 'GET',
        // 応答をJSONとして読み取り、空の場合はエラーにする。
        dataType: 'text',
        headers: { Accept: 'application/json' },
        success: function(response, textStatus, xhr) {
          if (xhr.status < 200 || xhr.status >= 300) {
            reject(new Error('HTTP ' + xhr.status));
            return;
          }
          try {
            resolve(JSON.parse(response == null ? '' : response));
          } catch (error) {
            reject(error);
          }
        },
        error: function(xhr) {
          reject(xhr.status ? new Error('HTTP ' + xhr.status) : new TypeError('Failed to fetch'));
        }
      });
    });
  }

  // 変更リクエストはJSON応答を受け取り、サーバーのエラーメッセージを呼び出し元の画面へ渡す。
  function request(url, method, data, multipart) {
    return new Promise(function(resolve, reject) {
      $.ajax({
        url: ctx + url,
        type: method,
        data: multipart ? data : JSON.stringify(data),
        processData: false,
        contentType: multipart ? false : 'application/json; charset=UTF-8',
        dataType: 'json',
        headers: { Accept: 'application/json' },
        success: resolve,
        error: function(xhr) {
          var body = xhr.responseJSON;
          reject(new Error(body && body.message ? body.message
            : (xhr.status ? 'リクエストを処理できませんでした。入力内容を確認してから再度お試しください。'
              : 'サーバーに接続できません。接続状態を確認してください。')));
        }
      });
    });
  }

  // フォームのlines[0].itemIdなどをJSON配列へ変換する。金額とIDは精度を保つため文字列で送る。
  function formPayload(form) {
    var payload = {};
    $(form).serializeArray().forEach(function(entry) {
      if (entry.name.indexOf('return.') === 0)
        return;
      var match = /^(lines|changes)\[(\d+)\]\.([A-Za-z][A-Za-z0-9]*)$/.exec(entry.name);
      var name = match ? match[3] : entry.name;
      var value = entry.value;
      if (value === '' && (/(Id|Price)$/.test(name) || name === 'quantity'))
        value = null;
      if (name === 'selected')
        value = value === 'true';
      if (match) {
        var rows = payload[match[1]] || (payload[match[1]] = []);
        var row = rows[Number(match[2])] || (rows[Number(match[2])] = {});
        row[name] = value;
      } else if (name === 'removedLineIds') {
        (payload.removedLineIds || (payload.removedLineIds = [])).push(value);
      } else if (/^[A-Za-z][A-Za-z0-9]*$/.test(name)) {
        payload[name] = value;
      }
    });
    ['lines', 'changes'].forEach(function(name) {
      if (payload[name])
        payload[name] = payload[name].filter(function(row) { return !!row; });
    });
    return payload;
  }

  var returnKeys = ['keyword', 'progressStatus', 'includeInactive', 'page', 'pageSize',
    'datePreset', 'startDate', 'endDate', 'view', 'warehouseIds', 'partnerIds', 'itemIds',
    'assigneeIds', 'warehouseId', 'partnerId', 'itemId', 'assigneeId'];

  function returnUrl(form, path, results) {
    var params = new URLSearchParams();
    $(form).serializeArray().forEach(function(entry) {
      if (entry.name.indexOf('return.') !== 0)
        return;
      var key = entry.name.slice(7);
      if (returnKeys.indexOf(key) !== -1)
        params.append(key, entry.value);
    });
    if (!params.has('page'))
      params.set('page', '1');
    if (!params.has('pageSize'))
      params.set('pageSize', '25');
    if (results)
      params.set('view', 'results');
    return ctx + path + '?' + params.toString();
  }

  function sendForm(form, url, method, payload, success) {
    var $form = $(form);
    if ($form.data('apiPending'))
      return;
    $form.data('apiPending', true);
    var $buttons = $form.find('button:not(:disabled), input[type="submit"]:not(:disabled)');
    $buttons.prop('disabled', true);
    request(url, method, payload, false).then(function(response) {
      success(response);
    }).catch(function(error) {
      notice(error.message);
    }).then(function() {
      $form.removeData('apiPending');
      $buttons.prop('disabled', false);
    });
  }

  function submitApiForm(form, submitter) {
    var $form = $(form);
    if ($form.is('[data-api-document]')) {
      var payload = formPayload(form);
      var id = payload[$form.attr('data-api-id-field')];
      var apiBase = $form.attr('data-api-base');
      var viewBase = $form.attr('data-api-view-base');
      payload.lines = payload.lines || [];
      payload.removedLineIds = payload.removedLineIds || [];
      sendForm(form, apiBase + (id ? '/' + encodeURIComponent(id) : ''), id ? 'PUT' : 'POST',
        payload, function() {
          window.location.assign(id ? returnUrl(form, viewBase + '/list') : ctx + viewBase + '/form');
        });
      return;
    }
    if ($form.is('[data-api-selection]')) {
      var ids = $form.find('input[name="selectedIds"]:checked').map(function() {
        return this.value;
      }).get();
      if (!ids.length) {
        notice('処理する伝票を先に選択してください。');
        return;
      }
      var deleting = $(submitter).is('[data-api-delete]');
      var body = { ids: ids };
      if (!deleting)
        body.progressStatus = $(form.elements.nextProgressStatus).val();
      sendForm(form, $form.attr('data-api-base') + (deleting ? '' : '/status'),
        deleting ? 'DELETE' : 'PATCH', body, function() { window.location.reload(); });
      return;
    }
    if ($form.is('[data-api-prices]')) {
      var prices = formPayload(form);
      prices.changes = (prices.changes || []).filter(function(row) { return row.selected; });
      if (!prices.changes.length) {
        notice('単価を変更する行を選択してください。');
        return;
      }
      sendForm(form, '/api/sales/prices', 'PATCH', prices, function() {
        window.location.assign(returnUrl(form, '/sale/bulk-price', true));
      });
    }
  }

  function pages(el, data, callback) {
    var $el = $(el).empty();
    $el.append(node('span', '全 ' + data.totalCount + '件・25件ずつ', 'muted'));
    for (var i = 1; i <= data.totalPages; i++) {
      var $button = $(node('button', i, 'page-button ' + (i === data.page ? 'is-active' : '')));
      $button.prop('type', 'button').attr('data-page', i);
      $el.append($button);
    }
    // ページ移動イベントが重複登録されないよう更新する。
    $el.off('click.hexaPages', 'button[data-page]');
    $el.on('click.hexaPages', 'button[data-page]', function() {
      callback(Number($(this).attr('data-page')));
    });
  }

  var picker = { type: '', callback: null, keyword: '', rows: [] };
  var fields = {
    partner: ['partnerCode', 'partnerName'],
    warehouse: ['warehouseCode', 'warehouseName', 'warehouseType'],
    assignee: ['assigneeCode', 'assigneeName'],
    item: ['itemCode', 'itemName', 'specification', 'stockQuantity']
  };
  var labels = { partner: '取引先', warehouse: '倉庫', assignee: '担当者', item: '品目' };

  function loadPicker(page) {
    var $body = $('#picker-body').empty();
    picker.rows = [];
    $body.append(node('tr', '照会中…'));
    json('/lookup/options/' + picker.type
      + '?keyword=' + encodeURIComponent(picker.keyword)
      + '&page=' + page
      + '&warehouseId=' + encodeURIComponent($('[name=warehouseId]').first().val() || '')
    ).then(function(data) {
      $body.empty();
      picker.rows = data.rows;
      data.rows.forEach(function(row, rowIndex) {
        var $tr = $(node('tr'));
        fields[picker.type].forEach(function(f, i) {
          var $td = $(node('td'));
          if (i === 0 || i === 1) {
            var $button = $(node('button', row[f] || '選択', 'btn btn-sm'));
            $button.prop('type', 'button').attr('data-picker-index', rowIndex);
            $td.append($button);
          } else {
            $td.text(row[f] == null ? '—' : String(row[f]));
          }
          $tr.append($td);
        });
        $body.append($tr);
      });
      if (!data.rows.length) {
        var $empty = $(node('td', '検索結果がありません。', 'empty-state')).prop('colSpan', 4);
        $body.append($(node('tr')).append($empty));
      }
      pages($('#picker-pages')[0], data, loadPicker);
    }).catch(function() {
      $body.empty();
      notice('選択一覧を取得できませんでした。照会APIの接続を確認してください。');
    });
  }

  function pick(type, callback, initialKeyword) {
    picker = {
      type: type,
      callback: callback,
      keyword: initialKeyword == null ? '' : String(initialKeyword),
      rows: []
    };
    $('#picker-title').text(labels[type] + ' 選択');
    $('#picker-search input').val(picker.keyword);
    var $head = $('#picker-head').empty();
    var $tr = $(node('tr'));
    var columns = {
      partner: ['取引先コード', '取引先名'],
      warehouse: ['倉庫コード', '倉庫名', '区分'],
      assignee: ['担当者コード', '担当者名'],
      item: ['品目コード', '品目名', '規格', '現在庫']
    };
    columns[type].forEach(function(c) {
      $tr.append(node('th', c));
    });
    $head.append($tr);
    openModal('picker-modal');
    loadPicker(1);
  }

  $('#picker-body').on('click.hexaPicker', 'button[data-picker-index]', function() {
    var row = picker.rows[Number($(this).attr('data-picker-index'))];
    closeModal('picker-modal');
    picker.callback(row);
  });

  $('#picker-search').on('submit.hexaPicker', function(e) {
    e.preventDefault();
    picker.keyword = $(this.elements.keyword).val().trim();
    loadPicker(1);
  });

  function closeStatusMenus() {
    $('[data-status-toggle]').each(function() {
      $(this).attr('aria-expanded', 'false');
      $(document.getElementById($(this).attr('aria-controls'))).prop('hidden', true);
    });
  }

  function openStatusMenu(button) {
    closeStatusMenus();
    $(button).attr('aria-expanded', 'true');
    $(document.getElementById($(button).attr('aria-controls'))).prop('hidden', false);
  }

  $(document).on('click.hexaCommon', function(e) {
    var $target = $(e.target);
    var searchStatus = $target.closest('[data-search-status]')[0];
    if (searchStatus) {
      var searchForm = searchStatus.form;
      if (!searchForm || !$(searchForm).is('[data-status-search]'))
        return;
      $(searchForm.elements.progressStatus).val($(searchStatus).attr('data-search-status'));
      if (searchForm.elements.page)
        $(searchForm.elements.page).val('1');
      $(searchForm).find('[data-search-status]').each(function() {
        var active = this === searchStatus;
        $(this).toggleClass('btn-primary', active).attr('aria-pressed', String(active));
      });
      searchForm.requestSubmit();
      return;
    }
    if (!$target.closest('.action-dropdown').length)
      closeStatusMenus();
    var statusToggle = $target.closest('[data-status-toggle]')[0];
    if (statusToggle) {
      if ($(statusToggle).attr('aria-expanded') === 'true')
        closeStatusMenus();
      else
        openStatusMenu(statusToggle);
      return;
    }
    var statusOption = $target.closest('[data-status-value]')[0];
    if (statusOption) {
      var statusForm = statusOption.form;
      closeStatusMenus();
      if (!$(statusForm).find('input[name="selectedIds"]:checked').length) {
        notice('変更する伝票を先に選択してください。');
        return;
      }
      $(statusForm.elements.nextProgressStatus).val($(statusOption).attr('data-status-value'));
      // 進捗状況変更ボタンの送信先を使う。
      statusForm.requestSubmit($(statusForm).find('[data-status-submit]')[0]);
      return;
    }
    var $close = $target.closest('[data-modal-close]');
    if ($close.length) {
      closeModal($close.closest('dialog')[0]);
      return;
    }
    var $action = $target.closest('[data-notice-action]');
    if ($action.length) {
      e.preventDefault();
      var message = $action.attr('data-notice-action');
      notice(message && message.length > 15 ? message : undefined);
      return;
    }
    var $trigger = $target.closest('[data-pick]');
    if ($trigger.length) {
      var $box = $trigger.closest('[data-selector-field]');
      // コード入力による選択はreference-selector.jsで処理する。
      if (!$box.length || $box.is('[data-code-selector]'))
        return;
      pick($trigger.attr('data-pick'), function(row) {
        $box.find('input[name]').each(function() {
          var name = this.name.replace(/^.*\./, '');
          $(this).val(row[name] == null ? '' : row[name]);
          this.dispatchEvent(new Event('change', { bubbles: true }));
        });
      });
    }
  });

  $(document).on('keydown.hexaCommon', '.action-dropdown', function(e) {
    var $dropdown = $(this);
    var toggle = $dropdown.find('[data-status-toggle]')[0];
    if (e.key === 'Escape') {
      toggle.focus();
      closeStatusMenus();
    }
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault();
      openStatusMenu(toggle);
      var $options = $dropdown.find('[data-status-value]');
      var index = $options.index(e.target);
      index = index < 0
        ? (e.key === 'ArrowDown' ? 0 : $options.length - 1)
        : (index + (e.key === 'ArrowDown' ? 1 : -1) + $options.length) % $options.length;
      $options[index].focus();
    }
  });

  $(document).on('click.hexaCommon', 'dialog', function(e) {
    if (e.target === this) {
      var event = e.originalEvent || e;
      var r = this.getBoundingClientRect();
      if (event.clientX < r.left || event.clientX > r.right || event.clientY < r.top || event.clientY > r.bottom)
        this.close();
    }
  });

  $(document).on('change.hexaCommon', '[data-check-all], [data-date-preset]', function() {
    var $input = $(this);
    if ($input.is('[data-check-all]')) {
      var selector = $input.attr('data-check-all');
      var $targets = selector
        ? $(selector)
        : $($input.closest('table')[0] || this.form).find('tbody input[type=checkbox]');
      $targets.filter(function() {
        return !$(this).prop('disabled');
      }).prop('checked', $input.prop('checked'));
    }
    if ($input.is('[data-date-preset]'))
      setDates(this);
  });

  // 検索画面（GET）で名称を変更すると、選択済みIDを空にする。
  $(document).on('input.hexaCommon', 'input[name$="Name"]', function() {
    var input = this;
    if (!input.form || input.form.method.toLowerCase() !== 'get')
      return;
    var $box = $(input).closest('[data-selector-field]');
    if (!$box.length)
      return;
    var idName = input.name.slice(0, -4) + 'Id';
    $box.find('input[type="hidden"][name]').filter(function() {
      return this.name === idName;
    }).val('');
  });

  $(document).on('submit.hexaCommon', 'form', function(e) {
    var event = e.originalEvent || e;
    var form = event.target;
    var $form = $(form);
    if (e.isDefaultPrevented())
      return;
    if ($form.data('apiPending')) {
      e.preventDefault();
      return;
    }
    if ($(event.submitter).is('[data-confirm-delete]')) {
      if (!$form.find('input[name="selectedIds"]:checked').length) {
        e.preventDefault();
        notice('削除する伝票を先に選択してください。');
        return;
      }
      if (!window.confirm('選択した伝票を削除しますか？')) {
        e.preventDefault();
        return;
      }
    }
    // 読み取り専用の入力欄はrequiredで検証されないため個別に確認する。
    if ($form.is('[data-document-form]') && form.method.toLowerCase() === 'post') {
      var $missing = $form.find('[data-selector-field] input[readonly][required][name$="Name"]').filter(function() {
        var $input = $(this);
        var idName = this.name.slice(0, -4) + 'Id';
        var $id = $input.closest('[data-selector-field]').find('input[type="hidden"][name]').filter(function() {
          return this.name === idName;
        }).first();
        return $id.length && (!$input.val().trim() || !$id.val().trim());
      }).first();
      if ($missing.length) {
        e.preventDefault();
        var $button = $missing.closest('[data-selector-field]').find('[data-pick]').first();
        notice('必須項目を選択してください: ' + ($button.length ? labels[$button.attr('data-pick')] : '選択値'));
        return;
      }
    }
    if ($form.is('[data-api-document], [data-api-selection], [data-api-prices]')) {
      e.preventDefault();
      submitApiForm(form, event.submitter);
      return;
    }
    if ($form.is('[data-unimplemented-submit]')) {
      e.preventDefault();
      notice();
    }
  });

  function iso(d) {
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
      + '-' + String(d.getDate()).padStart(2, '0');
  }

  function setDates(select) {
    var form = select.form, now = new Date(), start = new Date(now), end = new Date(now), v = $(select).val();
    if (v === 'custom' || v === '')
      return;
    if (v === 'today') { } else if (v === 'yesterday') {
      start.setDate(start.getDate() - 1);
      end = new Date(start);
    } else if (v === 'thisWeek' || v === 'week') {
      start.setDate(start.getDate() - ((start.getDay() + 6) % 7));
    } else if (v === 'lastWeek') {
      start.setDate(start.getDate() - ((start.getDay() + 6) % 7) - 7);
      end = new Date(start);
      end.setDate(end.getDate() + 6);
    } else if (v === 'thisMonth' || v === 'month') {
      start.setDate(1);
    } else if (v === 'lastMonth') {
      start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      end = new Date(now.getFullYear(), now.getMonth(), 0);
    } else if (v === 'year') {
      start = new Date(now.getFullYear(), 0, 1);
    } else
      return;
    if (form.elements.startDate)
      $(form.elements.startDate).val(iso(start));
    if (form.elements.endDate)
      $(form.elements.endDate).val(iso(end));
    if (form.elements.cutoffDate)
      $(form.elements.cutoffDate).val(iso(end));
  }

  window.Hexa = {
    ctx: ctx,
    openModal: openModal,
    closeModal: closeModal,
    notice: notice,
    pick: pick,
    json: json,
    request: request,
    node: node,
    pages: pages,
    number: function(v) {
      return Number(v || 0).toLocaleString('ja-JP', { maximumFractionDigits: 3 });
    }
  };

  $(document).on('click.hexaCommon', '[data-history-back]', function() {
    window.history.back();
  });

  $(function() {
    var errorMessage = $('#server-error-message').text().trim();
    if (errorMessage)
      notice(errorMessage);
  });
}(jQuery));
