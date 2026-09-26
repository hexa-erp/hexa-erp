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
  // 전표 ID가 있는지 확인해 신규 입력과 수정을 구분한다.
  var headerIdField = idField.replace(/LineId$/, 'Id');
  var isNew = !$form.find('input[name="' + headerIdField + '"]').val();
  // '다시 작성'에 사용할 처음 선택값을 보관한다.
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
    // 저장된 상세행을 삭제할 때 해당 ID를 서버에 전달한다.
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
      if (event.isDefaultPrevented() || (nativeEvent && nativeEvent.defaultPrevented))
        return;
      originalSelectors.forEach(function(saved) {
        $(saved.input).val(saved.value);
      });
      if (window.HexaReference)
        window.HexaReference.refreshForm($form[0]);
      // 처음 열었을 때의 품목 행으로 되돌린다.
      $body.html(originalRows);
      $removed.empty();
      addInitialRows();
      reindex();
      recalc();
    }, 0);
  });
  var source = { type: '', keyword: '', status: '', doc: null };
  var sourceRows = [];
  var sourceRequestId = 0;
  // 불러올 수량은 서버에서 계산한 remainingQuantity를 사용한다.

  function loadSources(page) {
    var requestId = ++sourceRequestId;
    source.doc = null;
    sourceRows = [];
    $('#source-detail').prop('hidden', true);
    $('#source-pages').empty();
    var $loading = $(Hexa.node('td', '조회 중입니다.', 'empty-state')).prop('colSpan', 8);
    $('#source-body').empty().append($(Hexa.node('tr')).append($loading));
    Hexa.json('/lookup/sources/' + source.type
      + '?keyword=' + encodeURIComponent(source.keyword)
      + '&progressStatus=' + encodeURIComponent(source.status)
      + '&page=' + page
    ).then(function(data) {
      if (requestId !== sourceRequestId)
        return;
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
      if (requestId !== sourceRequestId)
        return;
      var $error = $(Hexa.node('td', '목록을 불러오지 못했습니다. 다시 검색해 주세요.', 'empty-state')).prop('colSpan', 8);
      $('#source-body').empty().append($(Hexa.node('tr')).append($error));
      Hexa.notice('원전표 목록을 불러오지 못했습니다.');
    });
  }

  $('#source-body').on('click.hexaDocument', 'button[data-source-index]', function() {
    source.doc = sourceRows[$(this).attr('data-source-index')];
    showSourceLines(source.doc);
  });

  function showSourceLines(doc) {
    var $rows = $('#source-lines').empty();
    $('#source-detail [data-check-all]').prop('checked', false);
    doc.lines.forEach(function(line, i) {
      var $tr = $(Hexa.node('tr')), $td = $(Hexa.node('td'));
      var $cb = $(Hexa.node('input')).prop('type', 'checkbox').val(String(i));
      $cb.attr('aria-label', (line.itemName || '품목') + ' 선택');
      $cb.prop('disabled', line.remainingQuantity == null || Number(line.remainingQuantity) <= 0);
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
    var selected = $('#source-lines input:checked:not(:disabled)').map(function() {
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
    ['note', 'deliveryContact', 'deliveryPostalCode', 'deliveryAddress'].forEach(function(key) {
      var $input = $($form[0].elements[key]);
      if ($input.length && !$input.val() && source.doc[key] != null)
        $input.val(source.doc[key]);
    });
    Hexa.closeModal('source-modal');
    reindex();
    recalc();
  });
  addInitialRows();
  reindex();
  recalc();
}(jQuery));
