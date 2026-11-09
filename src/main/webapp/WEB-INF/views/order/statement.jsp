<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="受注伝票 伝票" />
<c:set var="activeMenu" value="order" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace statement-workspace">
	<div class="workspace-heading">
		<h1>受注伝票 伝票</h1>
		<a class="btn" href="${ctx}/order/list">一覧</a>
	</div>
	<c:choose>
		<c:when test="${empty form.salesOrderId}">
			<div class="empty-state">照会する伝票がありません。</div>
		</c:when>
		<c:otherwise>
			<div class="statement-meta form-grid">
				<div class="field">
					<span>日付</span>
					<c:out value="${form.businessDate}" />
				</div>
				<div class="field">
					<span>番号</span>
					<c:out value="${form.salesOrderNo}" />
				</div>
				<div class="field">
					<span>取引先</span>
					<c:out value="${form.partnerName}" />
				</div>
				<div class="field">
					<span>担当者</span>
					<c:out value="${form.assigneeName}" />
				</div>
				<div class="field">
					<span>倉庫</span>
					<c:out value="${form.warehouseName}" />
				</div>
				<div class="field">
					<span>納期日</span>
					<c:out value="${form.dueDate}" />
				</div>
				<div class="field">
					<span>備考</span>
					<c:out value="${form.note}" />
				</div>
			</div>
			<div class="table-wrap">
				<table class="data-table workspace-table statement-table">
					<thead>
						<tr>
							<th class="col-code">品目コード</th>
							<th class="col-item">品目名</th>
							<th class="col-spec">規格</th>
							<th class="col-unit text-center">単位</th>
							<th class="text-right col-number">数量</th>
							<th class="text-right col-number">単価</th>
							<th class="text-right col-number">税抜金額</th>
							<th class="text-right col-number">消費税</th>
							<th class="text-right col-number">合計</th>
							<th class="note-cell">備考</th>
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
								<td colspan="10" class="empty-state">明細品目がありません。</td>
							</tr>
						</c:if>
					</tbody>
					<tfoot>
						<tr>
							<th colspan="6">合計</th>
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
