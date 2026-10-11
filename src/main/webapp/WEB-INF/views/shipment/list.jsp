<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 진행상태 탭과 검색어로 조회하고, 선택한 전표의 진행상태 변경·삭제를 한 form에서 처리한다. --%>

<c:set var="pageTitle" value="출하 조회" />
<c:set var="activeMenu" value="shipment" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace document-workspace">
	<form method="get" action="${ctx}/shipment/list" class="list-search"
		data-status-search>
		<%-- 진행상태 탭을 누르면 이 값을 바꾸고 1페이지부터 다시 조회한다. --%>
		<input type="hidden" name="progressStatus"
			value="<c:out value='${search.progressStatus}'/>" /> <input
			type="hidden" name="pageSize" value="25" />
		<div class="list-title-status">
			<h1>출하 조회</h1>
			<div class="tabs" role="group" aria-label="진행상태 검색">
				<button
					class="btn ${search.progressStatus eq '' ? 'btn-primary' : ''}"
					type="button" data-search-status=""
					aria-pressed="${search.progressStatus eq '' ? 'true' : 'false'}">전체</button>
				<button
					class="btn ${search.progressStatus eq 'UNCONFIRMED' ? 'btn-primary' : ''}"
					type="button" data-search-status="UNCONFIRMED"
					aria-pressed="${search.progressStatus eq 'UNCONFIRMED' ? 'true' : 'false'}">미확인</button>
				<button
					class="btn ${search.progressStatus eq 'CONFIRMED' ? 'btn-primary' : ''}"
					type="button" data-search-status="CONFIRMED"
					aria-pressed="${search.progressStatus eq 'CONFIRMED' ? 'true' : 'false'}">확인</button>
			</div>
		</div>

		<label class="input-group list-keyword"><span class="sr-only">검색어</span><input
			type="search" name="keyword"
			value="<c:out value="${search.keyword}"/>" placeholder="입력 후 [Enter]" />
			<button class="btn btn-primary" type="submit">검색</button></label>
	</form>
	<div class="workspace-meta">
		<c:set var="basePath" value="/shipment/list" />
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">일자·번호순 · 품목명으로 검색할 수 있습니다.</span>
	</div>
	<form id="document-list-actions" method="post"
		action="${ctx}/shipment/change-status">
		<%-- 처리 후 같은 검색조건의 목록으로 돌아가도록 현재 검색조건을 return.* 로 함께 보낸다. --%>
		<%@ include file="../common/return-search.jspf"%>
		<div class="table-wrap">
			<table class="data-table workspace-table document-table">
				<thead>
					<tr>
						<th class="check-cell col-select"><input type="checkbox"
							data-check-all="#document-list-actions input[name=selectedIds]"
							aria-label="현재 페이지 전체 선택" /></th>
						<th class="col-date">일자-No.</th>
						<th>창고명</th>
						<th class="col-item">품목명(요약)</th>
						<th class="text-right col-number">수량 합계</th>
						<th class="col-status text-center">진행상태</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${shipmentList}">
						<tr>
							<td class="col-select"><input type="checkbox"
								name="selectedIds" value="<c:out value="${row.shipmentId}"/>"
								aria-label="전표 선택" /></td>
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
										<c:when test="${row.progressStatus eq 'UNCONFIRMED'}">미확인</c:when>
										<c:when test="${row.progressStatus eq 'CONFIRMED'}">확인</c:when>
										<c:otherwise>
											<c:out value="${row.progressStatus}" />
										</c:otherwise>
									</c:choose></span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty shipmentList}">
						<tr>
							<td colspan="6" class="empty-state">조회된 전표가 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<div class="workspace-actions">
			<a class="btn btn-primary" href="${ctx}/shipment/form">신규</a>
			<div class="action-dropdown">
				<button class="btn" type="button" data-status-toggle
					aria-expanded="false" aria-controls="status-options">
					진행상태 변경 <span aria-hidden="true">▾</span>
				</button>
				<div class="status-options" id="status-options" role="group"
					aria-label="변경할 진행상태" hidden>
					<button class="status-option" type="button"
						data-status-value="UNCONFIRMED">미확인</button>
					<button class="status-option" type="button"
						data-status-value="CONFIRMED">확인</button>
				</div>
			</div>
			<%-- 상태 옵션을 고르면 nextProgressStatus에 값을 넣고 숨은 상태 적용 버튼으로 전송한다. --%>
			<input type="hidden" name="nextProgressStatus" value="" />
			<button type="submit" name="operation" value="status"
				data-status-submit hidden>상태 적용</button>
			<%-- 삭제는 formaction으로 전송 주소만 /shipment/delete로 바꾼다. --%>
			<button class="btn btn-danger" type="submit" name="operation"
				value="delete" data-confirm-delete
				formaction="${ctx}/shipment/delete">선택 삭제</button>
		</div>
	</form>
</section>
<%@ include file="../common/footer.jsp"%>