(function($) {
  'use strict';
  // .data()의 자동 변환·캐시 없이 현재 속성의 문자열을 읽는다.
  var ctx = $('body').attr('data-context-path') || '';

  function openModal(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    // native dialog가 Esc, 포커스 복원, 모달의 top layer를 담당한다.
    if (el && !$(el).prop('open'))
      el.showModal();
  }

  function closeModal(id) {
    var el = typeof id === 'string' ? document.getElementById(id) : id;
    if (el && $(el).prop('open'))
      el.close();
  }

  function notice(message) {
    $('#notice-message').text(String(message || '아직 저장 기능이 구현되지 않았습니다. 실제 데이터는 저장되지 않습니다.'));
    openModal('notice-modal');
  }

  function node(tag, value, cls) {
    var $el = $(document.createElement(tag));
    if (value != null)
      $el.text(String(value));
    if (cls)
      $el.attr('class', cls);
    // 호출부 호환성을 위해 jQuery 객체가 아닌 DOM 요소를 반환한다.
    return $el[0];
  }

  function json(url) {
    // 호출부의 then/catch가 JSON 값 또는 Error를 받도록 native Promise를 반환한다.
    return new Promise(function(resolve, reject) {
      $.ajax({
        url: ctx + url,
        type: 'GET',
        // 빈 응답(204 포함)도 JSON 오류로 처리하기 위해 직접 파싱한다.
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

  function pages(el, data, callback) {
    var $el = $(el).empty();
    $el.append(node('span', '총 ' + data.totalCount + '건 · 25개씩', 'muted'));
    for (var i = 1; i <= data.totalPages; i++) {
      var $button = $(node('button', i, 'page-button ' + (i === data.page ? 'is-active' : '')));
      $button.prop('type', 'button').attr('data-page', i);
      $el.append($button);
    }
    // 재조회 때 핸들러가 중복되지 않도록 이 컨테이너의 페이지 이벤트만 교체한다.
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
  var labels = { partner: '거래처', warehouse: '창고', assignee: '담당자', item: '품목' };

  function loadPicker(page) {
    var $body = $('#picker-body').empty();
    picker.rows = [];
    $body.append(node('tr', '조회 중…'));
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
            var $button = $(node('button', row[f] || '선택', 'btn btn-sm'));
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
        var $empty = $(node('td', '검색 결과가 없습니다.', 'empty-state')).prop('colSpan', 4);
        $body.append($(node('tr')).append($empty));
      }
      pages($('#picker-pages')[0], data, loadPicker);
    }).catch(function() {
      $body.empty();
      notice('선택 목록을 불러오지 못했습니다. 조회 API 연결을 확인해 주세요.');
    });
  }

  function pick(type, callback, initialKeyword) {
    picker = {
      type: type,
      callback: callback,
      keyword: initialKeyword == null ? '' : String(initialKeyword),
      rows: []
    };
    $('#picker-title').text(labels[type] + ' 선택');
    $('#picker-search input').val(picker.keyword);
    var $head = $('#picker-head').empty();
    var $tr = $(node('tr'));
    var columns = {
      partner: ['거래처코드', '거래처명'],
      warehouse: ['창고코드', '창고명', '구분'],
      assignee: ['담당자코드', '담당자명'],
      item: ['품목코드', '품목명', '규격', '현재고']
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
        notice('변경할 전표를 먼저 선택해 주세요.');
        return;
      }
      $(statusForm.elements.nextProgressStatus).val($(statusOption).attr('data-status-value'));
      // submitter의 name/value·formaction·formnovalidate는 브라우저가 처리한다.
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
      // 코드 입력 선택기는 reference-selector.js만 처리한다(중복 선택 방지).
      if (!$box.length || $box.is('[data-code-selector]'))
        return;
      pick($trigger.attr('data-pick'), function(row) {
        $box.find('input[name]').each(function() {
          var name = this.name.replace(/^.*\./, '');
          $(this).val(row[name] == null ? '' : row[name]);
          // native와 jQuery 리스너에 change를 한 번만 전달한다.
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

  // GET 검색 이름을 직접 고치면 ID 검색을 해제한다. POST의 스냅샷 ID는 유지한다.
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
    // 실제 submitter를 사용해 버튼별 제출 속성을 보존한다.
    var event = e.originalEvent || e;
    var form = event.target;
    var $form = $(form);
    // readonly는 native required 검증에서 제외되므로 필수 선택값을 따로 검사한다.
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
        notice('필수 항목을 선택해 주세요: ' + ($button.length ? labels[$button.attr('data-pick')] : '선택값'));
        return;
      }
    }
    // 미구현 Form만 차단하며 일반 Form 제출은 막지 않는다.
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
    node: node,
    pages: pages,
    number: function(v) {
      return Number(v || 0).toLocaleString('ko-KR', { maximumFractionDigits: 3 });
    }
  };
}(jQuery));
