<%@ page pageEncoding="UTF-8"%>
<%-- lineIdField는 현재 상세 PK, sourceIdField는 원전표 상세 FK다. 삭제 시 기존 상세 ID만 removedLineIds에 보낸다.
     단위·규격은 전표 스냅샷 --%>
<section id="document-lines" data-line-id-field="${lineIdField}"
	data-source-id-field="${sourceIdField}" data-priced="${priced}">
	<div class="toolbar line-toolbar">
		<h3>품목 내역</h3>
		<span class="spacer"></span>
		<button type="button" class="btn" data-line-add>행 추가</button>
		<button type="button" class="btn btn-danger" data-lines-remove>선택
			삭제</button>
	</div>
	<div class="table-wrap">
		<table class="data-table line-table">
			<colgroup>
				<col class="line-col-check">
				<col class="line-col-index">
				<col class="line-col-code">
				<col class="line-col-item">
				<col class="line-col-spec">
				<col class="line-col-quantity">
				<c:if test="${priced}">
					<col class="line-col-price">
					<col class="line-col-supply">
					<col class="line-col-vat">
				</c:if>
				<col class="line-col-note">
				<c:if test="${priced}">
					<col class="line-col-total">
				</c:if>
			</colgroup>
			<thead>
				<tr>
					<th><input type="checkbox"
						data-check-all="#line-body .line-select" aria-label="품목 전체 선택"></th>
					<th>행</th>
					<th>품목코드</th>
					<th>품목명</th>
					<th>규격</th>
					<th class="text-right">수량</th>
					<c:if test="${priced}">
						<th class="text-right">단가</th>
						<th class="text-right">공급가액</th>
						<th class="text-right">부가세</th>
					</c:if>
					<th>비고</th>
					<c:if test="${priced}">
						<th class="text-right">합계</th>
					</c:if>
				</tr>
			</thead>
			<tbody id="line-body">
				<c:forEach var="row" items="${form.lines}" varStatus="loop">
					<c:set var="rowIndex" value="${loop.index}" /><%@ include
						file="line-row.jspf"%></c:forEach>
			</tbody>
		</table>
	</div>
	<div class="total-strip">
		<span>수량 <strong id="line-total-quantity">0</strong></span>
		<c:if test="${priced}">
			<span>공급가액 <strong id="line-total-supply">0</strong></span>
			<span>부가세 <strong id="line-total-vat">0</strong></span>
			<span>합계 <strong id="line-total-amount">0</strong></span>
		</c:if>
	</div>
	<c:remove var="row" />
	<c:set var="rowIndex" value="0" />
	<template id="line-template"><%@ include
		file="line-row.jspf"%></template>
	<div id="removed-line-ids"></div>
</section>
