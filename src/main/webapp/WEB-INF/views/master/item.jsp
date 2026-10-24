<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />

<%@ include file="../common/header.jsp"%>

<section class="erp-workspace master-workspace">
    <form class="list-search"
        method="get"
        action="<c:url value='/master/item'/>">

        <input type="hidden" name="pageSize" value="50">
        <input type="hidden" name="page" value="1">
        <input type="hidden"
            name="includeInactive"
            value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">

        <div class="list-title-status">
            <h1>品目 登録</h1>

            <button
                class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
                type="button"
                data-master-inactive-toggle
                aria-pressed="${search.includeInactive eq 'Y'}">利用停止を含む</button>
        </div>

        <div class="input-group list-keyword">
            <input type="search"
                name="keyword"
                aria-label="品目登録検索"
                placeholder="入力後に［Enter］"
                value="<c:out value="${search.keyword}"/>">

            <button class="btn btn-primary" type="submit">検索</button>
        </div>
    </form>

    <div class="workspace-meta">
        <%@ include file="../common/pagination.jsp"%>
        <span class="workspace-hint">品目マスターの照会・登録を行います。</span>
    </div>

    <form id="item-selection" data-master-active-form
        data-master-api="/api/items/active"
        method="post"
        action="<c:url value='/api/items/active'/>">

        <%@ include file="../common/return-search.jspf"%>

        <input type="hidden" name="activeFlag" value="N">

        <div class="table-wrap">
            <table class="data-table workspace-table master-table item-table">
                <thead>
                    <tr>
                        <th class="col-select">
                            <input type="checkbox"
                                data-check-all="#item-selection input[name=ids]"
                                aria-label="すべて選択">
                        </th>
                        <th class="col-code">品目 コード</th>
                        <th class="col-item">品目名</th>
                        <th class="col-spec">規格</th>
                        <th class="col-unit text-center">単位</th>
                        <th class="col-number text-right">入庫単価</th>
                        <th class="col-number text-right">出庫単価</th>
                        <th>品目区分</th>
                        <th class="item-image-cell col-image text-center">画像</th>
                        <th class="note-cell">摘要</th>
                        <th class="col-active text-center">利用中</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="row" items="${itemList}">
                        <tr
                            data-record-type="item"
                            data-item-id="<c:out value="${row.itemId}"/>"
                            data-item-code="<c:out value="${row.itemCode}"/>"
                            data-item-name="<c:out value="${row.itemName}"/>"
                            data-specification="<c:out value="${row.specification}"/>"
                            data-unit="<c:out value="${row.unit}"/>"
                            data-item-type="<c:out value="${row.itemType}"/>"
                            data-inbound-price="<c:out value="${row.inboundPrice}"/>"
                            data-outbound-price="<c:out value="${row.outboundPrice}"/>"
                            data-image-path="<c:out value="${row.imagePath}"/>"
                            data-image-url="<c:out value="${row.imageUrl}"/>"
                            data-note="<c:out value="${row.note}"/>"
                            data-active-flag="<c:out value="${row.activeFlag}"/>">

                            <td class="col-select">
                                <input type="checkbox"
                                    name="ids"
                                    value="<c:out value="${row.itemId}"/>"
                                    aria-label="<c:out value="${row.itemName}"/> 選択">
                            </td>

                            <td class="col-code">
                                <a href="#"
                                    aria-haspopup="dialog"
                                    data-master-edit="item"><c:out value="${row.itemCode}"/></a>
                            </td>

                            <td class="col-item">
                                <a href="#"
                                    aria-haspopup="dialog"
                                    data-master-edit="item"><c:out value="${row.itemName}"/></a>
                            </td>

                            <td class="col-spec">
                                <c:out value="${row.specification}"/>
                            </td>

                            <td class="col-unit text-center">
                                <c:out value="${row.unit}"/>
                            </td>

                            <td class="text-right col-number">
                                <fmt:formatNumber
                                    value="${row.inboundPrice}"
                                    pattern="#,##0.##"/>
                            </td>

                            <td class="text-right col-number">
                                <fmt:formatNumber
                                    value="${row.outboundPrice}"
                                    pattern="#,##0.##"/>
                            </td>

                            <td>
                                <c:out value="${row.itemType}"/>
                            </td>

                            <td class="item-image-cell col-image text-center">
                                <c:choose>
                                    <c:when test="${not empty row.imageUrl}">
                                        <button class="btn btn-sm"
                                            type="button"
                                            aria-haspopup="dialog"
                                            data-item-image="<c:out value="${row.imageUrl}"/>"
                                            data-item-image-name="<c:out value="${row.itemName}"/>">

                                            <img
                                                src="<c:out value="${row.imageUrl}"/>&amp;thumbnail=true"
                                                alt="<c:out value="${row.itemName}"/> 代表画像"
                                                width="40"
                                                height="40">
                                        </button>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="item-image-empty">未登録</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td class="note-cell">
                                <c:out value="${row.note}"/>
                            </td>

                            <td class="col-active text-center">
                                <span class="badge">${row.activeFlag eq 'N' ? '停止' : '利用中'}</span>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty itemList}">
                        <tr>
                            <td class="empty-state" colspan="11">品目が見つかりません。</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="workspace-actions">
            <button class="btn btn-primary"
                type="button"
                data-master-new="item">新規</button>

            <button class="btn"
                type="button"
                data-stock-open>簡易在庫調整</button>

            <button class="btn"
                type="button"
                data-master-active="N"
                data-selection-form="item-selection">利用停止</button>

            <button class="btn"
                type="button"
                data-master-active="Y"
                data-selection-form="item-selection">利用再開</button>
        </div>
    </form>
