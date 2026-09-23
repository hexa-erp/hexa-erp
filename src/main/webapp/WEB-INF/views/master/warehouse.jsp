<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace master-workspace">
	<form class="list-search" method="get"
		action="<c:url value='/master/warehouse'/>">
		<input type="hidden" name="pageSize" value="50"> <input
			type="hidden" name="page" value="1"> <input type="hidden"
			name="includeInactive"
			value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">
		<div class="list-title-status">
			<h1>창고 등록</h1>
			<button
				class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
				type="button" data-master-inactive-toggle
				aria-pressed="${search.includeInactive eq 'Y'}">사용중단 포함</button>
		</div>
		<div class="input-group list-keyword">
			<input type="search" name="keyword" aria-label="창고 등록 검색"
				placeholder="입력 후 [Enter]"
				value="<c:out value="${search.keyword}"/>">
			<button class="btn btn-primary" type="submit">검색</button>
		</div>
	</form>
	<div class="workspace-meta">
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">코드순 · 창고 등록 정보를 조회하고 등록합니다.</span>
	</div>
	<form id="warehouse-selection" method="post"
		action="<c:url value='/master/warehouse/active'/>" data-unimplemented-submit>
		<input type="hidden" name="activeFlag" value="N">
		<div class="table-wrap">
			<table
				class="data-table workspace-table master-table warehouse-table">
				<thead>
					<tr>
						<th class="col-select"><input type="checkbox"
							data-check-all="#warehouse-selection input[name=ids]"
							aria-label="전체 선택"></th>
						<th class="col-code">창고 코드</th>
						<th>창고명</th>
						<th>구분</th>
						<th class="col-active text-center">사용</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${warehouseList}">
						<tr data-record-type="warehouse"
							data-warehouse-id="<c:out value="${row.warehouseId}"/>"
							data-warehouse-code="<c:out value="${row.warehouseCode}"/>"
							data-warehouse-name="<c:out value="${row.warehouseName}"/>"
							data-warehouse-type="<c:out value="${row.warehouseType}"/>"
							data-active-flag="<c:out value="${row.activeFlag}"/>">
							<td class="col-select"><input type="checkbox" name="ids"
								value="<c:out value="${row.warehouseId}"/>"
								aria-label="<c:out value="${row.warehouseName}"/> 선택"></td>
							<td class="col-code"><a href="#" aria-haspopup="dialog"
								data-master-edit="warehouse"><c:out
										value="${row.warehouseCode}" /></a></td>
							<td><a href="#" aria-haspopup="dialog"
								data-master-edit="warehouse"><c:out
										value="${row.warehouseName}" /></a></td>
							<td><c:out value="${row.warehouseType}" /></td>
							<td class="col-active text-center"><span class="badge">${row.activeFlag eq 'N' ? '중단' : '사용'}</span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty warehouseList}">
						<tr>
							<td class="empty-state" colspan="5">조회된 창고가 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<div class="workspace-actions">
			<button class="btn btn-primary" type="button"
				data-master-new="warehouse">신규</button>
			<button class="btn" type="button" data-master-active="N"
				data-selection-form="warehouse-selection">사용중단</button>
			<button class="btn" type="button" data-master-active="Y"
				data-selection-form="warehouse-selection">재사용</button>

		</div>
	</form>
</section>
<dialog class="modal master-modal" id="warehouse-form-modal"
	aria-labelledby="warehouse-form-title">
<div class="modal-header">
	<h2 id="warehouse-form-title" data-master-title="warehouse">창고 등록</h2>
	<button class="btn btn-sm" type="button" data-modal-close
		aria-label="닫기">×</button>
</div>
<form id="warehouse-form"
	action="<c:url value='/master/warehouse/save'/>" method="post"
	data-unimplemented-submit>
	<div class="modal-body">
		<div class="form-grid editor-fields">
			<input type="hidden" name="warehouseId"><input type="hidden"
				name="activeFlag"> <label class="field"><span>창고
					코드</span><input type="text" name="warehouseCode" maxlength="30"
				value="<c:out value="${newWarehouseCode}"/>" placeholder="코드 입력"
				title="신규 등록 시에만 코드를 입력하거나 수정할 수 있습니다."></label> <label
				class="field"><span>창고명</span><input type="text"
				name="warehouseName" maxlength="100" required></label>
			<fieldset class="field field-wide">
				<legend>구분</legend>
				<div class="choice-options">
					<label><input type="radio" name="warehouseType" value="창고"
						checked> 창고</label><label><input type="radio"
						name="warehouseType" value="공장"> 공장</label>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn btn-primary" type="submit">저장</button>
		<button class="btn" type="reset">다시 작성</button>
		<button class="btn" type="button" data-modal-close>닫기</button>
	</div>
</form>
</dialog>
<c:set var="pageScript" value="master.js" />
<%@ include file="../common/footer.jsp"%>
