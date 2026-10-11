package hexa.erp.partner.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;

public interface PartnerMapper {

    List<PartnerVO> getListWithPaging(PartnerCriteria criteria);

    int getTotalCount(PartnerCriteria criteria);

    void insertSelectKey(PartnerVO partner);

    int update(PartnerVO partner);

    int changeActive(
        @Param("ids") List<Long> ids,
        @Param("activeFlag") String activeFlag
    );
}