</section>

<dialog class="modal master-modal"
    id="item-form-modal"
    aria-labelledby="item-form-title">

    <div class="modal-header">
        <h2 id="item-form-title" data-master-title="item">品目 登録</h2>

        <button class="btn btn-sm"
            type="button"
            data-modal-close
            aria-label="閉じる">×</button>
    </div>

    <form id="item-form" data-master-api="/api/items"
        data-master-kind="item"
        action="<c:url value='/api/items'/>"
        method="post"
        enctype="multipart/form-data">

        <%@ include file="../common/return-search.jspf"%>

        <div class="modal-body">
            <div class="form-grid editor-fields">
                <input type="hidden" name="itemId">
                <input type="hidden" name="activeFlag">

                <label class="field">
                    <span>品目 コード</span>
                    <input type="text"
                        name="itemCode"
                        maxlength="30"
                        required
                        value="<c:out value="${newItemCode}"/>"
                        placeholder="コード入力"
                        title="コードの入力・変更は新規登録時のみ可能です。">
                </label>

                <label class="field">
                    <span>品目名</span>
                    <input type="text"
                        name="itemName"
                        maxlength="100"
                        required>
                </label>

                <label class="field">
                    <span>規格</span>
                    <input type="text"
                        name="specification"
                        maxlength="100">
                </label>

                <label class="field">
                    <span>単位</span>
                    <input type="text"
                        name="unit"
                        maxlength="20">
                </label>

                <fieldset class="field field-wide">
                    <legend>品目区分</legend>

                    <div class="choice-options">
                        <label>
                            <input type="radio"
                                name="itemType"
                                value="原材料"> 原材料
                        </label>

                        <label>
                            <input type="radio"
                                name="itemType"
                                value="副資材"> 副資材
                        </label>

                        <label>
                            <input type="radio"
                                name="itemType"
                                value="製品"> 製品
                        </label>

                        <label>
                            <input type="radio"
                                name="itemType"
                                value="半製品"> 半製品
                        </label>

                        <label>
                            <input type="radio"
                                name="itemType"
                                value="商品"
                                checked> 商品
                        </label>

                        <label>
                            <input type="radio"
                                name="itemType"
                                value="無形商品"> 無形商品
                        </label>
                    </div>
                </fieldset>

                <label class="field">
                    <span>入庫単価</span>
                    <input type="number"
                        name="inboundPrice"
                        required
                        step="0.01"
                        value="0">
                </label>

                <label class="field">
                    <span>出庫単価</span>
                    <input type="number"
                        name="outboundPrice"
                        required
                        step="0.01"
                        value="0">
                </label>

                <div class="field field-wide stock-entry-field">
                    <span>在庫数量</span>

                    <div>
                        <button class="btn"
                            type="button"
                            data-stock-open="current">在庫数量入力</button>

                        <span class="muted">保存済み品目の倉庫別数量を入力します。</span>
                    </div>
                </div>

                <input type="hidden" name="imagePath">
                <input type="hidden" name="removeImage">

                <label class="field field-wide photo-picker-field">
                    <span>代表画像1枚</span>

                    <input type="file"
                        name="imageFile"
                        accept="image/jpeg,image/png,image/gif"
                        data-item-file>

                    <span class="muted">5MB以下のJPG・PNG・GIFファイルを登録できます。</span>
                </label>

                <div class="field field-wide photo-image-field">
                    <img id="item-photo-image"
                        alt="品目 代表画像プレビュー"
                        width="180"
                        height="140"
                        hidden>

                    <button class="btn btn-sm"
                        type="button"
                        data-item-remove-image>画像を削除</button>
                </div>

                <label class="field field-wide">
                    <span>摘要</span>
                    <textarea name="note"
                        rows="3"
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

