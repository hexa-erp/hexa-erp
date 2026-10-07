/* Run from the project root: node src/test/js/decimal.test.js
 * This is an optional, dependency-free UI regression test; Maven does not run it.
 */
'use strict';
var assert = require('assert');
var decimal = require('../../main/webapp/resources/js/decimal.js');
var cases = [
		[ '1.005', '100', '101', '10', '111' ],
		[ '-1.005', '100', '-101', '-10', '-111' ],
		[ '0.29', '50', '15', '2', '17' ],
		[ '0.58', '25', '15', '2', '17' ],
		[ '2.675', '100', '268', '27', '295' ],
		[ '1.005', '99.99', '100', '10', '110' ],
		[ '0.005', '100', '1', '0', '1' ],
		[ '-0.005', '100', '-1', '0', '-1' ],
		[ '1', '5', '5', '1', '6' ],
		[ '1', '-5', '-5', '-1', '-6' ],
		[ '4', '10000', '40000', '4000', '44000' ],
		[ '0', '100', '0', '0', '0' ],
		[ '-0', '100', '0', '0', '0' ],
		[ '1e-3', '500', '1', '0', '1' ],
		[ '.5', '1.', '1', '0', '1' ],
		[ '999999999999999.999', '1', '1000000000000000', '100000000000000',
				'1100000000000000' ],
		[ '1', '9007199254740993', '9007199254740993', '900719925474099',
				'9907919180215092' ],
		[ '999999999999999.999', '9999999999999999.99',
				'9999999999999999980000000000000',
				'999999999999999998000000000000',
				'10999999999999999978000000000000' ] ];
cases.forEach(function(c) {
	assert.deepStrictEqual(decimal.lineAmounts(c[0], c[1]), {
		supplyAmount : c[2],
		vatAmount : c[3],
		totalAmount : c[4]
	}, c[0] + ' * ' + c[1]);
});
assert.strictEqual(decimal.adjustPercent('30000', '10'), '33000');
assert.strictEqual(decimal.adjustPercent('30000', '-10'), '27000');
assert.strictEqual(decimal.adjustPercent('19999', '10'), '21998.9');
assert.strictEqual(decimal.adjustPercent('0.05', '10'), '0.06');
assert.strictEqual(decimal.adjustPercent('1000', '2.5'), '1025');
assert.strictEqual(decimal.adjustPercent('1000', '-100'), '0');
assert.strictEqual(decimal.adjustPercent('', '10'), null);
assert.strictEqual(decimal.add('0.1', '0.2'), '0.3');
assert.strictEqual(decimal.add('1.005', '-1.005'), '0');
assert.strictEqual(decimal.add('9007199254740993', '1'), '9007199254740994');
assert.strictEqual(decimal.add('1e2', '-.5'), '99.5');
assert.strictEqual(decimal.format('9007199254740993'), '9,007,199,254,740,993');
assert.strictEqual(decimal.format('-1234567.890'), '-1,234,567.89');
assert.strictEqual(decimal.format('-0.000'), '0');
assert.strictEqual(decimal.format(null), '-');
[ '', ' ', 'NaN', 'Infinity', '1,000', 'wrong', '1e10000', null, undefined ]
		.forEach(function(value) {
			assert.strictEqual(decimal.lineAmounts(value, '100'), null);
			assert.strictEqual(decimal.lineAmounts('100', value), null);
			assert.strictEqual(decimal.add('1', value), null);
		});
console.log('PASS: ' + cases.length
		+ ' amount cases, exact totals/formatting, invalid input handling.');
