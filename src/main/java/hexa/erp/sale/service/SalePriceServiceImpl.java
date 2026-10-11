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

/** 단가 변경 시 금액은 화면 값을 믿지 않고 DB에 저장된 수량으로 다시 계산한다. */
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
				// 체크하지 않은 행은 건너뛴다.
				if (change == null || !change.isSelected()) {
					continue;
				}
				if (change.getSaleId() == null || change.getSaleLineId() == null) {
					throw new IllegalArgumentException("판매 상세행 정보를 확인할 수 없습니다. 다시 조회한 뒤 선택해 주세요.");
				}
				// 같은 품목 행이 두 번 넘어오면 처음 값만 반영한다.
				if (!changedLineIds.add(change.getSaleLineId())) {
					continue;
				}
				if (change.getUnitPrice() == null) {
					throw new IllegalArgumentException("선택한 판매 상세행의 단가를 입력해 주세요.");
				}
				SalePriceVO row = mapper.read(change.getSaleId(), change.getSaleLineId());
				if (row == null) {
					throw new IllegalArgumentException("선택한 판매 또는 상세행이 삭제되었거나 변경되었습니다. 다시 조회해 주세요.");
				}
				// 수량은 제출값이 아니라 현재 판매 상세행에 저장된 값을 사용한다.
				// 단가는 소수 둘째 자리, 금액은 원 단위로 반올림한다. (DB 컬럼 자릿수와 같음)
				// 공급가액 = 수량 × 단가, 부가세 = 공급가액 × 10%, 합계 = 공급가액 + 부가세
				BigDecimal unitPrice = change.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
				BigDecimal supplyAmount = row.getQuantity().multiply(unitPrice).setScale(0, RoundingMode.HALF_UP);
				BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);
				row.setUnitPrice(unitPrice);
				row.setSupplyAmount(supplyAmount);
				row.setVatAmount(vatAmount);
				row.setTotalAmount(supplyAmount.add(vatAmount));
				// 조회 이후 삭제된 경우 0건이 바뀌므로 예외를 던져 앞에서 바꾼 행까지 모두 롤백한다.
				if (mapper.updatePrice(row) != 1 || mapper.updateSaleTimestamp(row.getSaleId()) != 1) {
					throw new IllegalArgumentException("단가를 변경할 판매 상세행을 찾을 수 없습니다. 다시 조회해 주세요.");
				}
				count++;
			}
		}
		if (count == 0) {
			throw new IllegalArgumentException("단가를 변경할 판매 상세행을 선택해 주세요.");
		}
		log.info("판매 상세행 단가 변경: " + count + "건");
		return count;
	}
}
