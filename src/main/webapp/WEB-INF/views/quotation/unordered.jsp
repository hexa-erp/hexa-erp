<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%-- businessDateの年月別に全件の月計・総合計を表示し、ページ分割しない。
     単価合計は作らず、サーバーが計算した残数量・残税抜金額・消費税を使う。
     monthGroupsには照会結果のある月だけを入れ、結果がなければ空の一覧を渡す。
     各グループはmonthLabel、rows、totalsを持つ。 --%>
<c:set var="pageTitle" value="未受注一覧" />
<c:set var="activeMenu" value="quotation" />
<c:set var="workspacePage" value="true" />
<c:set var="multiFilterPage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace report-workspace">
	<c:set var="reportResults" value="${param.view eq 'results'}" />
	<c:choose>
		<c:when test="${not reportResults}">
			<div class="workspace-heading">
				<h1>未受注一覧</h1>
				<p class="workspace-hint">基準日までの未受注数量と金額を照会します。</p>
			</div>

			<form class="report-search" method="get"
				action="${ctx}/quotation/unordered">
				<input type="hidden" name="view" value="results" />
				<div class="form-grid report-fields">
					<label class="field"><span>照会期間</span><select
						name="datePreset" data-date-preset data-date-target="cutoffDate"><option
								value="custom"
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
					<label class="field"><span>見積日付</span>
   	 				<div class="input-group"><input type="date" name="startDate"
						value="<c:out value="${search.startDate}"/>" aria-label="照会開始日" /><span>~</span><input
            			type="date" name="endDate"
						value="<c:out value="${search.endDate}"/>" aria-label="照会終了日" />
    				</div></label>
					<label class="field"><span>基準日</span><input type="date"
						name="cutoffDate" value="<c:out value="${search.cutoffDate}"/>"
						required /></label> <label class="field"><span>見積書 番号</span><input
						type="text" name="quotationNo"
						value="<c:out value="${search.quotationNo}"/>" maxlength="30" /></label>
					<c:set var="multiKind" value="warehouse" />
					<c:set var="multiLabel" value="出荷倉庫" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="partner" />
					<c:set var="multiLabel" value="取引先" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="item" />
					<c:set var="multiLabel" value="品目" />
					<%@ include file="../common/multi-filter.jspf"%>
					<c:set var="multiKind" value="assignee" />
					<c:set var="multiLabel" value="担当者" />
					<%@ include file="../common/multi-filter.jspf"%>
					<label class="field"><span>規格</span><input type="text"
						name="specification"
						value="<c:out value="${search.specification}"/>" maxlength="100" /></label>
					<label class="field"><span>未受注 数量</span>
						<div class="input-group">
							<input type="number" name="minQuantity" step="0.001"
								value="<c:out value="${search.minQuantity}"/>"
								aria-label="最小 数量" /><span>~</span><input type="number"
								name="maxQuantity" step="0.001"
								value="<c:out value="${search.maxQuantity}"/>"
								aria-label="最大 数量" />
						</div></label> <label class="field"><span>単価</span>
						<div class="input-group">
							<input type="number" name="minUnitPrice" step="0.01"
								value="<c:out value="${search.minUnitPrice}"/>"
								aria-label="最小 単価" /><span>~</span><input type="number"
								name="maxUnitPrice" step="0.01"
								value="<c:out value="${search.maxUnitPrice}"/>"
								aria-label="最大 単価" />
						</div></label> <label class="field"><span>未受注 税抜金額</span>
						<div class="input-group">
							<input type="number" name="minSupplyAmount" step="1"
								value="<c:out value="${search.minSupplyAmount}"/>"
								aria-label="最小 税抜金額" /><span>~</span><input type="number"
								name="maxSupplyAmount" step="1"
								value="<c:out value="${search.maxSupplyAmount}"/>"
								aria-label="最大 税抜金額" />
						</div></label> <label class="field"><span>未受注 消費税</span>
						<div class="input-group">
							<input type="number" name="minVatAmount" step="1"
								value="<c:out value="${search.minVatAmount}"/>"
								aria-label="最小 消費税" /><span>~</span><input type="number"
								name="maxVatAmount" step="1"
								value="<c:out value="${search.maxVatAmount}"/>"
								aria-label="最大 消費税" />
						</div></label>
					<fieldset class="field field-wide">
						<legend>進捗状況</legend>
						<div class="tabs">
							<label><input type="radio" name="progressStatus" value=""
								${search.progressStatus eq '' ? 'checked' : ''} /> すべて</label><label><input
								type="radio" name="progressStatus" value="UNCONFIRMED"
								${search.progressStatus eq 'UNCONFIRMED' ? 'checked' : ''} />
								未確認</label><label><input type="radio" name="progressStatus"
								value="IN_PROGRESS"
								${search.progressStatus eq 'IN_PROGRESS' ? 'checked' : ''} />
								進行中</label><label><input type="radio" name="progressStatus"
								value="COMPLETED"
								${search.progressStatus eq 'COMPLETED' ? 'checked' : ''} /> 完了</label>
						</div>
					</fieldset>

				</div>
				<div class="workspace-actions search-actions">
					<button class="btn btn-primary" type="submit">検索</button>
					<a class="btn" href="${ctx}/quotation/unordered">リセット</a>
				</div>
			</form>
		</c:when>
		<c:otherwise>

			<c:url var="searchConditionsUrl" value="/quotation/unordered">
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
				<h1>未受注一覧</h1>
				<form class="input-group list-keyword" method="get"
					action="${ctx}/quotation/unordered">
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
				<span class="workspace-hint">日付・番号の昇順 · 閲覧専用</span>
			</div>
			<div class="table-wrap">
				<table class="data-table workspace-table report-table">
					<thead>
						<tr>
							<th class="col-date">日付-No.</th>
							<th class="col-item">品目名(規格)</th>
							<th class="text-right col-number">数量</th>
							<th class="text-right col-number">未受注 数量</th>
							<th class="text-right col-number">未受注 税抜金額</th>
							<th class="text-right col-number">未受注 消費税</th>
							<th>取引先名</th>
							<th class="note-cell">備考</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="monthGroup" items="${monthGroups}">
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
											value="${row.remainingQuantity}" maxFractionDigits="3" /></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.remainingSupplyAmount}" maxFractionDigits="0" /></td>
									<td class="text-right col-number"><fmt:formatNumber
											value="${row.remainingVatAmount}" maxFractionDigits="0" /></td>
									<td><c:out value="${row.partnerName}" /></td>
									<td class="note-cell"><c:out value="${row.note}" /></td>
								</tr>
							</c:forEach>
							<tr class="report-subtotal">
								<th scope="row" colspan="2"><c:out
										value="${monthGroup.monthLabel}" /> 計</th>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.quantity}" maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.remainingQuantity}"
										maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.remainingSupplyAmount}"
										maxFractionDigits="0" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${monthGroup.totals.remainingVatAmount}"
										maxFractionDigits="0" /></td>
								<td></td>
								<td></td>
							</tr>
						</c:forEach>
						<c:if test="${empty monthGroups}">
							<tr>
								<td colspan="8" class="empty-state">未受注の結果がありません。</td>
							</tr>
						</c:if>
					</tbody>
					<c:if test="${not empty monthGroups}">
						<tfoot>
							<tr class="report-grand-total">
								<th scope="row" colspan="2">総合計</th>
								<td class="text-right col-number"><fmt:formatNumber
										value="${remainingTotals.quantity}" maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${remainingTotals.remainingQuantity}"
										maxFractionDigits="3" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${remainingTotals.remainingSupplyAmount}"
										maxFractionDigits="0" /></td>
								<td class="text-right col-number"><fmt:formatNumber
										value="${remainingTotals.remainingVatAmount}"
										maxFractionDigits="0" /></td>
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
