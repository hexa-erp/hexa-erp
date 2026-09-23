<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="출하지시서 조회" />
<c:set var="activeMenu" value="shipping-instruction" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace document-workspace">
	<%-- 검색·Enter 제출에도 진행상태가 유지되도록 hidden 하나로 전송한다. --%>
	<form method="get" action="${ctx}/shipping-instruction/list"
		class="list-search" data-status-search>
		<input type="hidden" name="progressStatus"
			value="<c:out value='${search.progressStatus}'/>" /> <input
			type="hidden" name="pageSize" value="25" />
		<div class="list-title-status">
			<h1>출하지시서 조회</h1>
			<div class="tabs" role="group" aria-label="진행상태 검색">
				<button
					class="btn ${search.progressStatus eq '' ? 'btn-primary' : ''}"
					type="button" data-search-status=""
					aria-pressed="${search.progressStatus eq '' ? 'true' : 'false'}">전체</button>
				<button
					class="btn ${search.progressStatus eq 'IN_PROGRESS' ? 'btn-primary' : ''}"
					type="button" data-search-status="IN_PROGRESS"
					aria-pressed="${search.progressStatus eq 'IN_PROGRESS' ? 'true' : 'false'}">진행중</button>
				<button
					class="btn ${search.progressStatus eq 'COMPLETED' ? 'btn-primary' : ''}"
					type="button" data-search-status="COMPLETED"
					aria-pressed="${search.progressStatus eq 'COMPLETED' ? 'true' : 'false'}">완료</button>
			</div>
		</div>
		<label class="input-group list-keyword"><span class="sr-only">검색어</span><input
			type="search" name="keyword"
			value="<c:out value="${search.keyword}"/>" placeholder="입력 후 [Enter]" />
			<button class="btn btn-primary" type="submit">검색</button></label>
	</form>

	<div class="workspace-meta">
		<c:set var="basePath" value="/shipping-instruction/list" />
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">일자·번호순 · 품목명으로 검색할 수 있습니다.</span>
	</div>
	<form id="document-list-actions" method="post"
		action="${ctx}/shipping-instruction/change-status" data-unimplemented-submit>
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
					<c:forEach var="row" items="${shipInstructionList}">
						<tr>
							<td class="col-select"><input type="checkbox"
								name="selectedIds"
								value="<c:out value="${row.shipInstructionId}"/>"
								aria-label="전표 선택" /></td>
							<td class="col-date"><a
								href="${ctx}/shipping-instruction/form?id=<c:out value="${row.shipInstructionId}"/>"><c:out
										value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
										value="${row.shipInstructionNo}" /></a></td>
							<td><c:out value="${row.warehouseName}" /></td>
							<td class="col-item"><c:out value="${row.itemSummary}" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.totalQuantity}" maxFractionDigits="3" /></td>
							<td class="col-status text-center"><span class="badge"><c:choose>
										<c:when test="${row.progressStatus eq 'IN_PROGRESS'}">진행중</c:when>
										<c:when test="${row.progressStatus eq 'COMPLETED'}">완료</c:when>
										<c:otherwise>
											<c:out value="${row.progressStatus}" />
										</c:otherwise>
									</c:choose></span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty shipInstructionList}">
						<tr>
							<td colspan="6" class="empty-state">조회된 전표가 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>

		<div class="workspace-actions">
			<a class="btn btn-primary" href="${ctx}/shipping-instruction/form">신규</a>
			<div class="action-dropdown">
				<button class="btn" type="button" data-status-toggle
					aria-expanded="false" aria-controls="status-options">
					진행상태 변경 <span aria-hidden="true">▾</span>
				</button>
				<div class="status-options" id="status-options" role="group"
					aria-label="변경할 진행상태" hidden>
					<button class="status-option" type="button"
						data-status-value="IN_PROGRESS">진행중</button>
					<button class="status-option" type="button"
						data-status-value="COMPLETED">완료</button>
				</div>
			</div>
			<input type="hidden" name="nextProgressStatus" value="" />
			<button type="submit" name="operation" value="status"
				data-status-submit hidden>상태 적용</button>
			<button class="btn btn-danger" type="submit" name="operation"
				value="delete" formaction="${ctx}/shipping-instruction/delete">선택
				삭제</button>
		</div>
	</form>
</section>
<%@ include file="../common/footer.jsp"%>
