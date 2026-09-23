<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageStyle" value="master.css" />
<c:set var="workspacePage" value="true" />
<%@ include file="../common/header.jsp"%>
<section class="erp-workspace master-workspace">
	<form class="list-search" method="get"
		action="<c:url value='/master/partner'/>">
		<input type="hidden" name="pageSize" value="50"> <input
			type="hidden" name="page" value="1"> <input type="hidden"
			name="includeInactive"
			value="${search.includeInactive eq 'Y' ? 'Y' : 'N'}">
		<div class="list-title-status">
			<h1>거래처 등록</h1>
			<button
				class="btn ${search.includeInactive eq 'Y' ? 'btn-primary' : ''}"
				type="button" data-master-inactive-toggle
				aria-pressed="${search.includeInactive eq 'Y'}">사용중단 포함</button>
		</div>
		<div class="input-group list-keyword">
			<input type="search" name="keyword" aria-label="거래처 등록 검색"
				placeholder="입력 후 [Enter]"
				value="<c:out value="${search.keyword}"/>">
			<button class="btn btn-primary" type="submit">검색</button>
		</div>
	</form>
	<div class="workspace-meta">
		<%@ include file="../common/pagination.jsp"%>
		<span class="workspace-hint">코드순 · 거래처 등록 정보를 조회하고 등록합니다.</span>
	</div>
	<form id="partner-selection" method="post"
		action="<c:url value='/master/partner/active'/>" data-unimplemented-submit>
		<input type="hidden" name="activeFlag" value="N">
		<div class="table-wrap">
			<table class="data-table workspace-table master-table partner-table">
				<thead>
					<tr>
						<th class="col-select"><input type="checkbox"
							data-check-all="#partner-selection input[name=ids]"
							aria-label="전체 선택"></th>
						<th class="col-code">거래처 코드</th>
						<th>거래처명</th>
						<th class="col-person">대표자명</th>
						<th>전화번호</th>
						<th>Email</th>
						<th class="note-cell">주소</th>
						<th class="note-cell">적요</th>
						<th class="col-active text-center">사용</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="row" items="${partnerList}">
						<tr data-record-type="partner"
							data-partner-id="<c:out value="${row.partnerId}"/>"
							data-partner-code="<c:out value="${row.partnerCode}"/>"
							data-partner-name="<c:out value="${row.partnerName}"/>"
							data-business-no="<c:out value="${row.businessNo}"/>"
							data-representative="<c:out value="${row.representative}"/>"
							data-business-type="<c:out value="${row.businessType}"/>"
							data-business-item="<c:out value="${row.businessItem}"/>"
							data-phone="<c:out value="${row.phone}"/>"
							data-mobile="<c:out value="${row.mobile}"/>"
							data-email="<c:out value="${row.email}"/>"
							data-postal-code="<c:out value="${row.postalCode}"/>"
							data-address="<c:out value="${row.address}"/>"
							data-assignee-id="<c:out value="${row.assigneeId}"/>"
							data-assignee-name="<c:out value="${row.assigneeName}"/>"
							data-note="<c:out value="${row.note}"/>"
							data-active-flag="<c:out value="${row.activeFlag}"/>">
							<td class="col-select"><input type="checkbox" name="ids"
								value="<c:out value="${row.partnerId}"/>"
								aria-label="<c:out value="${row.partnerName}"/> 선택"></td>
							<td class="col-code"><a href="#" aria-haspopup="dialog"
								data-master-edit="partner"><c:out value="${row.partnerCode}" /></a></td>
							<td><a href="#" aria-haspopup="dialog"
								data-master-edit="partner"><c:out value="${row.partnerName}" /></a></td>
							<td class="col-person"><c:out value="${row.representative}" /></td>
							<td><c:out value="${row.phone}" /></td>
							<td><c:out value="${row.email}" /></td>
							<td class="note-cell"><c:out value="${row.address}" /></td>
							<td class="note-cell"><c:out value="${row.note}" /></td>
							<td class="col-active text-center"><span class="badge">${row.activeFlag eq 'N' ? '중단' : '사용'}</span></td>
						</tr>
					</c:forEach>
					<c:if test="${empty partnerList}">
						<tr>
							<td class="empty-state" colspan="9">조회된 거래처가 없습니다.</td>
						</tr>
					</c:if>
				</tbody>
			</table>
		</div>
		<div class="workspace-actions">
			<button class="btn btn-primary" type="button"
				data-master-new="partner">신규</button>
			<button class="btn" type="button" data-master-active="N"
				data-selection-form="partner-selection">사용중단</button>
			<button class="btn" type="button" data-master-active="Y"
				data-selection-form="partner-selection">재사용</button>

		</div>
	</form>
</section>
<dialog class="modal master-modal" id="partner-form-modal"
	aria-labelledby="partner-form-title">
<div class="modal-header">
	<h2 id="partner-form-title" data-master-title="partner">거래처 등록</h2>
	<button class="btn btn-sm" type="button" data-modal-close
		aria-label="닫기">×</button>
</div>
<form id="partner-form" action="<c:url value='/master/partner/save'/>"
	method="post" data-unimplemented-submit>
	<div class="modal-body">
		<div class="form-grid editor-fields">
			<input type="hidden" name="partnerId"><input type="hidden"
				name="activeFlag"> <label class="field"><span>거래처
					코드</span><input type="text" name="partnerCode" maxlength="30"
				value="<c:out value="${newPartnerCode}"/>" placeholder="코드 입력"
				title="신규 등록 시에만 코드를 입력하거나 수정할 수 있습니다."></label> <label
				class="field"><span>상호(이름)</span><input type="text"
				name="partnerName" maxlength="100" required></label> <label
				class="field"><span>사업자등록번호</span><input type="text"
				name="businessNo" maxlength="20"></label> <label class="field"><span>대표자명</span><input
				type="text" name="representative" maxlength="100"></label> <label
				class="field"><span>업태</span><input type="text"
				name="businessType" maxlength="100"></label> <label class="field"><span>종목</span><input
				type="text" name="businessItem" maxlength="100"></label> <label
				class="field"><span>전화</span><input type="text" name="phone"
				maxlength="30"></label> <label class="field"><span>모바일</span><input
				type="text" name="mobile" maxlength="30"></label> <label
				class="field"><span>우편번호</span><input type="text"
				name="postalCode" maxlength="20"></label> <label
				class="field field-wide"><span>주소</span><input type="text"
				name="address" maxlength="300"></label>
			<div class="field">
				<span>담당자</span>
				<div class="input-group">
					<input type="hidden" name="assigneeId"><input type="text"
						name="assigneeName" maxlength="100" aria-label="담당자 이름">
					<button class="btn" type="button" data-master-pick="assignee">선택</button>
				</div>
			</div>
			<label class="field"><span>Email</span><input type="email"
				name="email" maxlength="254"></label> <label
				class="field field-wide"><span>적요</span> <textarea
					name="note" rows="3" maxlength="500"></textarea></label>
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
