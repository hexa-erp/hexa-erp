<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 更新時は伝票・明細・元伝票明細のIDを一緒に送信する。コードは表示・検索に使う。 --%>

<c:set var="pageTitle" value="売上入力" />
<c:set var="activeMenu" value="sale" />

<%@ include file="../common/header.jsp"%>

<c:url var="documentListUrl" value="/sale/list">
    <%@ include file="../common/list-return-params.jspf"%>
</c:url>

<div class="erp-editor">
    <div class="page-heading editor-heading">
        <div>
            <h1>
				売上
                <c:choose>
                    <c:when test="${not empty form.saleId}">編集</c:when>
                    <c:otherwise>入力</c:otherwise>
                </c:choose>
            </h1>

            <p class="muted">直接入力するか、受注伝票の明細品目を取り込めます。</p>
        </div>

        <a class="btn"
            href="<c:out value='${documentListUrl}'/>">一覧</a>
    </div>

    <form id="document-form"
        data-document-form
        data-api-document="sale" data-api-base="/api/sales"
        data-api-id-field="saleId" data-api-view-base="/sale"
        method="post"
        action="${ctx}/api/sales">

        <%@ include file="../common/return-search.jspf"%>

        <input type="hidden"
            name="saleId"
            value="<c:out value="${form.saleId}"/>" />

        <input type="hidden"
            name="saleNo"
            value="<c:out value="${form.saleNo}"/>" />

        <input type="hidden"
            name="progressStatus"
            value="<c:out value="${form.progressStatus}"/>" />

        <div class="form-panel form-grid editor-fields">
            <label class="field">
                <span>日付 <span class="required">*</span></span>

                <input type="date"
                    name="businessDate"
                    value="<c:out value="${form.businessDate}"/>"
                    required />
            </label>

            <label class="field">
                <span>売上 番号</span>

                <input
                    value="<c:out value="${form.saleNo}"/>"
                    placeholder="保存時に採番"
                    readonly
                    aria-label="伝票番号" />
            </label>

            <div class="field reference-field">
                <span id="partner-label">取引先</span>

                <div class="input-group reference-picker"
                    data-selector-field
                    data-code-selector="partner"
                    role="group"
                    aria-labelledby="partner-label">

                    <input type="hidden"
                        name="partnerId"
                        value="<c:out value="${form.partnerId}"/>" />

                    <input type="text"
                        id="partner-code"
                        class="reference-code"
                        data-reference-code="partnerCode"
                        value="<c:out value="${form.partnerCode}"/>"
                        maxlength="30"
                        placeholder="取引先 コード"
                        aria-label="取引先 コード"
                        autocomplete="off"
                        spellcheck="false"
                        title="コード入力後にEnterキーまたは検索アイコンで検索" />

                    <button class="btn reference-search"
                        type="button"
                        data-pick="partner"
                        aria-label="取引先 選択"
                        title="取引先 検索">

                        <svg viewBox="0 0 20 20"
                            width="16"
                            height="16"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="1.7"
                            aria-hidden="true"
                            focusable="false">

                            <circle cx="8.5" cy="8.5" r="5.25" />
                            <path d="M12.5 12.5L17 17" />
                        </svg>
                    </button>

                    <input type="text"
                        class="reference-name"
                        name="partnerName"
                        value="<c:out value="${form.partnerName}"/>"
                        maxlength="100"
                        placeholder="取引先名"
                        aria-label="取引先名"
                        readonly />
                </div>
            </div>

            <div class="field reference-field">
                <span id="assignee-label">担当者</span>

                <div class="input-group reference-picker"
                    data-selector-field
                    data-code-selector="assignee"
                    role="group"
                    aria-labelledby="assignee-label">

                    <input type="hidden"
                        name="assigneeId"
                        value="<c:out value="${form.assigneeId}"/>" />

                    <input type="text"
                        id="assignee-code"
                        class="reference-code"
                        data-reference-code="assigneeCode"
                        value="<c:out value="${form.assigneeCode}"/>"
                        maxlength="30"
                        placeholder="担当者 コード"
                        aria-label="担当者 コード"
                        autocomplete="off"
                        spellcheck="false"
                        title="コード入力後にEnterキーまたは検索アイコンで検索" />

                    <button class="btn reference-search"
                        type="button"
                        data-pick="assignee"
                        aria-label="担当者 選択"
                        title="担当者 検索">

                        <svg viewBox="0 0 20 20"
                            width="16"
                            height="16"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="1.7"
                            aria-hidden="true"
                            focusable="false">

                            <circle cx="8.5" cy="8.5" r="5.25" />
                            <path d="M12.5 12.5L17 17" />
                        </svg>
                    </button>

                    <input type="text"
                        class="reference-name"
                        name="assigneeName"
                        value="<c:out value="${form.assigneeName}"/>"
                        maxlength="100"
                        placeholder="担当者名"
                        aria-label="担当者名"
                        readonly />
                </div>
            </div>

            <div class="field reference-field">
                <span id="warehouse-label">出荷倉庫 <span class="required">*</span></span>

                <div class="input-group reference-picker"
                    data-selector-field
                    data-code-selector="warehouse"
                    role="group"
                    aria-labelledby="warehouse-label">

                    <input type="hidden"
                        name="warehouseId"
                        value="<c:out value="${form.warehouseId}"/>" />

                    <input type="text"
                        id="warehouse-code"
                        class="reference-code"
                        data-reference-code="warehouseCode"
                        value="<c:out value="${form.warehouseCode}"/>"
                        maxlength="30"
                        placeholder="倉庫 コード"
                        aria-label="倉庫 コード"
                        autocomplete="off"
                        spellcheck="false"
                        title="コード入力後にEnterキーまたは検索アイコンで検索" />

                    <button class="btn reference-search"
                        type="button"
                        data-pick="warehouse"
                        aria-label="出荷倉庫 選択"
                        title="出荷倉庫 検索">

                        <svg viewBox="0 0 20 20"
                            width="16"
                            height="16"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="1.7"
                            aria-hidden="true"
                            focusable="false">

                            <circle cx="8.5" cy="8.5" r="5.25" />
                            <path d="M12.5 12.5L17 17" />
                        </svg>
                    </button>

                    <input type="text"
                        class="reference-name"
                        name="warehouseName"
                        value="<c:out value="${form.warehouseName}"/>"
                        maxlength="100"
                        placeholder="倉庫名"
                        aria-label="倉庫名"
                        readonly
                        required />
                </div>
            </div>

            <label class="field field-wide">
                <span>備考</span>

                <textarea name="note"
                    maxlength="500"
                    rows="2"><c:out value="${form.note}" /></textarea>
            </label>
        </div>

        <div class="toolbar editor-source-tools">
            <button class="btn"
                type="button"
                data-source="order">受注伝票 取り込み</button>

            <span class="muted">必要な明細品目を選択して取り込んでください。</span>
        </div>

        <c:set var="lineIdField" value="saleLineId" />
        <c:set var="sourceIdField" value="salesOrderLineId" />
        <c:set var="priced" value="true" />

        <%@ include file="../common/lines.jsp"%>

        <div class="toolbar editor-actions">
            <button class="btn btn-primary"
                type="submit">保存</button>

            <button class="btn"
                type="reset">リセット</button>

            <a class="btn"
                href="<c:out value='${documentListUrl}'/>">一覧</a>

            <c:if test="${not empty form.saleId}">
                <a class="btn"
                    href="${ctx}/sale/statement?id=<c:out value="${form.saleId}"/>">納品書 照会</a>
            </c:if>

            <c:if test="${not empty form.saleId}">
                <button class="btn"
                    type="button"
                    data-notice-action="最終更新日時: <c:out value='${form.updatedAt}' default='-'/>">更新履歴</button>
            </c:if>
        </div>
    </form>
</div>

<%@ include file="../common/footer.jsp"%>
