package hexa.erp.common.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.common.domain.FilterSelectionVO;
import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;

@Service
public class FilterSelectionServiceImpl implements FilterSelectionService {
	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;
	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;
	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;

	@Override
	public Map<String, List<FilterSelectionVO>> getSelections(Map<String, List<String>> filterIds) {
		Map<String, List<FilterSelectionVO>> selections = new LinkedHashMap<>();
		for (String kind : new String[] { "warehouse", "partner", "item", "assignee" }) {
			List<String> ids = filterIds.get(kind + "Ids");
			if (ids == null) {
				continue;
			}
			List<FilterSelectionVO> selected = new ArrayList<>();
			for (String id : ids) {
				selected.add(getSelection(kind, id));
			}
			selections.put(kind, selected);
		}
		return selections;
	}

	private FilterSelectionVO getSelection(String kind, String id) {
		FilterSelectionVO selection = new FilterSelectionVO();
		selection.setId(id);
		selection.setCode("");
		selection.setName("찾을 수 없는 항목 (" + id + ")");
		Long key;
		try {
			key = Long.valueOf(id);
		} catch (NumberFormatException e) {
			return selection;
		}
		switch (kind) {
		case "partner":
			PartnerLookupVO partner = partnerLookupService.get(key);
			if (partner != null) {
				selection.setCode(partner.getPartnerCode());
				selection.setName(partner.getPartnerName());
			}
			break;
		case "warehouse":
			WarehouseLookupVO warehouse = warehouseLookupService.get(key);
			if (warehouse != null) {
				selection.setCode(warehouse.getWarehouseCode());
				selection.setName(warehouse.getWarehouseName());
			}
			break;
		case "item":
			ItemLookupVO item = itemLookupService.get(key);
			if (item != null) {
				selection.setCode(item.getItemCode());
				selection.setName(item.getItemName());
			}
			break;
		case "assignee":
			AssigneeLookupVO assignee = assigneeLookupService.get(key);
			if (assignee != null) {
				selection.setCode(assignee.getAssigneeCode());
				selection.setName(assignee.getAssigneeName());
			}
			break;
		default:
			break;
		}
		return selection;
	}
}