<dialog class="modal stock-modal"
    id="stock-modal"
    aria-labelledby="stock-title">

    <div class="modal-header">
        <h2 id="stock-title">簡易在庫調整</h2>

        <button class="btn btn-sm"
            type="button"
            data-modal-close
            aria-label="閉じる">×</button>
    </div>

    <form id="stock-form"
        action="<c:url value='/master/item/stock'/>"
        method="post">

        <%@ include file="../common/return-search.jspf"%>

        <div class="modal-body">
            <div class="form-grid editor-fields">
                <div class="field field-wide">
                    <span>品目</span>

                    <div class="input-group">
                        <input type="hidden" name="itemId">

                        <input type="text"
                            name="itemName"
                            readonly
                            aria-label="在庫調整品目">

                        <button class="btn"
                            type="button"
                            data-stock-pick-item>品目 選択</button>
                    </div>
                </div>

                <label class="field">
                    <span>倉庫</span>

                    <select name="warehouseId" required>
                        <option value="">選択</option>

                        <c:forEach var="warehouse" items="${warehouseOptions}">
                            <option value="<c:out value="${warehouse.warehouseId}"/>"><c:out value="${warehouse.warehouseCode}"/> · <c:out value="${warehouse.warehouseName}"/></option>
                        </c:forEach>
                    </select>
                </label>

                <div class="field">
                    <span>現在庫</span>
                    <output id="stock-current">—</output>
                </div>

                <label class="field">
                    <span>入力数量</span>
                    <input type="number"
                        name="quantity"
                        step="0.001"
                        required>
                </label>
            </div>

            <p class="muted">保存すると、選択した倉庫の現在庫が入力数量に変更されます。</p>
        </div>

        <div class="modal-footer">
            <button class="btn btn-primary" type="submit">保存</button>
            <button class="btn" type="button" data-modal-close>閉じる</button>
        </div>
    </form>
</dialog>

<div hidden id="stock-data">
    <c:forEach var="stock" items="${stockList}">
        <span
            data-stock-item-id="<c:out value="${stock.itemId}"/>"
            data-stock-warehouse-id="<c:out value="${stock.warehouseId}"/>"
            data-stock-quantity="<c:out value="${stock.quantity}"/>"></span>
    </c:forEach>
</div>

<dialog class="modal image-modal"
    id="item-image-modal"
    aria-labelledby="item-image-title">

    <div class="modal-header">
        <h2 id="item-image-title">品目 画像</h2>

        <button class="btn btn-sm"
            type="button"
            data-modal-close
            aria-label="閉じる">×</button>
    </div>

    <div class="modal-body">
        <img id="item-image-full" alt="品目 代表画像">
    </div>
</dialog>

<c:set var="pageScript" value="master.js" />

<%@ include file="../common/footer.jsp"%>
