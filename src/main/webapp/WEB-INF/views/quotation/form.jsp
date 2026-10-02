<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- 창고 선택은 선택사항이다. 수정 시 전표·상세행 ID를 함께 보낸다. 코드는 표시·검색에 사용한다. --%>
<c:set var="pageTitle" value="견적서 입력" />
<c:set var="activeMenu" value="quotation" />
<%@ include file="../common/header.jsp"%>
<c:url var="documentListUrl" value="/quotation/list">
	<%@ include file="../common/list-return-params.jspf"%>
</c:url>
<div class="erp-editor">
	<div class="page-heading editor-heading">
		<div>
			<h1>
				견적서
				<c:choose>
					<c:when test="${not empty form.quotationId}">수정</c:when>
					<c:otherwise>입력</c:otherwise>
				</c:choose>
			</h1>
			<p class="muted">거래처와 품목을 선택하여 견적 내용을 입력합니다.</p>
		</div>
		<a class="btn" href="<c:out value='${documentListUrl}'/>">목록</a>
	</div>
	<form id="document-form" data-document-form
		method="post" action="${ctx}/quotation/save">
		<%@ include file="../common/return-search.jspf"%>
		<input type="hidden" name="quotationId"
			value="<c:out value="${form.quotationId}"/>" /> <input type="hidden"
			name="quotationNo" value="<c:out value="${form.quotationNo}"/>" /> <input
			type="hidden" name="progressStatus"
			value="<c:out value="${form.progressStatus}"/>" />
		<div class="form-panel form-grid editor-fields">
			<label class="field"><span>일자 *</span><input type="date"
				name="businessDate" value="<c:out value="${form.businessDate}"/>"
				required /></label> <label class="field"><span>견적서 번호</span><input
				value="<c:out value="${form.quotationNo}"/>" placeholder="저장 시 발급"
				readonly aria-label="전표번호" /></label>
			<div class="field reference-field">
				<span id="partner-label">거래처</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="partner" role="group"
					aria-labelledby="partner-label">
					<input type="hidden" name="partnerId"
						value="<c:out value="${form.partnerId}"/>" /> <input type="text"
						id="partner-code" class="reference-code"
						data-reference-code="partnerCode"
						value="<c:out value="${form.partnerCode}"/>" maxlength="30"
						placeholder="거래처 코드" aria-label="거래처 코드" autocomplete="off"
						spellcheck="false" title="코드 입력 후 Enter 또는 돋보기로 검색" />
					<button class="btn reference-search" type="button"
						data-pick="partner" aria-label="거래처 선택" title="거래처 검색">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="partnerName" value="<c:out value="${form.partnerName}"/>"
						maxlength="100" placeholder="거래처명" aria-label="거래처명" />
				</div>
			</div>
			<div class="field reference-field">
				<span id="assignee-label">담당자</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="assignee" role="group"
					aria-labelledby="assignee-label">
					<input type="hidden" name="assigneeId"
						value="<c:out value="${form.assigneeId}"/>" /> <input type="text"
						id="assignee-code" class="reference-code"
						data-reference-code="assigneeCode"
						value="<c:out value="${form.assigneeCode}"/>" maxlength="30"
						placeholder="담당자 코드" aria-label="담당자 코드" autocomplete="off"
						spellcheck="false" title="코드 입력 후 Enter 또는 돋보기로 검색" />
					<button class="btn reference-search" type="button"
						data-pick="assignee" aria-label="담당자 선택" title="담당자 검색">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="assigneeName" value="<c:out value="${form.assigneeName}"/>"
						maxlength="100" placeholder="담당자명" aria-label="담당자명" />
				</div>
			</div>
			<div class="field reference-field">
				<span id="warehouse-label">출하창고</span>
				<div class="input-group reference-picker" data-selector-field
					data-code-selector="warehouse" role="group"
					aria-labelledby="warehouse-label">
					<input type="hidden" name="warehouseId"
						value="<c:out value="${form.warehouseId}"/>" /> <input
						type="text" id="warehouse-code" class="reference-code"
						data-reference-code="warehouseCode"
						value="<c:out value="${form.warehouseCode}"/>" maxlength="30"
						placeholder="창고 코드" aria-label="창고 코드" autocomplete="off"
						spellcheck="false" title="코드 입력 후 Enter 또는 돋보기로 검색" />
					<button class="btn reference-search" type="button"
						data-pick="warehouse" aria-label="출하창고 선택" title="출하창고 검색">
						<svg viewBox="0 0 20 20" width="16" height="16" fill="none"
							stroke="currentColor" stroke-width="1.7" aria-hidden="true"
							focusable="false">
							<circle cx="8.5" cy="8.5" r="5.25" />
							<path d="M12.5 12.5L17 17" /></svg>
					</button>
					<input type="text" class="reference-name" readonly
						name="warehouseName"
						value="<c:out value="${form.warehouseName}"/>" maxlength="100"
						placeholder="창고명" aria-label="창고명" />
				</div>
			</div>
			<label class="field field-wide"><span>비고</span> <textarea
					name="note" maxlength="500" rows="2"><c:out
						value="${form.note}" /></textarea></label>
		</div>
		<c:set var="lineIdField" value="quotationLineId" />
		<c:set var="sourceIdField" value="" />
		<c:set var="priced" value="true" />
		<%@ include file="../common/lines.jsp"%>
		<div class="toolbar editor-actions">
			<button class="btn btn-primary" type="submit">저장</button>
			<button class="btn" type="reset">다시 작성</button>
			<a class="btn" href="<c:out value='${documentListUrl}'/>">목록</a>
			<c:if test="${not empty form.quotationId}">
				<a class="btn"
					href="${ctx}/quotation/statement?id=<c:out value="${form.quotationId}"/>">견적서
					전표 조회</a>
			</c:if>
			<c:if test="${not empty form.quotationId}">
				<button class="btn" type="button"
					data-notice-action="마지막으로 변경한 시각: <c:out value='${form.updatedAt}' default='-'/>">최종
					이력</button>
			</c:if>
		</div>
	</form>
</div>
<%@ include file="../common/footer.jsp"%>
