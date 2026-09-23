<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="거래명세서" />
<c:set var="activeMenu" value="sale" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace statement-workspace">
	<div class="workspace-heading">
		<div>
			<h1>거래명세서</h1>
			<p class="muted">판매 전표의 상세 내용을 조회합니다.</p>
		</div>
		<a class="btn" href="${ctx}/sale/list">판매 목록</a>
	</div>
	<c:choose>
		<c:when test="${empty form.saleId}">
			<div class="empty-state">조회할 판매 전표가 없습니다.</div>
		</c:when>
		<c:otherwise>
			<div class="statement-meta form-grid">
				<div class="field">
					<span>판매 일자-No.</span><strong><c:out
							value="${fn:replace(form.businessDate, '-', '/')}" />-<c:out
							value="${form.saleNo}" /></strong>
				</div>
				<div class="field">
					<span>거래처</span><strong><c:out value="${form.partnerName}"
							default="-" /></strong>
				</div>
				<div class="field">
					<span>출하창고</span>
					<c:out value="${form.warehouseName}" />
				</div>
				<div class="field">
					<span>담당자</span>
					<c:out value="${form.assigneeName}" default="-" />
				</div>
				<div class="field field-wide">
					<span>비고</span>
					<c:out value="${form.note}" default="-" />
				</div>
			</div>
			<div class="table-wrap">
				<table class="data-table workspace-table statement-table">
					<thead>
						<tr>
							<th class="col-code">품목코드</th>
							<th class="col-item">품목명</th>
							<th class="col-spec">규격</th>
							<th class="col-unit text-center">단위</th>
							<th class="text-right col-number">수량</th>
							<th class="text-right col-number">단가</th>
							<th class="text-right col-number">공급가액</th>
							<th class="text-right col-number">부가세</th>
							<th class="text-right col-number">합계</th>
							<th class="note-cell">비고</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="row" items="${form.lines}">
							<tr>
								<td class="col-code"><c:out value="${row.itemCode}" /></td>
								<td class="col-item"><c:out value="${row.itemName}" /></td>
								<td class="col-spec"><c:out value="${row.specification}" /></td>
								<td class="col-unit text-center"><c:out value="${row.unit}" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${row.quantity}" maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${row.unitPrice}" maxFractionDigits="2" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${row.supplyAmount}" maxFractionDigits="0" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${row.vatAmount}" maxFractionDigits="0" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${row.totalAmount}" maxFractionDigits="0" /></td>
								<td class="note-cell"><c:out value="${row.note}" /></td>
							</tr>
						</c:forEach>
						<c:if test="${empty form.lines}">
							<tr>
								<td colspan="10" class="empty-state">판매 상세행이 없습니다.</td>
							</tr>
						</c:if>
					</tbody>
					<tfoot>
						<tr>
							<th colspan="6">합계</th>
							<td class="text-right"><fmt:formatNumber
									value="${statementTotals.supplyAmount}" maxFractionDigits="0" /></td>
							<td class="text-right"><fmt:formatNumber
									value="${statementTotals.vatAmount}" maxFractionDigits="0" /></td>
							<td class="text-right"><fmt:formatNumber
									value="${statementTotals.totalAmount}" maxFractionDigits="0" /></td>
							<td></td>
						</tr>
					</tfoot>
				</table>
			</div>
		</c:otherwise>
	</c:choose>
</section>
<%@ include file="../common/footer.jsp"%>
