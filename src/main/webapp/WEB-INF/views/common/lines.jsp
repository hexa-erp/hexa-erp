<%@ page pageEncoding="UTF-8"%>
<%-- lineIdFieldはこの伝票の明細ID、sourceIdFieldは元伝票の明細ID。
     削除した既存行のIDはremovedLineIdsで送る。単位・規格は伝票に保存したスナップショット値。 --%>
<section id="document-lines" data-line-id-field="${lineIdField}"
	data-source-id-field="${sourceIdField}" data-priced="${priced}">
	<div class="toolbar line-toolbar">
		<h3>品目明細</h3>
		<span class="spacer"></span>
		<button type="button" class="btn" data-line-add>行を追加</button>
		<button type="button" class="btn btn-danger" data-lines-remove>選択
			削除</button>
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
						data-check-all="#line-body .line-select" aria-label="すべての品目を選択"></th>
					<th>行</th>
					<th>品目コード</th>
					<th>品目名</th>
					<th>規格</th>
					<th class="text-right">数量</th>
					<c:if test="${priced}">
						<th class="text-right">単価</th>
						<th class="text-right">税抜金額</th>
						<th class="text-right">消費税</th>
					</c:if>
					<th>備考</th>
					<c:if test="${priced}">
						<th class="text-right">合計</th>
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
		<span>数量 <strong id="line-total-quantity">0</strong></span>
		<c:if test="${priced}">
			<span>税抜金額 <strong id="line-total-supply">0</strong></span>
			<span>消費税 <strong id="line-total-vat">0</strong></span>
			<span>合計 <strong id="line-total-amount">0</strong></span>
		</c:if>
	</div>
	<c:remove var="row" />
	<c:set var="rowIndex" value="0" />
	<template id="line-template"><%@ include
		file="line-row.jspf"%></template>
	<div id="removed-line-ids"></div>
</section>
