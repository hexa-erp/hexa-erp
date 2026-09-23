<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%-- businessDate 연월별 전체 결과의 월계·총합계를 표시하며 페이지 분할하지 않는다.
     단가 합계는 평균이 아닌 상세행 단가의 단순 합계다. --%>
<c:set var="pageTitle" value="견적서 현황" />
<c:set var="activeMenu" value="quotation" />
<c:set var="workspacePage" value="true" />
<c:set var="multiFilterPage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace report-workspace">
	<c:set var="reportResults" value="${param.view eq 'results'}" />
	<c:choose>
		<c:when test="${not reportResults}">
			<div class="workspace-heading">
				<h1>견적서 현황</h1>
				<p class="workspace-hint">상세 품목별 수량과 공급가액을 조회합니다.</p>
			</div>

			<form class="report-search" method="get"
				action="${ctx}/quotation/status">
				<input type="hidden" name="view" value="results" />
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
						</div></label> <label class="field"><span>견적서 번호</span><input
						type="text" name="quotationNo"
						value="<c:out value="${search.quotationNo}"/>" maxlength="30" /></label>
					<c:set var="multiKind" value="warehouse" />
					<c:set var="multiLabel" value="출하창고" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="partner" />
					<c:set var="multiLabel" value="거래처" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="item" />
					<c:set var="multiLabel" value="품목" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="assignee" />
					<c:set var="multiLabel" value="담당자" />
					<%@ include file="../common/multi-filter.jspf"%>
					<label class="field"><span>규격</span><input type="text"
						name="specification"
						value="<c:out value="${search.specification}"/>" maxlength="100" /></label>
					<label class="field"><span>수량</span>
						<div class="input-group">
							<input type="number" name="minQuantity" step="0.001"
								value="<c:out value="${search.minQuantity}"/>"
								aria-label="최소 수량" /><span>~</span><input type="number"
								name="maxQuantity" step="0.001"
								value="<c:out value="${search.maxQuantity}"/>"
								aria-label="최대 수량" />
						</div></label> <label class="field"><span>단가</span>
						<div class="input-group">
							<input type="number" name="minUnitPrice" step="0.01"
								value="<c:out value="${search.minUnitPrice}"/>"
								aria-label="최소 단가" /><span>~</span><input type="number"
								name="maxUnitPrice" step="0.01"
								value="<c:out value="${search.maxUnitPrice}"/>"
								aria-label="최대 단가" />
						</div></label> <label class="field"><span>공급가액</span>
						<div class="input-group">
							<input type="number" name="minSupplyAmount" step="1"
								value="<c:out value="${search.minSupplyAmount}"/>"
								aria-label="최소 공급가액" /><span>~</span><input type="number"
								name="maxSupplyAmount" step="1"
								value="<c:out value="${search.maxSupplyAmount}"/>"
								aria-label="최대 공급가액" />
						</div></label> <label class="field"><span>부가세</span>
						<div class="input-group">
							<input type="number" name="minVatAmount" step="1"
								value="<c:out value="${search.minVatAmount}"/>"
								aria-label="최소 부가세" /><span>~</span><input type="number"
								name="maxVatAmount" step="1"
								value="<c:out value="${search.maxVatAmount}"/>"
								aria-label="최대 부가세" />
						</div></label>
					<fieldset class="field field-wide">
						<legend>진행상태</legend>
						<div class="tabs">
							<label><input type="radio" name="progressStatus" value=""
								${search.progressStatus eq '' ? 'checked' : ''} /> 전체</label><label><input
								type="radio" name="progressStatus" value="UNCONFIRMED"
								${search.progressStatus eq 'UNCONFIRMED' ? 'checked' : ''} />
								미확인</label><label><input type="radio" name="progressStatus"
								value="IN_PROGRESS"
								${search.progressStatus eq 'IN_PROGRESS' ? 'checked' : ''} />
								진행중</label><label><input type="radio" name="progressStatus"
								value="COMPLETED"
								${search.progressStatus eq 'COMPLETED' ? 'checked' : ''} /> 완료</label>
						</div>
					</fieldset>

				</div>
				<div class="workspace-actions search-actions">
					<button class="btn btn-primary" type="submit">검색</button>
					<a class="btn" href="${ctx}/quotation/status">다시 작성</a>
				</div>
			</form>
		</c:when>
		<c:otherwise>

			<c:url var="searchConditionsUrl" value="/quotation/status">
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
				<h1>견적서 현황</h1>
				<form class="input-group list-keyword" method="get"
					action="${ctx}/quotation/status">
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
				<span class="workspace-hint">일자·번호 오름차순 · 읽기 전용</span>
			</div>
			<div class="table-wrap">
				<table class="data-table workspace-table report-table">
					<thead>
						<tr>
							<th class="col-date">일자-No.</th>
							<th class="col-item">품목명(규격)</th>
							<th class="text-right col-number">수량</th>
							<th class="text-right col-number">단가</th>
							<th class="text-right col-number">공급가액</th>
							<th>거래처명</th>
							<th class="note-cell">비고</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="monthGroup" items="${reportMonths}">
							<c:forEach var="row" items="${monthGroup.rows}">
								<tr>
									<td class="col-date"><c:out
											value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
											value="${row.quotationNo}" /></td>
									<td class="col-item"><c:out value="${row.itemName}" /> <c:if
											test="${not empty row.specification}"> [<c:out
												value="${row.specification}" />]</c:if></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.quantity}" maxFractionDigits="3" /></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.unitPrice}" maxFractionDigits="2" /></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.supplyAmount}" maxFractionDigits="0" /></td>
									<td><c:out value="${row.partnerName}" /></td>
									<td class="note-cell"><c:out value="${row.note}" /></td>
								</tr>
							</c:forEach>
							<tr class="report-subtotal">
								<th scope="row" colspan="2"><c:out
										value="${monthGroup.monthLabel}" /> 계</th>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.quantity}" maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.unitPrice}" maxFractionDigits="2" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.supplyAmount}"
										maxFractionDigits="0" /></td>
								<td></td>
								<td></td>
							</tr>
						</c:forEach>
						<c:if test="${empty quotationStatusList}">
							<tr>
								<td colspan="7" class="empty-state">조회된 견적서 현황 내역이 없습니다.</td>
							</tr>
						</c:if>
					</tbody>
					<c:if test="${not empty quotationStatusList}">
						<tfoot>
							<tr class="report-grand-total">
								<th scope="row" colspan="2">총합계</th>
								<td class="text-right col-number"><fmt:formatNumber
										value="${statusTotals.quantity}" maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${statusTotals.unitPrice}" maxFractionDigits="2" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${statusTotals.supplyAmount}" maxFractionDigits="0" /></td>
								<td></td>
								<td></td>
							</tr>
						</tfoot>
					</c:if>
				</table>
			</div>

		</c:otherwise>
	</c:choose>
</section>
<%@ include file="../common/footer.jsp"%>
