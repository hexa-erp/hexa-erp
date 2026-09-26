<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="pageTitle" value="판매 조회" />
<c:set var="activeMenu" value="sale" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace document-workspace">
	<form method="get" action="${ctx}/sale/list" class="list-search"
		data-status-search>
		<input type="hidden" name="progressStatus"
			value="<c:out value='${search.progressStatus}'/>" /> <input
			type="hidden" name="pageSize" value="25" />
		<div class="list-title-status">
			<h1>판매 조회</h1>
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
		<c:set var="basePath" value="/sale/list" />
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">일자·번호순 · 거래처명 또는 품목명으로 검색할 수 있습니다.</span>
	</div>
	<form id="document-list-actions" method="post"
		action="${ctx}/sale/change-status" data-unimplemented-submit>
		<%@ include file="../common/return-search.jspf"%>
		<div class="table-wrap">
			<table class="data-table workspace-table document-table">
				<thead>
					<tr>
						<th class="check-cell col-select"><input type="checkbox"
							data-check-all="#document-list-actions input[name=selectedIds]"
							aria-label="현재 페이지 전체 선택" /></th>
						<th class="col-date">일자-No.</th>
						<th>거래처명</th>
						<th>창고명</th>
						<th class="col-item">품목명(요약)</th>
						<th class="text-right col-number">금액 합계</th>
						<th class="col-status text-center">진행상태</th>
						<th class="col-link text-center">거래명세서</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${saleList}">
						<tr>
							<td class="col-select"><input type="checkbox"
								name="selectedIds" value="<c:out value="${row.saleId}"/>"
								aria-label="전표 선택" /></td>
							<td class="col-date"><c:url var="documentEditUrl"
									value="/sale/form">
									<c:param name="id" value="${row.saleId}" />
									<%@ include file="../common/return-search-params.jspf"%>
								</c:url> <a href="<c:out value='${documentEditUrl}'/>"><c:out
										value="${fn:replace(row.businessDate, '-', '/')}" />-<c:out
										value="${row.saleNo}" /></a></td>
							<td><c:out value="${row.partnerName}" /></td>
							<td><c:out value="${row.warehouseName}" /></td>
							<td class="col-item"><c:out value="${row.itemSummary}" /></td>
							<td class="text-right col-number"><fmt:formatNumber
									value="${row.totalAmount}" maxFractionDigits="0" /></td>
							<td class="col-status text-center"><span class="badge"><c:choose>
										<c:when test="${row.progressStatus eq 'UNCONFIRMED'}">미확인</c:when>
										<c:when test="${row.progressStatus eq 'CONFIRMED'}">확인</c:when>
										<c:otherwise>
											<c:out value="${row.progressStatus}" />
										</c:otherwise>
									</c:choose></span></td>
							<td class="col-link text-center"><a
								href="${ctx}/sale/statement?id=<c:out value="${row.saleId}"/>">조회</a></td>
						</tr>
					</c:forEach>
					<c:if test="${empty saleList}">
						<tr>
							<td colspan="8" class="empty-state">조회된 전표가 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>

		<div class="workspace-actions">
			<a class="btn btn-primary" href="${ctx}/sale/form">신규</a>
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
			<input type="hidden" name="nextProgressStatus" value="" />
			<button type="submit" name="operation" value="status"
				data-status-submit hidden>상태 적용</button>
			<button class="btn btn-danger" type="submit" name="operation"
				value="delete" data-confirm-delete formaction="${ctx}/sale/delete">선택
				삭제</button>
		</div>
	</form>
</section>
<%@ include file="../common/footer.jsp"%>
