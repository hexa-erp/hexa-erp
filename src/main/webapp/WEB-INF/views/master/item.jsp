<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace master-workspace">
	<form class="list-search" method="get"
		action="<c:url value='/master/item'/>">
		<input type="hidden" name="pageSize" value="50"> <input
			type="hidden" name="page" value="1"> <input type="hidden"
			name="includeInactive"
			value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">
		<div class="list-title-status">
			<h1>품목 등록</h1>
			<button
				class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
				type="button" data-master-inactive-toggle
				aria-pressed="${search.includeInactive eq 'Y'}">사용중단 포함</button>
		</div>
		<div class="input-group list-keyword">
			<input type="search" name="keyword" aria-label="품목 등록 검색"
				placeholder="입력 후 [Enter]"
				value="<c:out value="${search.keyword}"/>">
			<button class="btn btn-primary" type="submit">검색</button>
		</div>
	</form>
	<div class="workspace-meta">
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">코드순 · 품목 등록 정보를 조회하고 등록합니다.</span>
	</div>
	<form id="item-selection" method="post"
		action="<c:url value='/master/item/active'/>" data-unimplemented-submit>
		<input type="hidden" name="activeFlag" value="N">
		<%-- imageUrl은 표시용 URL이며 저장 경로인 imagePath와 구분한다. --%>
		<div class="table-wrap">
			<table class="data-table workspace-table master-table item-table">
				<thead>
					<tr>
						<th class="col-select"><input type="checkbox"
							data-check-all="#item-selection input[name=ids]"
							aria-label="전체 선택"></th>
						<th class="col-code">품목 코드</th>
						<th class="col-item">품목명</th>
						<th class="col-spec">규격</th>
						<th class="col-unit text-center">단위</th>
						<th class="col-number text-right">입고단가</th>
						<th class="col-number text-right">출고단가</th>
						<th>품목구분</th>
						<th class="item-image-cell col-image text-center">이미지</th>
						<th class="note-cell">적요</th>
						<th class="col-active text-center">사용</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${itemList}">
						<tr data-record-type="item"
							data-item-id="<c:out value="${row.itemId}"/>"
							data-item-code="<c:out value="${row.itemCode}"/>"
							data-item-name="<c:out value="${row.itemName}"/>"
							data-specification="<c:out value="${row.specification}"/>"
							data-unit="<c:out value="${row.unit}"/>"
							data-item-type="<c:out value="${row.itemType}"/>"
							data-inbound-price="<c:out value="${row.inboundPrice}"/>"
							data-outbound-price="<c:out value="${row.outboundPrice}"/>"
							data-image-path="<c:out value="${row.imagePath}"/>"
							data-image-url="<c:out value="${row.imageUrl}"/>"
							data-note="<c:out value="${row.note}"/>"
							data-active-flag="<c:out value="${row.activeFlag}"/>">
							<td class="col-select"><input type="checkbox" name="ids"
								value="<c:out value="${row.itemId}"/>"
								aria-label="<c:out value="${row.itemName}"/> 선택"></td>
							<td class="col-code"><a href="#" aria-haspopup="dialog"
								data-master-edit="item"><c:out value="${row.itemCode}" /></a></td>
							<td class="col-item"><a href="#" aria-haspopup="dialog"
								data-master-edit="item"><c:out value="${row.itemName}" /></a></td>
							<td class="col-spec"><c:out value="${row.specification}" /></td>
							<td class="col-unit text-center"><c:out value="${row.unit}" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.inboundPrice}" pattern="#,##0.##" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.outboundPrice}" pattern="#,##0.##" /></td>
							<td><c:out value="${row.itemType}" /></td>
							<td class="item-image-cell col-image text-center"><c:choose>
									<c:when test="${not empty row.imageUrl}">
										<button class="btn btn-sm" type="button"
											aria-haspopup="dialog"
											data-item-image="<c:out value="${row.imageUrl}"/>"
											data-item-image-name="<c:out value="${row.itemName}"/>">
											<img src="<c:out value="${row.imageUrl}"/>"
												alt="<c:out value="${row.itemName}"/> 대표 사진" width="40"
												height="40">
										</button>
									</c:when>
									<c:otherwise>
										<span class="item-image-empty">미등록</span>
									</c:otherwise>
								</c:choose></td>
							<td class="note-cell"><c:out value="${row.note}" /></td>
							<td class="col-active text-center"><span class="badge">${row.activeFlag eq 'N' ? '중단' : '사용'}</span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty itemList}">
						<tr>
							<td class="empty-state" colspan="11">조회된 품목이 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<div class="workspace-actions">
			<button class="btn btn-primary" type="button" data-master-new="item">신규</button>
			<button class="btn" type="button" data-stock-open>간편재고조정</button>
			<button class="btn" type="button" data-master-active="N"
				data-selection-form="item-selection">사용중단</button>
			<button class="btn" type="button" data-master-active="Y"
				data-selection-form="item-selection">재사용</button>

		</div>
	</form>
</section>
<dialog class="modal master-modal" id="item-form-modal"
	aria-labelledby="item-form-title">
<div class="modal-header">
	<h2 id="item-form-title" data-master-title="item">품목 등록</h2>
	<button class="btn btn-sm" type="button" data-modal-close
		aria-label="닫기">×</button>
