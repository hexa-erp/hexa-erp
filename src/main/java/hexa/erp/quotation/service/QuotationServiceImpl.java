package hexa.erp.quotation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.mapper.QuotationMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class QuotationServiceImpl implements QuotationService {

	@Setter(onMethod_ = @Autowired)
	private QuotationMapper mapper;
	
	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;
	
	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;
	
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;
	
	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;

	@Override
	public QuotationVO get(Long quotationId) {
		log.info("견적서 조회");
		
		if (quotationId == null){
			return null;
		}
		
		QuotationVO quotation = mapper.read(quotationId);
		
		if (quotation != null)
			quotation.setLines(mapper.getLines(quotationId));
	
		return quotation;
		
	}

	@Override
	public List<QuotationVO> getList(QuotationCriteria criteria) {
		log.info("견적서 목록 조회");
		
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(QuotationCriteria criteria) {
		log.info("견적서 전체 수 조회");
		
		return mapper.getTotalCount(criteria);
	}
	
	@Transactional
	@Override
	public Long save(QuotationVO quotation) {
		
		if (quotation == null || quotation.getBusinessDate() == null || quotation.getBusinessDate().trim().isEmpty()) {
			throw new IllegalArgumentException("견적서를 올바르게 입력해 주세요.");
		}
		
		try {
				quotation.setBusinessDate(
					 LocalDate.parse(quotation.getBusinessDate().trim()).toString());
					
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("견적 일자를 올바르게 입력해 주세요. ");
		}
		
		List<QuotationLineVO> lines = new ArrayList<>();
		
		List<Long> removedLineIds = cleanIds(quotation.getRemovedLineIds());
		Set<Long> lineIds = new HashSet<>();

		//중복, 삭제된 품목 행 검사
		if (quotation.getLines() != null) {

		    for (QuotationLineVO line : quotation.getLines()) {

		        if (line == null || line.getItemId() == null) {
		            continue;
		        }

		        if (line.getQuotationLineId() != null) {
		        	if (!lineIds.add(line.getQuotationLineId()) || removedLineIds.contains(line.getQuotationLineId())) {
		        		throw new IllegalArgumentException("중복되거나 삭제 표시된 품목 행을 확인해 주세요.");
		        	}
		        }
		        
		        prepareLine(line);
		        lines.add(line);
		    }
		}

		if (lines.isEmpty()) {
		    throw new IllegalArgumentException("견적 품목을 한 개 이상 입력해 주세요.");
		}

		quotation.setLines(lines);
		
		//신규 견적서 등록
		if (quotation.getQuotationId() == null) {
			
			fillHeaderNames(quotation, null);
			
			mapper.insertSelectKey(quotation);
			
			for(QuotationLineVO line : lines) {
				
				line.setQuotationId(quotation.getQuotationId());
				
				mapper.insertLineSelectKey(line);
			}	
		}
				
		//기존 견적서 수정
		else {	
			
			//기존 견적서 불러오기
			QuotationVO existing = mapper.read(quotation.getQuotationId());
			
			if (existing == null){
				throw new IllegalArgumentException("수정할 견적서를 찾을 수 없습니다.");
			}
			
			quotation.setQuotationNo(existing.getQuotationNo());
			quotation.setProgressStatus(existing.getProgressStatus());
			
			fillHeaderNames(quotation, existing);
			
			//견적서 기본 정보 수정
			int result = mapper.update(quotation);
			
			if (result != 1) {
				throw new IllegalArgumentException("수정할 견적서를 찾을 수 없습니다.");
			}
			
			//기존 품목 삭제
			if (!cleanIds(quotation.getRemovedLineIds()).isEmpty()) {
				mapper.removeSelectedLines(quotation.getQuotationId(), quotation.getRemovedLineIds());
			}
			
			for(QuotationLineVO line : lines) {
				
				line.setQuotationId(quotation.getQuotationId());
				
				//새로운 품목 등록
				if (line.getQuotationLineId() == null) {
					mapper.insertLineSelectKey(line);
				}
				
				//기존 품목 수정
				else {
					
					int updateResult = mapper.updateLine(line);
					
					if (updateResult != 1) {
						throw new IllegalArgumentException("견적서에 속하지 않거나 삭제된 품목 행입니다.");
					}
				}
				
			}

		}
		
		log.info("견적서 저장: " + quotation.getQuotationId());
		return quotation.getQuotationId();
	}
	
	@Transactional
	@Override
	public int remove(List<Long> selectedIds) {
		
		List<Long> ids = cleanIds(selectedIds);
		
		if (ids.isEmpty()) {
			return 0;
		}
		
		int result = mapper.remove(ids);
		mapper.removeLines(ids);
		
		log.info("견적서 삭제" + result + " 건");
		return result;
		
	}

	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {
		
		List<Long> ids = cleanIds(selectedIds);
		
		if (ids.isEmpty()) {
			return 0;
		}
		
		if (!"UNCONFIRMED".equals(progressStatus) && !"IN_PROGRESS".equals(progressStatus) && !"COMPLETED".equals(progressStatus)) {
			throw new IllegalArgumentException("진행상태는 미확인, 진행중, 완료 중에 선택해 주세요.");
		}
		
		log.info("견적서 진행 상태 변경: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}
	
	//품목 행 처리
	private void prepareLine(QuotationLineVO line) {
		
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());
			
			if (item == null) {
				throw new IllegalArgumentException("선택한 품목을 찾을 수 없습니다.");
			}
			
			line.setItemName(item.getItemName());
		}
		
		BigDecimal quantity = line.getQuantity() == null? BigDecimal.ZERO : line.getQuantity();
		BigDecimal unitPrice = line.getUnitPrice() == null? BigDecimal.ZERO : line.getUnitPrice();
		
		quantity = quantity.setScale(3, RoundingMode.HALF_UP);
	    unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
	    
	    //공급가액 계산
		BigDecimal supplyAmount = quantity.multiply(unitPrice);
		supplyAmount = supplyAmount.setScale(0, RoundingMode.HALF_UP);
		
		//부가세 계산
		BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1"));
		vatAmount = vatAmount.setScale(0, RoundingMode.HALF_UP);
		
		//합계 계산
		BigDecimal totalAmount = supplyAmount.add(vatAmount);
		
		line.setQuantity(quantity);
		line.setUnitPrice(unitPrice);
		line.setSupplyAmount(supplyAmount);
		line.setVatAmount(vatAmount);
		line.setTotalAmount(totalAmount);
	}
	
	//견적서 기본정보에 들어가는 이름 보완
	private void fillHeaderNames(QuotationVO quotation, QuotationVO existing) {
		
		if (quotation.getPartnerId() == null){
			quotation.setPartnerName(null);
		} else if (isBlank(quotation.getPartnerName())) {
			if (existing != null && quotation.getPartnerId().equals(existing.getPartnerId()) && !isBlank(existing.getPartnerName())) {
				quotation.setPartnerName(existing.getPartnerName());
			}
			else {
				PartnerLookupVO partner = partnerLookupService.get(quotation.getPartnerId());
				quotation.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}
		
		if (quotation.getWarehouseId() == null){
			quotation.setWarehouseName(null);
		}else if (isBlank(quotation.getWarehouseName())) {
			if (existing != null && quotation.getWarehouseId().equals(existing.getWarehouseId()) && !isBlank(existing.getWarehouseName())) {
				quotation.setWarehouseName(existing.getWarehouseName());
			}
			else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(quotation.getWarehouseId());
				quotation.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}
		
		if (quotation.getAssigneeId() == null) {
			quotation.setAssigneeName(null);
		}else if (isBlank(quotation.getAssigneeName())) {
			if (existing != null && quotation.getAssigneeId().equals(existing.getAssigneeId()) && !isBlank(existing.getAssigneeName())) {
				quotation.setAssigneeName(existing.getAssigneeName());
			} 
			else {
				AssigneeLookupVO assignee = assigneeLookupService.get(quotation.getAssigneeId());
				quotation.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
			}
		}
	}
	
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
	
	private List<Long> cleanIds(List<Long> values){
		Set<Long> ids = new LinkedHashSet<>();
		
		if (values != null) {
			for (Long value : values) {
				if (value != null) {
					ids.add(value);
				}
			}
		}
		
		return new ArrayList<>(ids);
	}
}
