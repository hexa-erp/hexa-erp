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
        log.info("거래처 등록 처리");
        mapper.insertSelectKey(partner);
    }

    @Override
    public boolean modify(PartnerVO partner) {
        log.info("거래처 수정: " + partner.getPartnerId());
        return mapper.update(partner) == 1;
    }

    @Override
    public int changeActive(List<Long> ids, String activeFlag) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
            throw new IllegalArgumentException(
                "사용 여부는 Y 또는 N이어야 합니다."
            );
        }

        log.info("거래처 사용 여부 변경: " + activeFlag);
        return mapper.changeActive(ids, activeFlag);
    }
}
