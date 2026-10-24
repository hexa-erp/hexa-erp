<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />

<%@ include file="../common/header.jsp"%>

<section class="erp-workspace master-workspace">
    <form class="list-search" method="get"
        action="<c:url value='/master/warehouse'/>">

        <input type="hidden" name="pageSize" value="50">
        <input type="hidden" name="page" value="1">
        <input type="hidden" name="includeInactive"
            value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">

        <div class="list-title-status">
            <h1>倉庫 登録</h1>

            <button
                class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
                type="button" data-master-inactive-toggle
                aria-pressed="${search.includeInactive eq 'Y'}">利用停止を含む</button>
        </div>

        <div class="input-group list-keyword">
            <input type="search" name="keyword"
                aria-label="倉庫登録検索"
                placeholder="入力後に［Enter］"
                value="<c:out value='${search.keyword}'/>">

            <button class="btn btn-primary" type="submit">検索</button>
        </div>
    </form>

    <div class="workspace-meta">
        <%@ include file="../common/pagination.jsp"%>

        <span class="workspace-hint">倉庫マスターの照会・登録を行います。</span>
    </div>

    <form id="warehouse-selection" data-master-active-form
        data-master-api="/api/warehouses/active" method="post"
        action="<c:url value='/api/warehouses/active'/>">

        <%@ include file="../common/return-search.jspf"%>

        <input type="hidden" name="activeFlag" value="N">

        <div class="table-wrap">
            <table
                class="data-table workspace-table master-table warehouse-table">

                <thead>
                    <tr>
                        <th class="col-select">
                            <input type="checkbox"
                                data-check-all="#warehouse-selection input[name=ids]"
                                aria-label="すべて選択">
                        </th>
                        <th class="col-code">倉庫 コード</th>
                        <th>倉庫名</th>
                        <th>区分</th>
                        <th class="col-active text-center">利用中</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="row" items="${warehouseList}">
                        <tr data-record-type="warehouse"
                            data-warehouse-id="<c:out value='${row.warehouseId}'/>"
                            data-warehouse-code="<c:out value='${row.warehouseCode}'/>"
                            data-warehouse-name="<c:out value='${row.warehouseName}'/>"
                            data-warehouse-type="<c:out value='${row.warehouseType}'/>"
                            data-active-flag="<c:out value='${row.activeFlag}'/>">

                            <td class="col-select">
                                <input type="checkbox" name="ids"
                                    value="<c:out value='${row.warehouseId}'/>"
                                    aria-label="<c:out value='${row.warehouseName}'/> 選択">
                            </td>

                            <td class="col-code">
                                <a href="#" aria-haspopup="dialog"
                                    data-master-edit="warehouse"><c:out
                                        value="${row.warehouseCode}" /></a>
                            </td>

                            <td>
                                <a href="#" aria-haspopup="dialog"
                                    data-master-edit="warehouse"><c:out
                                        value="${row.warehouseName}" /></a>
                            </td>

                            <td>
                                <c:out value="${row.warehouseType}" />
                            </td>

                            <td class="col-active text-center">
                                <span class="badge">${row.activeFlag eq 'N' ? '停止' : '利用中'}</span>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty warehouseList}">
                        <tr>
                            <td class="empty-state" colspan="5">倉庫が見つかりません。</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="workspace-actions">
            <button class="btn btn-primary" type="button"
                data-master-new="warehouse">新規</button>

            <button class="btn" type="button" data-master-active="N"
                data-selection-form="warehouse-selection">利用停止</button>

            <button class="btn" type="button" data-master-active="Y"
                data-selection-form="warehouse-selection">利用再開</button>
        </div>
    </form>
</section>

<dialog class="modal master-modal" id="warehouse-form-modal"
    aria-labelledby="warehouse-form-title">

    <div class="modal-header">
        <h2 id="warehouse-form-title"
            data-master-title="warehouse">倉庫 登録</h2>

        <button class="btn btn-sm" type="button" data-modal-close
            aria-label="閉じる">×</button>
    </div>

    <form id="warehouse-form" data-master-api="/api/warehouses"
        data-master-kind="warehouse"
        action="<c:url value='/api/warehouses'/>" method="post">

        <%@ include file="../common/return-search.jspf"%>

        <div class="modal-body">
            <div class="form-grid editor-fields">
                <input type="hidden" name="warehouseId">
                <input type="hidden" name="activeFlag">

                <label class="field">
                    <span>倉庫 コード</span>
                    <input type="text" name="warehouseCode" maxlength="30"
                        required
                        value="<c:out value='${newWarehouseCode}'/>"
                        placeholder="コード入力"
                        title="コードの入力・変更は新規登録時のみ可能です。">
                </label>

                <label class="field">
                    <span>倉庫名</span>
                    <input type="text" name="warehouseName" maxlength="100"
                        required>
                </label>

                <fieldset class="field field-wide">
                    <legend>区分</legend>

                    <div class="choice-options">
                        <label>
                            <input type="radio" name="warehouseType"
                                value="倉庫" checked> 倉庫
                        </label>

                        <label>
                            <input type="radio" name="warehouseType"
                                value="工場"> 工場
                        </label>
                    </div>
                </fieldset>
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
