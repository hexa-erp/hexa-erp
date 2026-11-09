package hexa.erp.partner.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.mapper.PartnerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

	private final PartnerMapper mapper;

	@Override
	public List<PartnerVO> getList(PartnerCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(PartnerCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	@Override
	public void register(PartnerVO partner) {
		log.info("取引先の登録処理");
		mapper.insertSelectKey(partner);
	}

	@Override
	public boolean modify(PartnerVO partner) {
		log.info("取引先の更新: " + partner.getPartnerId());
		return mapper.update(partner) == 1;
	}

	@Override
	public int changeActive(List<Long> ids, String activeFlag) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}

		if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
			throw new IllegalArgumentException("利用状態にはYまたはNを指定してください。");
		}

		log.info("取引先の利用状態変更: " + activeFlag);
		return mapper.changeActive(ids, activeFlag);
	}
}
