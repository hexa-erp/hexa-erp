package hexa.erp.shipinstruction.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupVO;
import hexa.erp.shipinstruction.mapper.ShipInstructionLookupMapper;
import lombok.Setter;

@Service
public class ShipInstructionLookupServiceImpl implements ShipInstructionLookupService {
	@Setter(onMethod_ = @Autowired)
	private ShipInstructionLookupMapper mapper;

	// 出荷指示書読込ポップアップに、一覧と品目ごとの残数量を表示する。読込のみではDBを変更しない。
	@Override
	public List<ShipInstructionLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		List<ShipInstructionLookupVO> documents = mapper.getListWithPaging(criteria, progressStatus);
		for (ShipInstructionLookupVO document : documents) {
			document.setLines(mapper.getLines(document.getDocumentId()));
		}
		return documents;
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return mapper.getTotalCount(criteria, progressStatus);
	}
}
