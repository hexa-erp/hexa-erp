/* document-form.test.html을 Chrome에서 열어 실행한다. 추가 라이브러리 없이 실제 UI 스크립트를 검사한다. */
(function() {
  'use strict';
  var types = [
    ['quotation', 'quotation', '', true],
    ['order', 'salesOrder', 'quotationLineId', true],
    ['sale', 'sale', 'salesOrderLineId', true],
    ['shipping-instruction', 'shipInstruction', 'saleLineId', false],
    ['shipment', 'shipment', 'shipInstructionLineId', false]
  ];
  var cases = [];
  types.forEach(function(type) {
    [[false, 0], [true, 2], [true, 0], [false, 2]].forEach(function(state) {
      cases.push({ type: type[0], header: type[1] + 'Id', line: type[1] + 'LineId',
        source: type[2], priced: type[3], edit: state[0], rows: state[1] });
    });
  });
  var scripts = ['vendor/jquery-3.7.1.min.js', 'decimal.js', 'common.js', 'reference-selector.js', 'document.js'];
  var result = window.documentFormTestResult = { complete: false, passed: 0, failed: 0, checks: 0, cases: [] };
  var index = 0, frame, timer;

  function input(name, value, extra) {
    return '<input name="' + name + '" value="' + value + '" ' + (extra || '') + '>';
  }
  function selector(type, prefix) {
    return '<div data-selector-field data-code-selector="' + type + '">'
      + input(type + 'Id', prefix + '01', 'type="hidden"')
      + '<input data-reference-code="' + type + 'Code" value="00001">'
      + input(type + 'Name', '최초 ' + type, 'readonly')
      + '<button type="button" data-pick="' + type + '">선택</button></div>';
  }
  function row(c, i, saved) {
    var values = {};
    values[c.line] = saved ? String(700 + i) : '';
    if (c.source) values[c.source] = saved ? String(900 + i) : '';
    Object.assign(values, { itemId: saved ? String(500 + i) : '', itemCode: saved ? '00001' : '',
      itemName: saved ? '기존 품목' : '', specification: '', unit: 'EA', quantity: saved ? '1.005' : '', note: '' });
    if (c.priced) Object.assign(values, { unitPrice: saved ? '100' : '', supplyAmount: '', vatAmount: '', totalAmount: '' });
    return '<tr data-line-row><td><input type="checkbox" class="line-select"><span data-line-number></span>'
      + Object.keys(values).map(function(key) {
        return input('lines[' + i + '].' + key, values[key], 'data-field="' + key + '"');
      }).join('') + '<button type="button" data-line-pick>품목 선택</button>'
      + '<output data-amount="supplyAmount"></output><output data-amount="vatAmount"></output>'
      + '<output data-amount="totalAmount"></output></td></tr>';
  }
  function fixture(c) {
    var rows = '';
    for (var i = 0; i < c.rows; i++) rows += row(c, i, true);
    return '<form id="document-form" data-document-form data-unimplemented-submit method="post" action="/' + c.type + '/save">'
      + input(c.header, c.edit ? '1001' : '', 'type="hidden"')
      + selector('partner', '1') + selector('assignee', '2') + selector('warehouse', '3')
      + '<section id="document-lines" data-line-id-field="' + c.line + '" data-source-id-field="' + c.source
      + '" data-priced="' + c.priced + '"><button type="button" data-line-add>행 추가</button>'
      + '<button type="button" data-lines-remove>선택 삭제</button><table><tbody id="line-body">' + rows + '</tbody></table>'
      + '<template id="line-template"><table></table></template><div id="removed-line-ids"></div></section>'
      + '<span id="line-total-quantity"></span><span id="line-total-supply"></span><span id="line-total-vat"></span>'
      + '<span id="line-total-amount"></span><button type="reset">다시 작성</button></form>'
      + '<form id="other-form">' + selector('partner', '8') + selector('assignee', '9') + '</form>';
  }
  function run(c) {
    var checks = 0, nextRow, picks = 0;
    var $ = window.jQuery, form = document.getElementById('document-form');
    var $form = $(form), $body = $('#line-body');
    function equal(actual, expected, label) {
      checks++;
      if (JSON.stringify(actual) !== JSON.stringify(expected))
        throw new Error(label + ': ' + JSON.stringify(actual) + ' != ' + JSON.stringify(expected));
    }
    function selected(type, target) {
      var $box = $(target || form).find('[data-code-selector="' + type + '"]');
      return [$box.find('[name="' + type + 'Id"]').val(), $box.find('[data-reference-code]').val(),
        $box.find('[name="' + type + 'Name"]').val()];
    }
    function pick(type, data, target) {
      nextRow = data;
      $(target || form).find('[data-pick="' + type + '"]').trigger('click');
    }
    function snapshot() { return Array.from(new FormData(form).entries()); }
    function reset() { form.reset(); return new Promise(function(resolve) { setTimeout(resolve, 10); }); }
    function checkIndexes() {
      $body.children('tr').each(function(i) {
        $(this).find('[data-field]').each(function() {
          equal(this.name, 'lines[' + i + '].' + $(this).attr('data-field'), '행 인덱스');
        });
      });
    }
    Hexa.pick = function(type, callback) { picks++; callback(nextRow); };
    Hexa.notice = function(message) { throw new Error(message); };
    (async function() {
      var expectedRows = c.rows || (c.edit ? 0 : 3);
      equal($body.children('tr').length, expectedRows, '최초 행 수');
      equal($form.attr('method'), 'post', '제출 방식');
      equal($form.attr('action'), '/' + c.type + '/save', '제출 경로');
      var initial = snapshot(), counts = {};
      $form.find('[data-code-selector] input').each(function() {
        var key = this.name || $(this).attr('data-reference-code');
        counts[key] = 0;
        this.addEventListener('change', function() { counts[key]++; });
      });
      var linked = { partnerId: 42, partnerCode: '00002', partnerName: '새 거래처',
        assigneeId: 102, assigneeCode: '00007', assigneeName: '담당자' };
      pick('partner', linked);
      equal(selected('partner'), ['42', '00002', '새 거래처'], '거래처 선택');
      equal(selected('assignee'), ['102', '00007', '담당자'], '담당자 자동 반영');
      ['partnerId', 'partnerCode', 'partnerName', 'assigneeId', 'assigneeCode', 'assigneeName'].forEach(function(key) {
        equal(counts[key], 1, key + ' change 1회');
      });
      var manual = { assigneeId: 103, assigneeCode: '00009', assigneeName: '수동 담당자' };
      pick('assignee', manual);
      equal(selected('assignee'), ['103', '00009', '수동 담당자'], '수동 선택 유지');
      pick('partner', { partnerId: 43, partnerCode: '00003', partnerName: '미지정', assigneeName: '이름만 있음' });
      equal(selected('assignee'), ['', '', ''], '담당자 없는 거래처');
      pick('partner', linked);
      pick('partner', { partnerId: 44, partnerCode: '00004', partnerName: '불완전', assigneeId: 102 });
      equal(selected('assignee'), ['', '', ''], '불완전한 담당자 값도 섞지 않음');
      await reset();
      equal(snapshot(), initial, '헤더·선택·최초 행 초기화');
      pick('partner', linked, document.getElementById('other-form'));
      equal(selected('assignee', document.getElementById('other-form')), ['901', '00001', '최초 assignee'], '입력 Form 밖 전파 없음');
      $form.find('[data-code-selector="partner"] input').val('');
      HexaReference.applySource(form, linked);
      equal(selected('assignee'), ['201', '00001', '최초 assignee'], '원전표는 기존 담당자 보존');
      await reset();
      $form.find('[data-line-add]').trigger('click');
      equal($body.children('tr').length, expectedRows + 1, '행 추가 1회');
      nextRow = { itemId: 501, itemCode: '00001', itemName: '품목', unit: 'EA', specification: '규격', outboundPrice: 100 };
      $body.children('tr').last().find('[data-line-pick]').trigger('click');
      $body.children('tr').last().find('[data-field="quantity"]').val('1.005').trigger('input');
      if (c.priced) {
        ['101', '10', '111'].forEach(function(value, i) {
          equal($body.children('tr').last().find('[data-field="' + ['supplyAmount', 'vatAmount', 'totalAmount'][i] + '"]').val(), value, '소수 금액');
        });
      }
      $body.children('tr').last().find('.line-select').prop('checked', true);
      $form.find('[data-lines-remove]').trigger('click');
      equal($('#removed-line-ids input').length, 0, '신규 행 삭제 ID 없음');
      $body.find('.line-select').prop('checked', true);
      $form.find('[data-lines-remove]').trigger('click');
      equal($body.children('tr').length, 0, '전체 선택 삭제');
      equal($('#removed-line-ids input').map(function() { return this.value; }).get(), c.rows ? ['700', '701'] : [], '기존 행만 삭제 ID');
      await reset();
      equal(snapshot(), initial, '행 삭제 후 다시 작성');
      checkIndexes();
      for (var repeat = 0; repeat < 3; repeat++) {
        var before = picks;
        pick('partner', linked);
        equal(picks - before, 1, '반복 선택 1회');
        await reset();
        equal(snapshot(), initial, '반복 초기화');
      }
      var cancel = function(event) { event.preventDefault(); };
      form.addEventListener('reset', cancel);
      pick('partner', linked);
      var beforeCancel = snapshot();
      await reset();
      equal(snapshot(), beforeCancel, '취소된 reset 보존');
      form.removeEventListener('reset', cancel);
      return checks;
    })().then(function() {
      parent.postMessage({ kind: 'document-form-result', checks: checks, error: null }, '*');
    }).catch(function(error) {
      parent.postMessage({ kind: 'document-form-result', checks: checks, error: error.message }, '*');
    });
  }
  function next() {
    if (index >= cases.length) {
      result.complete = true;
      document.getElementById('results').textContent += '\n완료: ' + result.passed + '개 통과 / ' + result.failed + '개 실패 / ' + result.checks + '개 확인';
      return;
    }
    var c = cases[index];
    frame = document.createElement('iframe');
    frame.title = c.type + ' 테스트';
    frame.hidden = true;
    var base = new URL('../../main/webapp/resources/js/', location.href).href;
    frame.srcdoc = '<!doctype html><meta charset="UTF-8"><body>' + fixture(c)
      + '<script>document.getElementById("line-template").innerHTML=' + JSON.stringify(row(c, 0, false)) + ';<\/script>'
      + scripts.map(function(src) { return '<script src="' + base + src + '"><\/script>'; }).join('')
      + '<script>(' + run.toString() + ')(' + JSON.stringify(c) + ');<\/script></body>';
    document.body.appendChild(frame);
    timer = setTimeout(function() { finish({ checks: 0, error: '스크립트 로딩/실행 시간 초과' }); }, 10000);
  }
  function finish(message) {
    clearTimeout(timer);
    var c = cases[index];
    var name = c.type + ' ' + (c.edit ? '수정' : '신규') + ' / 기존 ' + c.rows + '행';
    result.checks += message.checks;
    result[message.error ? 'failed' : 'passed']++;
    result.cases.push({ name: name, checks: message.checks, error: message.error });
    document.getElementById('results').textContent += '\n' + (message.error ? '실패 ' : '통과 ') + name + (message.error ? ': ' + message.error : '');
    frame.remove();
    index++;
    next();
  }
  window.addEventListener('message', function(event) {
    if (frame && event.source === frame.contentWindow && event.data.kind === 'document-form-result') finish(event.data);
  });
  next();
}());
