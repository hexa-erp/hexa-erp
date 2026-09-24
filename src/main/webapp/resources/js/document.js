(function($) {
  'use strict';
  var $section = $('#document-lines');
  if (!$section.length)
    return;
  var $form = $section.closest('form'),
    $body = $('#line-body'),
    $template = $('#line-template'),
    $removed = $('#removed-line-ids');
  var idField = $section.attr('data-line-id-field'),
    sourceField = $section.attr('data-source-id-field'),
    priced = $section.attr('data-priced') === 'true';
  var originalRows = $body.html();
  // quotationLineId → quotationId처럼 상세 PK에 대응하는 최초 헤더 ID로 신규 여부를 판단한다.
  var headerIdField = idField.replace(/LineId$/, 'Id');
  var isNew = !$form.find('input[name="' + headerIdField + '"]').val();
  // hidden 입력은 native reset만으로 복원되지 않아 최초 ID·코드·이름을 별도로 보관한다.
  var originalSelectors = $form.find(
    '[data-selector-field] input[name], [data-selector-field] [data-reference-code]'
  ).map(function() {
    return { input: this, value: $(this).val() };
  }).get();

  function field(row, name) {
    return $(row).find('[data-field="' + name + '"]');
  }

  function assign(row, name, value) {
    field(row, name).val(value == null ? '' : value);
  }

  function decimalValue(row, name) {
    var $input = field(row, name);
    var validity = $input.prop('validity');
    if (validity && validity.badInput)
      return null;
    // 부동소수점 오차를 피하도록 원문 문자열을 계산에 전달한다.
    return $input.length && $input.val() !== '' ? $input.val() : '0';
  }

  function reindex() {
    $body.children('tr').each(function(i) {
      $(this).find('[data-line-number]').text(i + 1);
      $(this).find('[data-field]').each(function() {
        $(this).attr('name', 'lines[' + i + '].' + $(this).attr('data-field'));
      });
    });
  }
  // 부동소수점 오차를 피하기 위해 금액은 문자열로 합산한다.

  function recalc() {
    var decimal = window.HexaDecimal;
    var total = { quantity: '0', supplyAmount: '0', vatAmount: '0', totalAmount: '0' };
    $body.children('tr').each(function() {
      var row = this;
      var qty = decimalValue(row, 'quantity');
      total.quantity = decimal.add(total.quantity, qty);
      if (!priced)
        return;
      var amounts = decimal.lineAmounts(qty, decimalValue(row, 'unitPrice'));
      ['supplyAmount', 'vatAmount', 'totalAmount'].forEach(function(name) {
        var value = amounts ? amounts[name] : null;
        assign(row, name, value);
        $(row).find('[data-amount="' + name + '"]').text(decimal.format(value));
        total[name] = decimal.add(total[name], value);
      });
    });
    $('#line-total-quantity').text(decimal.format(total.quantity));
    if (priced) {
      $('#line-total-supply').text(decimal.format(total.supplyAmount));
      $('#line-total-vat').text(decimal.format(total.vatAmount));
      $('#line-total-amount').text(decimal.format(total.totalAmount));
    }
  }

  function addRow(values) {
    // 중복 실행을 피하기 위해 이벤트 없이 행 내용만 복제한다.
    var $content = $template.contents().clone(false, false);
    var $row = $content.filter('tr');
    $row.find('input').val('').prop('checked', false);
    ['itemId', 'itemCode', 'itemName', 'specification', 'unit', 'quantity', 'unitPrice', 'note']
      .forEach(function(name) {
        assign($row, name, values && values[name] != null ? values[name] : '');
      });
    if (values && sourceField)
      assign($row, sourceField, values[sourceField]);
    $body.append($content);
    reindex();
    recalc();
    return $row[0];
  }

  function addInitialRows() {
    if (isNew && !$body.children('tr').length) {
      for (var i = 0; i < 3; i++)
        addRow();
    }
  }

  function removeRow(row) {
    var id = field(row, idField).val();
    // 현재 전표의 기존 상세 ID만 기록한다. 원전표 ID나 신규 행의 빈 ID는 기록하지 않는다.
    if (id) {
      var $input = $(Hexa.node('input')).prop('type', 'hidden');
      $input.attr('name', 'removedLineIds').val(id);
      $removed.append($input);
    }
    $(row).remove();
    reindex();
    recalc();
  }

  $section.on('click.hexaDocument', '[data-line-add]', function() {
    addRow();
  });

  $section.on('click.hexaDocument', '[data-lines-remove]', function() {
    $body.children('tr').filter(function() {
      return $(this).find('.line-select').prop('checked');
    }).each(function() {
      removeRow(this);
    });
  });

  $section.on('click.hexaDocument', '[data-line-pick]', function() {
    var row = $(this).closest('[data-line-row]')[0];
    if (!row)
      return;
    Hexa.pick('item', function(item) {
      if (!item.itemId)
        return;
      ['itemId', 'itemCode', 'itemName', 'specification', 'unit'].forEach(function(k) {
        assign(row, k, item[k]);
      });
      assign(row, 'unitPrice', item.outboundPrice);
      if (sourceField)
        assign(row, sourceField, '');
      recalc();
    });
  });

  $section.on('input.hexaDocument', '[data-field="quantity"], [data-field="unitPrice"]', function() {
    recalc();
  });

  $form.on('reset.hexaDocument', function(event) {
    var nativeEvent = event.originalEvent;
    setTimeout(function() {
      // 뒤의 리스너에서 취소한 reset도 반영하지 않는다.
      if (event.isDefaultPrevented() || (nativeEvent && nativeEvent.defaultPrevented))
        return;
      originalSelectors.forEach(function(saved) {
        $(saved.input).val(saved.value);
      });
      if (window.HexaReference)
        window.HexaReference.refreshForm($form[0]);
      // 최초 서버 렌더링 HTML만 복원한다. 사용자 입력이나 API 문자열을 HTML로 합치지 않는다.
      $body.html(originalRows);
      $removed.empty();
      addInitialRows();
      reindex();
      recalc();
    }, 0);
  });
  var source = { type: '', keyword: '', status: '', doc: null };
  // 재조회 대기·실패 중에는 화면에 남은 버튼과 기존 데이터를 함께 유지한다.
  var sourceRows = [];
  // lineId·remainingQuantity는 원전표 응답값이며 UI에서 잔량을 계산하지 않는다.

  function loadSources(page) {
    source.doc = null;
    $('#source-detail').prop('hidden', true);
    Hexa.json('/lookup/sources/' + source.type
      + '?keyword=' + encodeURIComponent(source.keyword)
      + '&progressStatus=' + encodeURIComponent(source.status)
      + '&page=' + page
    ).then(function(data) {
      var $tbody = $('#source-body').empty();
      sourceRows = data.rows;
      data.rows.forEach(function(doc, i) {
        var $tr = $(Hexa.node('tr'));
        [
          String(doc.businessDate || '').replace(/-/g, '/') + '-' + doc.documentNo,
          doc.partnerName,
          doc.assigneeName,
          doc.itemSummary
        ].forEach(function(value) {
          $tr.append(Hexa.node('td', value || '—'));
        });
        var $due = $(Hexa.node('td', doc.dueDate || '—'));
        $due.prop('hidden', source.type !== 'order');
        $tr.append($due);
        $tr.append(Hexa.node('td',
          Hexa.number(source.type === 'shipping-instruction' ? doc.totalQuantity : doc.totalAmount),
          'text-right'));
        [{
          UNCONFIRMED: '미확인',
          IN_PROGRESS: '진행중',
          COMPLETED: '완료',
          CONFIRMED: '확인'
        }[doc.progressStatus]].forEach(function(value) {
          $tr.append(Hexa.node('td', value || '—'));
        });
        var $td = $(Hexa.node('td'));
        var $button = $(Hexa.node('button', '품목 선택', 'btn btn-sm'));
        // 목록 위치만 DOM에 둔다. 업무 ID·CODE는 응답 객체의 문자열 그대로 사용한다.
        $button.prop('type', 'button').attr('data-source-index', i);
        $tr.append($td.append($button));
        $tbody.append($tr);
      });
      if (!data.rows.length) {
        var $empty = $(Hexa.node('td', '검색 결과가 없습니다.', 'empty-state')).prop('colSpan', 8);
        $tbody.append($(Hexa.node('tr')).append($empty));
      }
      Hexa.pages($('#source-pages')[0], data, loadSources);
    }).catch(function() {
      Hexa.notice('원전표 목록을 불러오지 못했습니다. API 연결을 확인해 주세요.');
    });
  }

  $('#source-body').on('click.hexaDocument', 'button[data-source-index]', function() {
    source.doc = sourceRows[$(this).attr('data-source-index')];
    showSourceLines(source.doc);
  });

  function showSourceLines(doc) {
    var $rows = $('#source-lines').empty();
    doc.lines.forEach(function(line, i) {
      var $tr = $(Hexa.node('tr')), $td = $(Hexa.node('td'));
      var $cb = $(Hexa.node('input')).prop('type', 'checkbox').val(String(i));
      $cb.attr('aria-label', (line.itemName || '품목') + ' 선택');
      $tr.append($td.append($cb));
      [
        line.itemCode,
        line.itemName,
        line.specification,
        Hexa.number(line.quantity),
        line.remainingQuantity == null ? '—' : Hexa.number(line.remainingQuantity)
      ].forEach(function(v) {
        $tr.append(Hexa.node('td', v || '—'));
      });
      $rows.append($tr);
    });
    $('#source-detail').prop('hidden', false);
  }

  $('[data-source]').on('click.hexaDocument', function() {
    source = { type: $(this).attr('data-source'), keyword: '', status: '', doc: null };
    var label = { quotation: '견적서', order: '주문서', sale: '판매', 'shipping-instruction': '출하지시서' }[source.type];
    $('#source-title').text(label + ' 불러오기');
    $('#source-due-head').prop('hidden', source.type !== 'order');
    $('#source-amount-head').text(source.type === 'shipping-instruction' ? '수량' : '금액합계');
    var $search = $('#source-search');
    $search.find('[name="keyword"]').val('');
    var $sel = $search.find('[name="progressStatus"]').empty();
    [['', '전체'], ['UNCONFIRMED', '미확인']].concat(
      source.type === 'sale'
        ? [['CONFIRMED', '확인']]
        : [['IN_PROGRESS', '진행중'], ['COMPLETED', '완료']]
    ).filter(function(pair) {
      return !(source.type === 'shipping-instruction' && pair[0] === 'UNCONFIRMED');
    }).forEach(function(pair) {
      $sel.append($(Hexa.node('option', pair[1])).val(pair[0]));
    });
    Hexa.openModal('source-modal');
    loadSources(1);
  });

  $('#source-search').on('submit.hexaDocument', function(e) {
    e.preventDefault();
    source.keyword = $(this.elements.keyword).val().trim();
    source.status = $(this.elements.progressStatus).val();
    loadSources(1);
  });

  $('#source-apply').on('click.hexaDocument', function() {
    if (!source.doc) {
      Hexa.notice('원전표를 선택한 뒤 품목을 선택해 주세요.');
      return;
    }
    var selected = $('#source-lines input:checked').map(function() {
      return source.doc.lines[$(this).val()];
    }).get();
    if (!selected.length) {
      Hexa.notice('불러올 품목을 선택해 주세요.');
      return;
    }
    if (selected.some(function(line) {
      return line.remainingQuantity == null;
    })) {
      Hexa.notice('적용 가능 수량이 없습니다. 원전표 조회 API에서 잔량을 전달해야 합니다.');
      return;
    }
    $body.children('tr').filter(function() {
      return !field(this, 'itemId').val() && !field(this, idField).val();
    }).remove();
    selected.forEach(function(line) {
      var copy = $.extend({}, line);
      copy.quantity = line.remainingQuantity;
      copy[sourceField] = line.lineId;
      addRow(copy);
    });
    if (window.HexaReference) {
      window.HexaReference.applySource($form[0], source.doc);
    } else {
      ['partnerId', 'partnerName', 'assigneeId', 'assigneeName', 'warehouseId', 'warehouseName'].forEach(function(key) {
        var $input = $($form[0].elements[key]);
        if ($input.length && !$input.val())
          $input.val(source.doc[key] || '');
      });
    }
    Hexa.closeModal('source-modal');
    reindex();
    recalc();
  });
  addInitialRows();
  reindex();
  recalc();
}(jQuery));
