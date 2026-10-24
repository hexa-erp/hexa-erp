package hexa.erp.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.sale.domain.SalePriceChangeVO;
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;
import hexa.erp.sale.mapper.SalePriceMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/** 単価変更時の金額は画面の値を使用せず、DBに保存された数量から再計算する。 */
@Service
@Log4j
public class SalePriceServiceImpl implements SalePriceService {
	@Setter(onMethod_ = @Autowired)
	private SalePriceMapper mapper;

	@Override
	public List<SalePriceVO> getList(SalePriceCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(SalePriceCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	@Override
	@Transactional
	public int savePrices(List<SalePriceChangeVO> changes) {
		int count = 0;
		Set<Long> changedLineIds = new HashSet<>();
		if (changes != null) {
			for (SalePriceChangeVO change : changes) {
				// 選択されていない行は処理を省略する。
				if (change == null || !change.isSelected()) {
					continue;
				}
				if (change.getSaleId() == null || change.getSaleLineId() == null) {
					throw new IllegalArgumentException("売上明細行の情報を確認できません。再度検索してから選択してください。");
				}
				// 同じ品目行が複数回送信された場合は、最初の値のみ反映する。
				if (!changedLineIds.add(change.getSaleLineId())) {
					continue;
				}
				if (change.getUnitPrice() == null) {
					throw new IllegalArgumentException("選択した売上明細行の単価を入力してください。");
				}
				SalePriceVO row = mapper.read(change.getSaleId(), change.getSaleLineId());
				if (row == null) {
					throw new IllegalArgumentException("選択した売上または明細行は削除または変更されています。再度検索してください。");
				}
				// 数量は送信値ではなく、現在の売上明細行に保存されている値を使用する。
				// 単価は小数第2位、金額は1円単位に四捨五入する。DB列の桁数と一致する。
				// 税抜金額 = 数量 × 単価、消費税 = 税抜金額 × 10%、合計 = 税抜金額 + 消費税
				BigDecimal unitPrice = change.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
				BigDecimal supplyAmount = row.getQuantity().multiply(unitPrice).setScale(0, RoundingMode.HALF_UP);
				BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);
				row.setUnitPrice(unitPrice);
				row.setSupplyAmount(supplyAmount);
				row.setVatAmount(vatAmount);
				row.setTotalAmount(supplyAmount.add(vatAmount));
				// 取得後に削除されると更新件数が0になるため、例外を送出して先に更新した行もすべてロールバックする。
				if (mapper.updatePrice(row) != 1 || mapper.updateSaleTimestamp(row.getSaleId()) != 1) {
					throw new IllegalArgumentException("単価を変更する売上明細行が見つかりません。再度検索してください。");
				}
				count++;
			}
		}
		if (count == 0) {
			throw new IllegalArgumentException("単価を変更する売上明細行を選択してください。");
		}
		log.info("売上明細行の単価変更: " + count + "件");
		return count;
	}
}
