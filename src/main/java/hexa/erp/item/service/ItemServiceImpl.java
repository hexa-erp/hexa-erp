package hexa.erp.item.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    // 품목 Mapper 연결
    private final ItemMapper mapper;

    // 품목 목록 조회
    @Override
    public List<ItemVO> getList(ItemCriteria criteria) {
        return mapper.getListWithPaging(criteria);
    }

    // 검색 조건 전체 건수 조회
    @Override
    public int getTotal(ItemCriteria criteria) {
        return mapper.getTotalCount(criteria);
    }

    // 품목 한 건 조회
    @Override
    public ItemVO get(Long itemId) {
        return mapper.read(itemId);
    }

    // 신규 품목 등록
    @Override
    public void register(ItemVO item) {
        log.info("품목 등록 처리");
        mapper.insertSelectKey(item);
    }

    // 기존 품목 정보 수정
    @Override
    public boolean modify(ItemVO item) {
        log.info("품목 수정: " + item.getItemId());
        return mapper.update(item) == 1;
    }

    // 품목 사용 여부 변경
    @Override
    public int changeActive(
            List<Long> ids,
            String activeFlag) {

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
            throw new IllegalArgumentException(
                "사용 여부는 Y 또는 N이어야 합니다."
            );
        }

        log.info("품목 사용 여부 변경: " + activeFlag);
        return mapper.changeActive(ids, activeFlag);
    }
}
