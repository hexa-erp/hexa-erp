package hexa.erp.partner.service;

import java.util.List;

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;

public interface PartnerService {

    List<PartnerVO> getList(PartnerCriteria criteria);

    int getTotal(PartnerCriteria criteria);

    void register(PartnerVO partner);

    boolean modify(PartnerVO partner);

    int changeActive(List<Long> ids, String activeFlag);
}
