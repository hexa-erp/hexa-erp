<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="見積一覧" />
<c:set var="activeMenu" value="quotation" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace document-workspace">
	<form method="get" action="${ctx}/quotation/list" class="list-search"
		data-status-search>
		<input type="hidden" name="progressStatus"
			value="<c:out value='${search.progressStatus}'/>" /> <input
			type="hidden" name="pageSize" value="25" />
		<div class="list-title-status">
			<h1>見積一覧</h1>
			<div class="tabs" role="group" aria-label="進捗状況検索">
				<button
					class="btn ${search.progressStatus eq '' ? 'btn-primary' : ''}"
					type="button" data-search-status=""
					aria-pressed="${search.progressStatus eq '' ? 'true' : 'false'}">すべて</button>
				<button
					class="btn ${search.progressStatus eq 'UNCONFIRMED' ? 'btn-primary' : ''}"
					type="button" data-search-status="UNCONFIRMED"
					aria-pressed="${search.progressStatus eq 'UNCONFIRMED' ? 'true' : 'false'}">未確認</button>
				<button
					class="btn ${search.progressStatus eq 'IN_PROGRESS' ? 'btn-primary' : ''}"
					type="button" data-search-status="IN_PROGRESS"
					aria-pressed="${search.progressStatus eq 'IN_PROGRESS' ? 'true' : 'false'}">進行中</button>
				<button
					class="btn ${search.progressStatus eq 'COMPLETED' ? 'btn-primary' : ''}"
					type="button" data-search-status="COMPLETED"
					aria-pressed="${search.progressStatus eq 'COMPLETED' ? 'true' : 'false'}">完了</button>
			</div>
		</div>
		<label class="input-group list-keyword"><span class="sr-only">検索語</span><input
			type="search" name="keyword"
			value="<c:out value="${search.keyword}"/>" placeholder="入力後に［Enter］" />
			<button class="btn btn-primary" type="submit">検索</button></label>
	</form>

	<div class="workspace-meta">
		<c:set var="basePath" value="/quotation/list" /><%@ include
			file="../common/pagination.jsp"%>
		<span class="workspace-hint">取引先名、担当者名、品目名で検索できます。</span>
	</div>
	<form id="document-list-actions" method="post" data-api-selection
		data-api-base="/api/quotations" action="${ctx}/api/quotations/status">
		<%@ include file="../common/return-search.jspf"%>
		<div class="table-wrap">
			<table class="data-table workspace-table document-table">
				<thead>
					<tr>
						<th class="col-select"><input type="checkbox"
							data-check-all="#document-list-actions input[name=selectedIds]"
							aria-label="現在のページをすべて選択" /></th>
						<th class="col-date">日付-No.</th>
						<th>取引先名</th>
						<th class="col-person">担当者名</th>
						<th class="col-item">品目名（概要）</th>
						<th class="text-right col-number">金額 合計</th>
						<th class="col-status text-center">進捗状況</th>
						<th class="col-link text-center">見積書 伝票</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${quotationList}">
						<tr>
							<td class="col-select"><input type="checkbox"
								name="selectedIds" value="<c:out value="${row.quotationId}"/>"
								aria-label="伝票選択" /></td>
							<td class="col-date"><c:url var="documentEditUrl"
									value="/quotation/form">
									<c:param name="id" value="${row.quotationId}" />
									<%@ include file="../common/return-search-params.jspf"%>
								</c:url> <a href="<c:out value='${documentEditUrl}'/>"><c:out
										value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
										value="${row.quotationNo}" /></a></td>
							<td><c:out value="${row.partnerName}" /></td>
							<td class="col-person"><c:out value="${row.assigneeName}" /></td>
							<td class="col-item"><c:out value="${row.itemSummary}" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.totalAmount}" maxFractionDigits="0" /></td>
							<td class="col-status text-center"><span class="badge"><c:choose>
										<c:when test="${row.progressStatus eq 'UNCONFIRMED'}">未確認</c:when>
										<c:when test="${row.progressStatus eq 'IN_PROGRESS'}">進行中</c:when>
										<c:when test="${row.progressStatus eq 'COMPLETED'}">完了</c:when>
										<c:otherwise>
											<c:out value="${row.progressStatus}" />
										</c:otherwise>
									</c:choose></span></td>
							<td class="col-link text-center"><a
								href="${ctx}/quotation/statement?id=<c:out value="${row.quotationId}"/>">照会</a></td>
						</tr>
					</c:forEach>
					<c:if test="${empty quotationList}">
						<tr>
							<td colspan="8" class="empty-state">伝票が見つかりません。</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>

		<div class="workspace-actions">
			<a class="btn btn-primary" href="${ctx}/quotation/form">新規</a>
			<div class="action-dropdown">
				<button class="btn" type="button" data-status-toggle
					aria-expanded="false" aria-controls="status-options">
					進捗状況を変更 <span aria-hidden="true">▾</span>
				</button>
				<div class="status-options" id="status-options" role="group"
					aria-label="変更後の進捗状況" hidden>
					<button class="status-option" type="button"
						data-status-value="UNCONFIRMED">未確認</button>
					<button class="status-option" type="button"
						data-status-value="IN_PROGRESS">進行中</button>
					<button class="status-option" type="button"
						data-status-value="COMPLETED">完了</button>
				</div>
			</div>
			<input type="hidden" name="nextProgressStatus" value="" />
			<button type="submit" name="operation" value="status"
				data-status-submit hidden>状態を適用</button>
			<button class="btn btn-danger" type="submit" name="operation"
				value="delete" data-confirm-delete
				formaction="${ctx}/api/quotations" data-api-delete>選択 削除</button>
		</div>
	</form>
</section>
<%@ include file="../common/footer.jsp"%>
