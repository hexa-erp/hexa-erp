/* document-form.test.htmlをChromeで開いて実行する。追加ライブラリなしで実際のUIスクリプトを検証する。 */
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
      + input(type + 'Name', '初期値 ' + type, 'readonly')
      + '<button type="button" data-pick="' + type + '">選択</button></div>';
  }
  function row(c, i, saved) {
    var values = {};
    values[c.line] = saved ? String(700 + i) : '';
    if (c.source) values[c.source] = saved ? String(900 + i) : '';
    Object.assign(values, { itemId: saved ? String(500 + i) : '', itemCode: saved ? '00001' : '',
      itemName: saved ? '既存品目' : '', specification: '', unit: 'EA', quantity: saved ? '1.005' : '', note: '' });
    if (c.priced) Object.assign(values, { unitPrice: saved ? '100' : '', supplyAmount: '', vatAmount: '', totalAmount: '' });
    return '<tr data-line-row><td><input type="checkbox" class="line-select"><span data-line-number></span>'
      + Object.keys(values).map(function(key) {
        return input('lines[' + i + '].' + key, values[key], 'data-field="' + key + '"');
      }).join('') + '<button type="button" data-line-pick>品目選択</button>'
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
      + '" data-priced="' + c.priced + '"><button type="button" data-line-add>行追加</button>'
      + '<button type="button" data-lines-remove>選択削除</button><table><tbody id="line-body">' + rows + '</tbody></table>'
      + '<template id="line-template"><table></table></template><div id="removed-line-ids"></div></section>'
      + '<span id="line-total-quantity"></span><span id="line-total-supply"></span><span id="line-total-vat"></span>'
      + '<span id="line-total-amount"></span><button type="reset">再入力</button></form>'
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
          equal(this.name, 'lines[' + i + '].' + $(this).attr('data-field'), '行インデックス');
        });
      });
    }
    Hexa.pick = function(type, callback) { picks++; callback(nextRow); };
    Hexa.notice = function(message) { throw new Error(message); };
    (async function() {
      var expectedRows = c.rows || (c.edit ? 0 : 3);
      equal($body.children('tr').length, expectedRows, '初期行数');
      equal($form.attr('method'), 'post', '送信方法');
      equal($form.attr('action'), '/' + c.type + '/save', '送信先');
      var initial = snapshot(), counts = {};
      $form.find('[data-code-selector] input').each(function() {
        var key = this.name || $(this).attr('data-reference-code');
        counts[key] = 0;
        this.addEventListener('change', function() { counts[key]++; });
      });
      var linked = { partnerId: 42, partnerCode: '00002', partnerName: '新しい取引先',
        assigneeId: 102, assigneeCode: '00007', assigneeName: '担当者' };
      pick('partner', linked);
      equal(selected('partner'), ['42', '00002', '新しい取引先'], '取引先選択');
      equal(selected('assignee'), ['102', '00007', '担当者'], '担当者の自動反映');
      ['partnerId', 'partnerCode', 'partnerName', 'assigneeId', 'assigneeCode', 'assigneeName'].forEach(function(key) {
        equal(counts[key], 1, key + ' change 1回');
      });
      var manual = { assigneeId: 103, assigneeCode: '00009', assigneeName: '手動選択担当者' };
      pick('assignee', manual);
      equal(selected('assignee'), ['103', '00009', '手動選択担当者'], '手動選択の保持');
      pick('partner', { partnerId: 43, partnerCode: '00003', partnerName: '未指定', assigneeName: '名前のみ' });
      equal(selected('assignee'), ['', '', ''], '担当者なしの取引先');
      pick('partner', linked);
      pick('partner', { partnerId: 44, partnerCode: '00004', partnerName: '不完全', assigneeId: 102 });
      equal(selected('assignee'), ['', '', ''], '不完全な担当者情報を組み合わせない');
      await reset();
      equal(snapshot(), initial, 'ヘッダー・選択値・初期行をリセット');
      pick('partner', linked, document.getElementById('other-form'));
      equal(selected('assignee', document.getElementById('other-form')), ['901', '00001', '初期assignee'], '入力フォームの外には伝播しない');
      $form.find('[data-code-selector="partner"] input').val('');
      HexaReference.applySource(form, linked);
      equal(selected('assignee'), ['201', '00001', '初期assignee'], '元伝票の担当者を保持');
      await reset();
      $form.find('[data-line-add]').trigger('click');
      equal($body.children('tr').length, expectedRows + 1, '行追加1回');
      nextRow = { itemId: 501, itemCode: '00001', itemName: '品目', unit: 'EA', specification: '規格', outboundPrice: 100 };
      $body.children('tr').last().find('[data-line-pick]').trigger('click');
      $body.children('tr').last().find('[data-field="quantity"]').val('1.005').trigger('input');
      if (c.priced) {
        ['101', '10', '111'].forEach(function(value, i) {
          equal($body.children('tr').last().find('[data-field="' + ['supplyAmount', 'vatAmount', 'totalAmount'][i] + '"]').val(), value, '小数の金額');
        });
      }
      $body.children('tr').last().find('.line-select').prop('checked', true);
      $form.find('[data-lines-remove]').trigger('click');
      equal($('#removed-line-ids input').length, 0, '新規行の削除IDはなし');
      $body.find('.line-select').prop('checked', true);
      $form.find('[data-lines-remove]').trigger('click');
      equal($body.children('tr').length, 0, '全行を選択して削除');
      equal($('#removed-line-ids input').map(function() { return this.value; }).get(), c.rows ? ['700', '701'] : [], '既存行だけを削除IDに追加');
      await reset();
      equal(snapshot(), initial, '行削除後の再入力');
      checkIndexes();
      for (var repeat = 0; repeat < 3; repeat++) {
        var before = picks;
        pick('partner', linked);
        equal(picks - before, 1, '繰り返し選択しても1回');
        await reset();
        equal(snapshot(), initial, '繰り返し初期化');
      }
      var cancel = function(event) { event.preventDefault(); };
      form.addEventListener('reset', cancel);
      pick('partner', linked);
      var beforeCancel = snapshot();
      await reset();
      equal(snapshot(), beforeCancel, 'キャンセルされたresetを保持');
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
      document.getElementById('results').textContent += '\n完了: ' + result.passed + '件成功 / ' + result.failed + '件失敗 / ' + result.checks + '件確認';
      return;
    }
    var c = cases[index];
    frame = document.createElement('iframe');
    frame.title = c.type + ' テスト';
    frame.hidden = true;
    var base = new URL('../../main/webapp/resources/js/', location.href).href;
    frame.srcdoc = '<!doctype html><meta charset="UTF-8"><body>' + fixture(c)
      + '<script>document.getElementById("line-template").innerHTML=' + JSON.stringify(row(c, 0, false)) + ';<\/script>'
      + scripts.map(function(src) { return '<script src="' + base + src + '"><\/script>'; }).join('')
      + '<script>(' + run.toString() + ')(' + JSON.stringify(c) + ');<\/script></body>';
    document.body.appendChild(frame);
    timer = setTimeout(function() { finish({ checks: 0, error: 'スクリプトの読み込みまたは実行がタイムアウト' }); }, 10000);
  }
  function finish(message) {
    clearTimeout(timer);
    var c = cases[index];
    var name = c.type + ' ' + (c.edit ? '更新' : '新規') + ' / 既存 ' + c.rows + '行';
    result.checks += message.checks;
    result[message.error ? 'failed' : 'passed']++;
    result.cases.push({ name: name, checks: message.checks, error: message.error });
    document.getElementById('results').textContent += '\n' + (message.error ? '失敗 ' : '成功 ') + name + (message.error ? ': ' + message.error : '');
    frame.remove();
    index++;
    next();
  }
  window.addEventListener('message', function(event) {
    if (frame && event.source === frame.contentWindow && event.data.kind === 'document-form-result') finish(event.data);
  });
  next();
}());
