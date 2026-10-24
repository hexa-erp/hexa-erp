<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />

<%@ include file="../common/header.jsp"%>

<section class="erp-workspace master-workspace">
    <form class="list-search" method="get"
        action="<c:url value='/master/partner'/>">

        <input type="hidden" name="pageSize" value="50">
        <input type="hidden" name="page" value="1">
        <input type="hidden" name="includeInactive"
            value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">

        <div class="list-title-status">
            <h1>取引先 登録</h1>

            <button
                class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
                type="button" data-master-inactive-toggle
                aria-pressed="${search.includeInactive eq 'Y'}">利用停止を含む</button>
        </div>

        <div class="input-group list-keyword">
            <input type="search" name="keyword"
                aria-label="取引先登録検索"
                placeholder="入力後に［Enter］"
                value="<c:out value='${search.keyword}'/>">

            <button class="btn btn-primary" type="submit">検索</button>
        </div>
    </form>

    <div class="workspace-meta">
        <%@ include file="../common/pagination.jsp"%>

        <span class="workspace-hint">取引先マスターの照会・登録を行います。</span>
    </div>

    <form id="partner-selection" data-master-active-form
        data-master-api="/api/partners/active" method="post"
        action="<c:url value='/api/partners/active'/>">

        <%@ include file="../common/return-search.jspf"%>

        <input type="hidden" name="activeFlag" value="N">

        <div class="table-wrap">
            <table
                class="data-table workspace-table master-table partner-table">

                <thead>
                    <tr>
                        <th class="col-select">
                            <input type="checkbox"
                                data-check-all="#partner-selection input[name=ids]"
                                aria-label="すべて選択">
                        </th>
                        <th class="col-code">取引先 コード</th>
                        <th>取引先名</th>
                        <th class="col-person">代表者名</th>
                        <th>電話番号</th>
                        <th>Email</th>
                        <th class="note-cell">住所</th>
                        <th class="note-cell">摘要</th>
                        <th class="col-active text-center">利用中</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="row" items="${partnerList}">
                        <tr data-record-type="partner"
                            data-partner-id="<c:out value='${row.partnerId}'/>"
                            data-partner-code="<c:out value='${row.partnerCode}'/>"
                            data-partner-name="<c:out value='${row.partnerName}'/>"
                            data-business-no="<c:out value='${row.businessNo}'/>"
                            data-representative="<c:out value='${row.representative}'/>"
                            data-business-type="<c:out value='${row.businessType}'/>"
                            data-business-item="<c:out value='${row.businessItem}'/>"
                            data-phone="<c:out value='${row.phone}'/>"
                            data-mobile="<c:out value='${row.mobile}'/>"
                            data-email="<c:out value='${row.email}'/>"
                            data-postal-code="<c:out value='${row.postalCode}'/>"
                            data-address="<c:out value='${row.address}'/>"
                            data-assignee-id="<c:out value='${row.assigneeId}'/>"
                            data-assignee-name="<c:out value='${row.assigneeName}'/>"
                            data-note="<c:out value='${row.note}'/>"
                            data-active-flag="<c:out value='${row.activeFlag}'/>">

                            <td class="col-select">
                                <input type="checkbox" name="ids"
                                    value="<c:out value='${row.partnerId}'/>"
                                    aria-label="<c:out value='${row.partnerName}'/> 選択">
                            </td>

                            <td class="col-code">
                                <a href="#" aria-haspopup="dialog"
                                    data-master-edit="partner"><c:out
                                        value="${row.partnerCode}" /></a>
                            </td>

                            <td>
                                <a href="#" aria-haspopup="dialog"
                                    data-master-edit="partner"><c:out
                                        value="${row.partnerName}" /></a>
                            </td>

                            <td class="col-person">
                                <c:out value="${row.representative}" />
                            </td>

                            <td>
                                <c:out value="${row.phone}" />
                            </td>

                            <td>
                                <c:out value="${row.email}" />
                            </td>

                            <td class="note-cell">
                                <c:out value="${row.address}" />
                            </td>

                            <td class="note-cell">
                                <c:out value="${row.note}" />
                            </td>

                            <td class="col-active text-center">
                                <span class="badge">${row.activeFlag eq 'N' ? '停止' : '利用中'}</span>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty partnerList}">
                        <tr>
                            <td class="empty-state" colspan="9">取引先が見つかりません。</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="workspace-actions">
            <button class="btn btn-primary" type="button"
                data-master-new="partner">新規</button>

            <button class="btn" type="button" data-master-active="N"
                data-selection-form="partner-selection">利用停止</button>

            <button class="btn" type="button" data-master-active="Y"
                data-selection-form="partner-selection">利用再開</button>
        </div>
    </form>
