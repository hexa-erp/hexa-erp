<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 進捗状況タブと検索語で照会し、選択した伝票の状況変更・削除を1つのformで扱う。 --%>

<c:set var="pageTitle" value="出荷一覧" />
<c:set var="activeMenu" value="shipment" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace document-workspace">
	<form method="get" action="${ctx}/shipment/list" class="list-search"
		data-status-search>
		<%-- 進捗状況タブを押すと値を変更し、1ページ目から再照会する。 --%>
		<input type="hidden" name="progressStatus"
			value="<c:out value='${search.progressStatus}'/>" /> <input
			type="hidden" name="pageSize" value="25" />
		<div class="list-title-status">
			<h1>出荷一覧</h1>
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
					class="btn ${search.progressStatus eq 'CONFIRMED' ? 'btn-primary' : ''}"
					type="button" data-search-status="CONFIRMED"
					aria-pressed="${search.progressStatus eq 'CONFIRMED' ? 'true' : 'false'}">確認</button>
			</div>
		</div>

		<label class="input-group list-keyword"><span class="sr-only">検索語</span><input
			type="search" name="keyword"
			value="<c:out value="${search.keyword}"/>" placeholder="入力後に［Enter］" />
			<button class="btn btn-primary" type="submit">検索</button></label>
	</form>
	<div class="workspace-meta">
		<c:set var="basePath" value="/shipment/list" />
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">品目名で検索できます。</span>
	</div>
	<form id="document-list-actions" method="post" data-api-selection
		data-api-base="/api/shipments" action="${ctx}/api/shipments/status">
		<%-- 処理後に同じ検索条件の一覧へ戻れるよう、現在の条件をreturn.*として一緒に送る。 --%>
		<%@ include file="../common/return-search.jspf"%>
		<div class="table-wrap">
			<table class="data-table workspace-table document-table">
				<thead>
					<tr>
						<th class="check-cell col-select"><input type="checkbox"
							data-check-all="#document-list-actions input[name=selectedIds]"
							aria-label="現在のページをすべて選択" /></th>
						<th class="col-date">日付-No.</th>
						<th>倉庫名</th>
						<th class="col-item">品目名（概要）</th>
						<th class="text-right col-number">数量 合計</th>
						<th class="col-status text-center">進捗状況</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${shipmentList}">
						<tr>
							<td class="col-select"><input type="checkbox"
								name="selectedIds" value="<c:out value="${row.shipmentId}"/>"
								aria-label="伝票選択" /></td>
							<td class="col-date"><c:url var="documentEditUrl"
									value="/shipment/form">
									<c:param name="id" value="${row.shipmentId}" />
									<%@ include file="../common/return-search-params.jspf"%>
								</c:url> <a href="<c:out value='${documentEditUrl}'/>"><c:out
										value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
										value="${row.shipmentNo}" /></a></td>
							<td><c:out value="${row.warehouseName}" /></td>
							<td class="col-item"><c:out value="${row.itemSummary}" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.totalQuantity}" maxFractionDigits="3" /></td>
							<td class="col-status text-center"><span class="badge"><c:choose>
										<c:when test="${row.progressStatus eq 'UNCONFIRMED'}">未確認</c:when>
										<c:when test="${row.progressStatus eq 'CONFIRMED'}">確認</c:when>
										<c:otherwise>
											<c:out value="${row.progressStatus}" />
										</c:otherwise>
									</c:choose></span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty shipmentList}">
						<tr>
							<td colspan="6" class="empty-state">伝票が見つかりません。</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<div class="workspace-actions">
			<a class="btn btn-primary" href="${ctx}/shipment/form">新規</a>
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
						data-status-value="CONFIRMED">確認</button>
				</div>
			</div>
			<%-- 状態を選ぶとnextProgressStatusに値を設定し、非表示の状態適用ボタンから送信する。 --%>
			<input type="hidden" name="nextProgressStatus" value="" />
			<button type="submit" name="operation" value="status"
				data-status-submit hidden>状態を適用</button>
			<%-- 削除ボタンは共通スクリプトからDELETEリクエストを送信する。 --%>
			<button class="btn btn-danger" type="submit" name="operation"
				value="delete" data-confirm-delete
				formaction="${ctx}/api/shipments" data-api-delete>選択 削除</button>
		</div>
	</form>
</section>
<%@ include file="../common/footer.jsp"%>