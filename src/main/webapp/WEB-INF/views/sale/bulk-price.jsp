<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- changes[i]のうち選択した行だけを保存する。単価の桁区切りは表示用で、元の値を送信する。 --%>
<%-- 担当者は検索条件に含めない。倉庫・取引先・品目の条件は結果内検索やページ移動でも維持する。 --%>
<c:set var="pageTitle" value="売上単価一括変更" />
<c:set var="activeMenu" value="sale" />
<c:set var="workspacePage" value="true" />
<c:set var="multiFilterPage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace bulk-workspace">
	<c:set var="reportResults" value="${param.view eq 'results'}" />
	<c:choose>
		<c:when test="${not reportResults}">
			<div class="workspace-heading">
				<h1>売上単価一括変更</h1>
				<p class="workspace-hint">売上明細ごとに単価を変更するか、選択した行に同じ単価・比率を一括適用します。</p>
			</div>
			<form class="report-search" method="get"
				action="${ctx}/sale/bulk-price">
				<input type="hidden" name="view" value="results" /> <input
					type="hidden" name="pageSize" value="25" />
				<div class="form-grid report-fields">
					<label class="field"><span>照会期間</span><select
						name="datePreset" data-date-preset><option value="custom"
								${search.datePreset eq 'custom' ? 'selected' : ''}>直接入力</option>
							<option value="today"
								${search.datePreset eq 'today' ? 'selected' : ''}>当日</option>
							<option value="yesterday"
								${search.datePreset eq 'yesterday' ? 'selected' : ''}>前日</option>
							<option value="thisWeek"
								${search.datePreset eq 'thisWeek' ? 'selected' : ''}>今週（本日まで）</option>
							<option value="lastWeek"
								${search.datePreset eq 'lastWeek' ? 'selected' : ''}>先週</option>
							<option value="thisMonth"
								${search.datePreset eq 'thisMonth' ? 'selected' : ''}>今月（本日まで）</option>
							<option value="lastMonth"
								${search.datePreset eq 'lastMonth' ? 'selected' : ''}>先月</option></select></label>
					<label class="field"><span>基準日</span>
						<div class="input-group">
							<input type="date" name="startDate"
								value="<c:out value="${search.startDate}"/>" aria-label="照会開始日" /><span>~</span><input
								type="date" name="endDate"
								value="<c:out value="${search.endDate}"/>" aria-label="照会終了日" />
						</div></label>
					<c:set var="multiKind" value="warehouse" />
					<c:set var="multiLabel" value="出荷倉庫" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="partner" />
					<c:set var="multiLabel" value="取引先" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="item" />
					<c:set var="multiLabel" value="品目" />
					<%@ include file="../common/multi-filter.jspf"%>
					<fieldset class="field field-wide">
						<legend>進捗状況</legend>
						<div class="tabs">
							<label><input type="radio" name="progressStatus" value=""
								${empty search.progressStatus ? 'checked' : ''} /> すべて</label><label><input
								type="radio" name="progressStatus" value="UNCONFIRMED"
								${search.progressStatus eq 'UNCONFIRMED' ? 'checked' : ''} />
								未確認</label><label><input type="radio" name="progressStatus"
								value="CONFIRMED"
								${search.progressStatus eq 'CONFIRMED' ? 'checked' : ''} /> 確認</label>
						</div>
					</fieldset>
				</div>
				<div class="workspace-actions search-actions">
					<button class="btn btn-primary" type="submit">検索</button>
					<a class="btn" href="${ctx}/sale/bulk-price">リセット</a>
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
				<h1>売上単価一括変更</h1>
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
						class="sr-only" for="result-keyword">結果内の検索語</label> <input
						id="result-keyword" type="search" name="keyword"
						value="<c:out value='${search.keyword}'/>"
						placeholder="入力後に［Enter］" />
					<button class="btn btn-primary" type="submit">検索</button>
				</form>
			</div>
			<div class="workspace-meta result-controls">
				<div class="result-tools">
					<a class="btn btn-primary"
						href="<c:out value='${searchConditionsUrl}'/>">検索条件を変更</a><span
						class="workspace-hint">照会結果</span>
				</div>
				<c:set var="basePath" value="/sale/bulk-price" /><%@ include
					file="../common/pagination.jsp"%><span
					class="workspace-hint">変更する行を選択し、単価を入力するか［選択行に適用］してから［選択した単価を保存］を押してください。</span>
			</div>
			<form method="post" action="${ctx}/api/sales/prices" data-api-prices
				id="bulk-price-form">
				<%@ include file="../common/return-search.jspf"%>
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
									aria-label="現在のページをすべて選択" /></th>
								<th class="col-date">日付-No.</th>
								<th>取引先名</th>
								<th class="col-person">担当者</th>
								<th>出荷倉庫</th>
								<th class="col-date">円受注 日付-No.</th>
								<th class="col-code">品目コード</th>
								<th class="col-item">品目名</th>
								<th class="col-spec">規格</th>
								<th class="text-right col-number">数量</th>
								<th class="col-unit text-center">単位</th>
								<th class="text-right col-number">単価</th>
								<th class="text-right col-number">税抜金額</th>
								<th class="text-right col-number">消費税</th>
								<th class="note-cell">備考</th>
								<th class="col-timestamp">作成日時</th>
								<th class="col-timestamp">最終更新日時</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="row" items="${salePriceList}" varStatus="loop">
								<%-- data-quantityはsale.jsが単価入力時の金額プレビューに使う。保存金額はサーバーで再計算する。 --%>
								<tr data-bulk-price-row
									data-quantity="<c:out value="${row.quantity}"/>">
									<%-- チェックした行だけselected=trueで送る。別の伝票の行を変更しないよう、サーバーで2つのIDを照合する。 --%>
									<td class="col-select"><input type="checkbox"
										name="changes[${loop.index}].selected" value="true"
										aria-label="単価変更行の選択" /><input type="hidden"
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
										aria-label="売上 明細行 単価" required /></td>
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
									<td colspan="17" class="empty-state">単価を変更する売上明細がありません。</td>
								</tr>
							</c:if>
						</tbody>
					</table>
				</div>
				<div class="workspace-actions">
					<%-- 選択行への一括適用は単価指定または現在単価に対する比率（%）調整。画面の値だけを変更し、保存は［選択した単価を保存］で行う。 --%>
					<div class="input-group bulk-apply">
						<select data-bulk-apply-mode aria-label="単価の適用方法">
							<option value="price">単価指定</option>
							<option value="percent">比率（%）調整</option>
						</select> <input type="number" step="any" data-bulk-apply-value
							placeholder="変更後の単価" aria-label="適用する値" /><span
							data-bulk-apply-unit>円</span>
						<button class="btn" type="button" data-bulk-apply>選択行に適用</button>
					</div>
					<button class="btn btn-primary" type="submit">選択した単価を保存</button>
					<span class="muted">単価を変更すると、税抜金額と消費税が自動で再計算されます。</span>
				</div>
			</form>
			<c:set var="pageScript" value="sale.js" />
		</c:otherwise>
	</c:choose>
</section>
<%@ include file="../common/footer.jsp"%>
