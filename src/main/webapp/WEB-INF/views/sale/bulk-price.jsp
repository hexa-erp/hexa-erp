<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- changes[i] 중 선택한 행만 저장 대상이다. 단가의 쉼표는 표시용이며 원문 값을 제출한다. --%>
<%-- 담당자는 검색조건이 아니다. 반복 ID는 결과 검색·페이지 이동에서도 유지한다. --%>
<c:set var="pageTitle" value="판매 단가 일괄 변경" />
<c:set var="activeMenu" value="sale" />
<c:set var="workspacePage" value="true" />
<c:set var="multiFilterPage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace bulk-workspace">
	<c:set var="reportResults" value="${param.view eq 'results'}" />
	<c:choose>
		<c:when test="${not reportResults}">
			<div class="workspace-heading">
				<h1>판매 단가 일괄 변경</h1>
				<p class="workspace-hint">같은 품목이라도 판매 상세행마다 단가를 따로 변경합니다.</p>
			</div>

			<form class="report-search" method="get"
				action="${ctx}/sale/bulk-price">
				<input type="hidden" name="view" value="results" /> <input
					type="hidden" name="pageSize" value="25" />
				<div class="form-grid report-fields">
					<label class="field"><span>조회기간</span><select
						name="datePreset" data-date-preset><option value="custom"
								${search.datePreset eq 'custom' ? 'selected' : ''}>직접
								입력</option>
							<option value="today"
								${search.datePreset eq 'today' ? 'selected' : ''}>금일</option>
							<option value="yesterday"
								${search.datePreset eq 'yesterday' ? 'selected' : ''}>전일</option>
							<option value="thisWeek"
								${search.datePreset eq 'thisWeek' ? 'selected' : ''}>금주(~오늘)</option>
							<option value="lastWeek"
								${search.datePreset eq 'lastWeek' ? 'selected' : ''}>전주</option>
							<option value="thisMonth"
								${search.datePreset eq 'thisMonth' ? 'selected' : ''}>금월(~오늘)</option>
							<option value="lastMonth"
								${search.datePreset eq 'lastMonth' ? 'selected' : ''}>전월</option></select></label>
					<label class="field"><span>기준일자</span>
						<div class="input-group">
							<input type="date" name="startDate"
								value="<c:out value="${search.startDate}"/>" aria-label="조회 시작일" /><span>~</span><input
								type="date" name="endDate"
								value="<c:out value="${search.endDate}"/>" aria-label="조회 종료일" />
						</div></label>
					<c:set var="multiKind" value="warehouse" />
					<c:set var="multiLabel" value="출하창고" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="partner" />
					<c:set var="multiLabel" value="거래처" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="item" />
					<c:set var="multiLabel" value="품목" />
					<%@ include file="../common/multi-filter.jspf"%>
					<fieldset class="field field-wide">
						<legend>진행상태</legend>
						<div class="tabs">
							<label><input type="radio" name="progressStatus" value=""
								${empty search.progressStatus ? 'checked' : ''} /> 전체</label><label><input
								type="radio" name="progressStatus" value="UNCONFIRMED"
								${search.progressStatus eq 'UNCONFIRMED' ? 'checked' : ''} />
								미확인</label><label><input type="radio" name="progressStatus"
								value="CONFIRMED"
								${search.progressStatus eq 'CONFIRMED' ? 'checked' : ''} /> 확인</label>
						</div>
					</fieldset>
				</div>
				<div class="workspace-actions search-actions">
					<button class="btn btn-primary" type="submit">검색</button>
					<a class="btn" href="${ctx}/sale/bulk-price">다시 작성</a>
				</div>
			</form>
		</c:when>
		<c:otherwise>

			<c:url var="searchConditionsUrl" value="/sale/bulk-price">
				<c:forEach var="conditionParam" items="${search}">
					<c:if
						test="${conditionParam.key ne 'view' and conditionParam.key ne 'page' and conditionParam.key ne 'keyword'}">
						<c:param name="${conditionParam.key}"
							value="${conditionParam.value}" />
					</c:if>
				</c:forEach>
				<%@ include file="../common/multi-filter-params.jspf"%>
				<c:param name="view" value="search" />
			</c:url>
			<div class="workspace-heading result-heading">
				<h1>판매 단가 일괄 변경</h1>
				<form class="input-group list-keyword" method="get"
					action="${ctx}/sale/bulk-price">
					<c:forEach var="resultParam" items="${search}">
						<c:if
							test="${resultParam.key ne 'keyword' and resultParam.key ne 'page' and resultParam.key ne 'view'}">
							<input type="hidden" name="<c:out value='${resultParam.key}'/>"
								value="<c:out value='${resultParam.value}'/>" />
						</c:if>
					</c:forEach>
					<%@ include file="../common/multi-filter-hidden.jspf"%>
					<input type="hidden" name="view" value="results" /> <label
						class="sr-only" for="result-keyword">결과 내 검색어</label> <input
						id="result-keyword" type="search" name="keyword"
						value="<c:out value='${search.keyword}'/>"
						placeholder="입력 후 [Enter]" />
					<button class="btn btn-primary" type="submit">검색</button>
				</form>
			</div>
			<div class="workspace-meta result-controls">
				<div class="result-tools">
					<a class="btn btn-primary"
						href="<c:out value='${searchConditionsUrl}'/>">검색조건 변경</a><span
						class="workspace-hint">조회 결과</span>
				</div>
				<c:set var="basePath" value="/sale/bulk-price" /><%@ include
					file="../common/pagination.jsp"%><span
					class="workspace-hint">변경할 행을 선택한 뒤 단가를 입력하세요.</span>
			</div>
			<form method="post" action="${ctx}/sale/bulk-price/save"
				data-unimplemented-submit id="bulk-price-form">
				<div class="table-wrap">
					<table class="data-table workspace-table bulk-table">
						<colgroup>
							<col class="bulk-select" />
							<col class="bulk-date" />
							<col class="bulk-partner" />
							<col class="bulk-person" />
							<col class="bulk-warehouse" />
							<col class="bulk-date" />
							<col class="bulk-code" />
							<col class="bulk-item" />
							<col class="bulk-spec" />
							<col class="bulk-quantity" />
							<col class="bulk-unit" />
							<col class="bulk-price" />
							<col class="bulk-supply" />
							<col class="bulk-vat" />
							<col class="bulk-note" />
							<col class="bulk-time" />
							<col class="bulk-time" />
						</colgroup>
						<thead>
							<tr>
								<th class="col-select"><input type="checkbox"
									data-check-all="#bulk-price-form input[type=checkbox][name]"
									aria-label="현재 페이지 전체 선택" /></th>
								<th class="col-date">일자-No.</th>
								<th>거래처명</th>
								<th class="col-person">담당자</th>
								<th>출하창고</th>
								<th class="col-date">원주문 일자-No.</th>
								<th class="col-code">품목코드</th>
								<th class="col-item">품목명</th>
								<th class="col-spec">규격</th>
								<th class="text-right col-number">수량</th>
								<th class="col-unit text-center">단위</th>
								<th class="text-right col-number">단가</th>
								<th class="text-right col-number">공급가액</th>
								<th class="text-right col-number">부가세</th>
								<th class="note-cell">비고</th>
								<th class="col-timestamp">최초 작성시각</th>
								<th class="col-timestamp">최종 수정시각</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="row" items="${salePriceList}" varStatus="loop">
								<tr data-bulk-price-row
									data-quantity="<c:out value="${row.quantity}"/>">
									<td class="col-select"><input type="checkbox"
										name="changes[${loop.index}].selected" value="true"
										aria-label="단가 변경 행 선택" /><input type="hidden"
										name="changes[${loop.index}].saleLineId"
										value="<c:out value="${row.saleLineId}"/>" /><input
										type="hidden" name="changes[${loop.index}].saleId"
										value="<c:out value="${row.saleId}"/>" /></td>
									<td class="col-date"><c:out
											value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
											value="${row.saleNo}" /></td>
									<td><c:out value="${row.partnerName}" /></td>
									<td class="col-person"><c:out value="${row.assigneeName}" /></td>
									<td><c:out value="${row.warehouseName}" /></td>
									<td class="col-date"><c:if
											test="${not empty row.sourceDocumentNo}">
											<c:out
												value="${fn:replace(row.sourceBusinessDate, '-', '/')}" />-<c:out
												value="${row.sourceDocumentNo}" />
										</c:if></td>
									<td class="col-code"><c:out value="${row.itemCode}" /></td>
									<td class="col-item"><c:out value="${row.itemName}" /></td>
									<td class="col-spec"><c:out value="${row.specification}" /></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.quantity}" maxFractionDigits="3" /></td>
									<td class="col-unit text-center"><c:out
											value="${row.unit}" /></td>
									<td class="col-number text-right"><input
										class="price-input" type="number" step="0.01"
										name="changes[${loop.index}].unitPrice"
										value="<c:out value="${row.unitPrice}"/>" data-bulk-unit-price
										aria-label="판매 상세행 단가" required /></td>
									<td class="text-right col-number" data-bulk-supply><fmt:formatNumber
											value="${row.supplyAmount}" maxFractionDigits="0" /></td>
									<td class="text-right col-number" data-bulk-vat><fmt:formatNumber
											value="${row.vatAmount}" maxFractionDigits="0" /></td>
									<td class="note-cell"><c:out value="${row.note}" /></td>
									<td class="col-timestamp"><c:out value="${row.createdAt}" /></td>
									<td class="col-timestamp"><c:out value="${row.updatedAt}" /></td>
								</tr>
							</c:forEach>
							<c:if test="${empty salePriceList}">
								<tr>
									<td colspan="17" class="empty-state">단가를 변경할 판매 상세행이 없습니다.</td>
								</tr>
							</c:if>
						</tbody>
					</table>
				</div>
				<div class="workspace-actions">
					<button class="btn btn-primary" type="submit">선택한 단가 저장</button>
					<span class="muted">단가 변경 시 금액이 자동으로 계산됩니다.</span>
				</div>
			</form>

			<c:set var="pageScript" value="sale.js" />

		</c:otherwise>
	</c:choose>
</section>
<%@ include file="../common/footer.jsp"%>
