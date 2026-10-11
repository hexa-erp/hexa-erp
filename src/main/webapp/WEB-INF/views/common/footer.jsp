<%@ page pageEncoding="UTF-8"%>
</main>
<div id="server-error-message" hidden>
	<c:out value="${errorMessage}" />
</div>
<dialog id="notice-modal" class="modal small-modal"
	aria-labelledby="notice-title">
<div class="modal-header">
	<h2 id="notice-title">안내</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="닫기">×</button>
</div>
<div class="modal-body">
	<p id="notice-message"></p>
</div>
<div class="modal-footer">
	<button type="button" class="btn btn-primary" data-modal-close>확인</button>
</div>
</dialog>
<dialog id="picker-modal" class="modal" aria-labelledby="picker-title">
<div class="modal-header">
	<h2 id="picker-title">선택</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="닫기">×</button>
</div>
<div class="modal-body">
	<form id="picker-search" class="toolbar">
		<input name="keyword" aria-label="선택 목록 검색어"
			placeholder="입력 후 [Enter]">
		<button class="btn btn-primary">검색</button>
	</form>
	<div class="table-wrap">
		<table class="data-table">
			<thead id="picker-head"></thead>
			<tbody id="picker-body"></tbody>
		</table>
	</div>
	<div id="picker-pages" class="pagination"></div>
</div>
<div class="modal-footer">
	<button type="button" class="btn" data-modal-close>닫기</button>
</div>
</dialog>
<dialog id="source-modal" class="modal wide-modal"
	aria-labelledby="source-title">
<div class="modal-header">
	<h2 id="source-title">원전표 불러오기</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="닫기">×</button>
</div>
<div class="modal-body">
	<form id="source-search" class="toolbar">
		<input name="keyword" aria-label="원전표 검색어" placeholder="입력 후 [Enter]"><select
			name="progressStatus" aria-label="원전표 진행상태"><option value="">전체</option></select>
		<button class="btn btn-primary">검색</button>
	</form>
	<div class="table-wrap">
		<table class="data-table">
			<thead>
				<tr>
					<th>일자-No.</th>
					<th>거래처</th>
					<th>담당자</th>
					<th>품목</th>
					<th id="source-due-head" hidden>납기일자</th>
					<th id="source-amount-head">금액합계</th>
					<th>진행상태</th>
					<th>상세</th>
				</tr>
			</thead>
			<tbody id="source-body"></tbody>
		</table>
	</div>
	<div id="source-pages" class="pagination"></div>
	<div id="source-detail" hidden>
		<h3>불러올 품목 선택</h3>
		<div class="table-wrap">
			<table class="data-table">
				<thead>
					<tr>
						<th><input type="checkbox"
							data-check-all="#source-lines input[type=checkbox]"
							aria-label="원전표 상세 전체 선택"></th>
						<th>품목코드</th>
						<th>품목명</th>
						<th>규격</th>
						<th>수량</th>
						<th>적용 가능 수량</th>
					</tr>
				</thead>
				<tbody id="source-lines"></tbody>
			</table>
		</div>
	</div>
</div>
<div class="modal-footer">
	<button type="button" class="btn btn-primary" id="source-apply">잔량
		적용</button>
	<button type="button" class="btn" data-modal-close>닫기</button>
</div>
</dialog>
<c:if test="${multiFilterPage}">
	<%@ include file="multi-filter-modal.jspf"%>
</c:if>
<%-- 공통 스크립트가 사용하는 jQuery를 먼저 한 번만 로드한다. --%>
<%-- JS를 고치면 ?v= 번호를 올려 브라우저가 캐시된 예전 파일 대신 새 파일을 받게 한다. --%>
<script src="${ctx}/resources/js/vendor/jquery-3.7.1.min.js"></script>
<script src="${ctx}/resources/js/decimal.js?v=2"></script>
<script src="${ctx}/resources/js/common.js?v=2"></script>
<script src="${ctx}/resources/js/layout.js?v=1"></script>
<script src="${ctx}/resources/js/reference-selector.js?v=2"></script>
<script src="${ctx}/resources/js/document.js?v=2"></script>
<c:if test="${not empty pageScript}">
	<script src="${ctx}/resources/js/${pageScript}?v=3"></script>
</c:if>
<c:if test="${multiFilterPage}">
	<script src="${ctx}/resources/js/multi-filter.js?v=1"></script>
</c:if>
</body>
</html>