</div>
<form id="item-form" action="<c:url value='/master/item/save'/>"
	method="post" enctype="multipart/form-data" data-unimplemented-submit>
	<div class="modal-body">
		<div class="form-grid editor-fields">
			<input type="hidden" name="itemId"><input type="hidden"
				name="activeFlag"> <label class="field"><span>품목
					코드</span><input type="text" name="itemCode" maxlength="30"
				value="<c:out value="${newItemCode}"/>" placeholder="코드 입력"
				title="신규 등록 시에만 코드를 입력하거나 수정할 수 있습니다."></label> <label
				class="field"><span>품목명</span><input type="text"
				name="itemName" maxlength="100" required></label> <label
				class="field"><span>규격</span><input type="text"
				name="specification" maxlength="100"></label> <label class="field"><span>단위</span><input
				type="text" name="unit" maxlength="20"></label>
			<fieldset class="field field-wide">
				<legend>품목구분</legend>
				<div class="choice-options">
					<label><input type="radio" name="itemType" value="원재료">
						원재료</label><label><input type="radio" name="itemType" value="부재료">
						부재료</label><label><input type="radio" name="itemType" value="제품">
						제품</label><label><input type="radio" name="itemType" value="반제품">
						반제품</label><label><input type="radio" name="itemType" value="상품"
						checked> 상품</label><label><input type="radio"
						name="itemType" value="무형상품"> 무형상품</label>
				</div>
			</fieldset>
			<label class="field"><span>입고단가</span><input type="number"
				name="inboundPrice" required step="0.01" value="0"></label> <label
				class="field"><span>출고단가</span><input type="number"
				name="outboundPrice" required step="0.01" value="0"></label>
			<div class="field field-wide stock-entry-field">
				<span>재고수량</span>
				<div>
					<button class="btn" type="button" data-stock-open="current">재고수량
						입력</button>
					<span class="muted">저장된 품목의 창고별 수량을 입력합니다.</span>
				</div>
			</div>
			<input type="hidden" name="imagePath"><input type="hidden"
				name="removeImage"> <label
				class="field field-wide photo-picker-field"><span>대표
					사진 1장</span><input type="file" name="imageFile" accept="image/*"
				data-item-file><span class="muted">사진은 화면에서 미리 확인할 수
					있습니다.</span></label>
			<div class="field field-wide photo-image-field">
				<img id="item-photo-image" alt="품목 대표 사진 미리보기" width="180"
					height="140" hidden>
				<button class="btn btn-sm" type="button" data-item-remove-image>사진
					제거</button>
			</div>
			<label class="field field-wide"><span>적요</span> <textarea
					name="note" rows="3" maxlength="500"></textarea></label>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" type="submit">저장</button>
		<button class="btn" type="reset">다시 작성</button>
		<button class="btn" type="button" data-modal-close>닫기</button>
	</div>
</form>
</dialog>
<dialog class="modal stock-modal" id="stock-modal"
	aria-labelledby="stock-title">
<div class="modal-header">
	<h2 id="stock-title">간편재고조정</h2>
	<button class="btn btn-sm" type="button" data-modal-close
		aria-label="닫기">×</button>
</div>
<form id="stock-form" action="<c:url value='/master/item/stock'/>"
	method="post" data-unimplemented-submit>
	<div class="modal-body">
		<div class="form-grid editor-fields">
			<div class="field field-wide">
				<span>품목</span>
				<div class="input-group">
					<input type="hidden" name="itemId"><input type="text"
						name="itemName" readonly aria-label="재고조정 품목">
					<button class="btn" type="button" data-stock-pick-item>품목
						선택</button>
				</div>
			</div>
			<label class="field"><span>창고</span><select
				name="warehouseId" required><option value="">선택</option>
					<c:forEach var="warehouse" items="${warehouseOptions}">
						<option value="<c:out value="${warehouse.warehouseId}"/>"><c:out
								value="${warehouse.warehouseCode}" /> ·
							<c:out value="${warehouse.warehouseName}" /></option>
					</c:forEach></select></label>
			<div class="field">
				<span>현재고</span>
				<output id="stock-current">—</output>
			</div>
			<label class="field"><span>입력 수량</span><input type="number"
				name="quantity" step="0.001" required></label>
		</div>
		<p class="muted">현재고는 선택한 품목과 창고의 조회값입니다. 실제 재고 반영은 저장 기능 연결 후
			가능합니다.</p>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" type="submit">저장</button>
		<button class="btn" type="button" data-modal-close>닫기</button>
	</div>
</form>
</dialog>
<div hidden id="stock-data">
	<c:forEach var="stock" items="${stockList}">
		<span data-stock-item-id="<c:out value="${stock.itemId}"/>"
			data-stock-warehouse-id="<c:out value="${stock.warehouseId}"/>"
			data-stock-quantity="<c:out value="${stock.quantity}"/>"></span>
	</c:forEach>
</div>
<dialog class="modal image-modal" id="item-image-modal"
	aria-labelledby="item-image-title">
<div class="modal-header">
	<h2 id="item-image-title">품목 사진</h2>
	<button class="btn btn-sm" type="button" data-modal-close
		aria-label="닫기">×</button>
</div>
<div class="modal-body">
	<img id="item-image-full" alt="품목 대표 사진">
</div>
</dialog>
<c:set var="pageScript" value="master.js" />
<%@ include file="../common/footer.jsp"%>
