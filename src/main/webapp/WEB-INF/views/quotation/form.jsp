<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 倉庫の選択は任意。更新時は伝票・明細IDを一緒に送信し、コードは表示・検索に使う。 --%>
<c:set var="pageTitle" value="見積入力" />
<c:set var="activeMenu" value="quotation" />
<%@ include file="../common/header.jsp"%>
<c:url var="documentListUrl" value="/quotation/list">
	<%@ include file="../common/list-return-params.jspf"%>
</c:url>
<div class="erp-editor">
	<div class="page-heading editor-heading">
		<div>
			<h1>
				見積書
				<c:choose>
					<c:when test="${not empty form.quotationId}">編集</c:when>
					<c:otherwise>入力</c:otherwise>
				</c:choose>
			</h1>
			<p class="muted">取引先と品目を選択し、見積内容を入力します。</p>
		</div>
		<a class="btn" href="<c:out value='${documentListUrl}'/>">一覧</a>
	</div>
	<form id="document-form" data-document-form
		data-api-document="quotation" data-api-base="/api/quotations"
		data-api-id-field="quotationId" data-api-view-base="/quotation"
		method="post" action="${ctx}/api/quotations">
		<%@ include file="../common/return-search.jspf"%>
		<input type="hidden" name="quotationId"
			value="<c:out value="${form.quotationId}"/>" /> <input type="hidden"
			name="quotationNo" value="<c:out value="${form.quotationNo}"/>" /> <input
			type="hidden" name="progressStatus"
			value="<c:out value="${form.progressStatus}"/>" />
		<div class="form-panel form-grid editor-fields">
			<label class="field"><span>日付 *</span><input type="date"
				name="businessDate" value="<c:out value="${form.businessDate}"/>"
				required /></label> <label class="field"><span>見積書 番号</span><input
				value="<c:out value="${form.quotationNo}"/>" placeholder="保存時に採番"
				readonly aria-label="伝票番号" /></label>
			<div class="field reference-field">
				<span id="partner-label">取引先</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="partner" role="group"
					aria-labelledby="partner-label">
					<input type="hidden" name="partnerId"
						value="<c:out value="${form.partnerId}"/>" /> <input type="text"
						id="partner-code" class="reference-code"
						data-reference-code="partnerCode"
						value="<c:out value="${form.partnerCode}"/>" maxlength="30"
						placeholder="取引先 コード" aria-label="取引先 コード" autocomplete="off"
						spellcheck="false" title="コード入力後にEnterキーまたは検索アイコンで検索" />
					<button class="btn reference-search" type="button"
						data-pick="partner" aria-label="取引先 選択" title="取引先 検索">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="partnerName" value="<c:out value="${form.partnerName}"/>"
						maxlength="100" placeholder="取引先名" aria-label="取引先名" />
				</div>
			</div>
			<div class="field reference-field">
				<span id="assignee-label">担当者</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="assignee" role="group"
					aria-labelledby="assignee-label">
					<input type="hidden" name="assigneeId"
						value="<c:out value="${form.assigneeId}"/>" /> <input type="text"
						id="assignee-code" class="reference-code"
						data-reference-code="assigneeCode"
						value="<c:out value="${form.assigneeCode}"/>" maxlength="30"
						placeholder="担当者 コード" aria-label="担当者 コード" autocomplete="off"
						spellcheck="false" title="コード入力後にEnterキーまたは検索アイコンで検索" />
					<button class="btn reference-search" type="button"
						data-pick="assignee" aria-label="担当者 選択" title="担当者 検索">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="assigneeName" value="<c:out value="${form.assigneeName}"/>"
						maxlength="100" placeholder="担当者名" aria-label="担当者名" />
				</div>
			</div>
			<div class="field reference-field">
				<span id="warehouse-label">出荷倉庫</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="warehouse" role="group"
					aria-labelledby="warehouse-label">
					<input type="hidden" name="warehouseId"
						value="<c:out value="${form.warehouseId}"/>" /> <input
						type="text" id="warehouse-code" class="reference-code"
						data-reference-code="warehouseCode"
						value="<c:out value="${form.warehouseCode}"/>" maxlength="30"
						placeholder="倉庫 コード" aria-label="倉庫 コード" autocomplete="off"
						spellcheck="false" title="コード入力後にEnterキーまたは検索アイコンで検索" />
					<button class="btn reference-search" type="button"
						data-pick="warehouse" aria-label="出荷倉庫 選択" title="出荷倉庫 検索">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="warehouseName"
						value="<c:out value="${form.warehouseName}"/>" maxlength="100"
						placeholder="倉庫名" aria-label="倉庫名" />
				</div>
			</div>
			<label class="field field-wide"><span>備考</span> <textarea
					name="note" maxlength="500" rows="2"><c:out
						value="${form.note}" /></textarea></label>
		</div>
		<c:set var="lineIdField" value="quotationLineId" />
		<c:set var="sourceIdField" value="" />
		<c:set var="priced" value="true" />
		<%@ include file="../common/lines.jsp"%>
		<div class="toolbar editor-actions">
			<button class="btn btn-primary" type="submit">保存</button>
			<button class="btn" type="reset">リセット</button>
			<a class="btn" href="<c:out value='${documentListUrl}'/>">一覧</a>
			<c:if test="${not empty form.quotationId}">
				<a class="btn"
					href="${ctx}/quotation/statement?id=<c:out value="${form.quotationId}"/>">見積書
					伝票 照会</a>
			</c:if>
			<c:if test="${not empty form.quotationId}">
				<button class="btn" type="button"
					data-notice-action="最終更新日時: <c:out value='${form.updatedAt}' default='-'/>">更新履歴</button>
			</c:if>
		</div>
	</form>
</div>
<%@ include file="../common/footer.jsp"%>
