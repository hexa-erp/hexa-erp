package hexa.erp.common.service;

import java.util.List;
import java.util.Map;

import hexa.erp.common.domain.FilterSelectionVO;

public interface FilterSelectionService {
	Map<String, List<FilterSelectionVO>> getSelections(Map<String, List<String>> filterIds);
}