</section>

<dialog class="modal master-modal" id="partner-form-modal"
    aria-labelledby="partner-form-title">

    <div class="modal-header">
        <h2 id="partner-form-title"
            data-master-title="partner">取引先 登録</h2>

        <button class="btn btn-sm" type="button" data-modal-close
            aria-label="閉じる">×</button>
    </div>

    <form id="partner-form" data-master-api="/api/partners"
        data-master-kind="partner"
        action="<c:url value='/api/partners'/>" method="post">

        <%@ include file="../common/return-search.jspf"%>

        <div class="modal-body">
            <div class="form-grid editor-fields">
                <input type="hidden" name="partnerId">
                <input type="hidden" name="activeFlag">

                <label class="field">
                    <span>取引先 コード</span>
                    <input type="text" name="partnerCode" maxlength="30"
                        required
                        value="<c:out value='${newPartnerCode}'/>"
                        placeholder="コード入力"
                        title="コードの入力・変更は新規登録時のみ可能です。">
                </label>

                <label class="field">
                    <span>商号（名称）</span>
                    <input type="text" name="partnerName" maxlength="100"
                        required>
                </label>

                <label class="field">
                    <span>法人番号</span>
                    <input type="text" name="businessNo" maxlength="20">
                </label>

                <label class="field">
                    <span>代表者名</span>
                    <input type="text" name="representative"
                        maxlength="100">
                </label>

                <label class="field">
                    <span>業種</span>
                    <input type="text" name="businessType"
                        maxlength="100">
                </label>

                <label class="field">
                    <span>取扱品目</span>
                    <input type="text" name="businessItem"
                        maxlength="100">
                </label>

                <label class="field">
                    <span>電話</span>
                    <input type="text" name="phone" maxlength="30">
                </label>

                <label class="field">
                    <span>携帯電話</span>
                    <input type="text" name="mobile" maxlength="30">
                </label>

                <label class="field">
                    <span>郵便番号</span>
                    <input type="text" name="postalCode" maxlength="20">
                </label>

                <label class="field field-wide">
                    <span>住所</span>
                    <input type="text" name="address" maxlength="300">
                </label>

                <div class="field">
                    <span>担当者</span>

                    <div class="input-group">
                        <input type="hidden" name="assigneeId">

                        <input type="text" name="assigneeName"
                            maxlength="100" aria-label="担当者名">

                        <button class="btn" type="button"
                            data-master-pick="assignee">選択</button>
                    </div>
                </div>

                <label class="field">
                    <span>Email</span>
                    <input type="email" name="email" maxlength="254">
                </label>

                <label class="field field-wide">
                    <span>摘要</span>
                    <textarea name="note" rows="3"
                        maxlength="500"></textarea>
                </label>
            </div>
        </div>

        <div class="modal-footer">
            <button class="btn btn-primary" type="submit">保存</button>
            <button class="btn" type="reset">リセット</button>
            <button class="btn" type="button" data-modal-close>閉じる</button>
        </div>
    </form>
</dialog>

<c:set var="pageScript" value="master.js" />

<%@ include file="../common/footer.jsp"%>
