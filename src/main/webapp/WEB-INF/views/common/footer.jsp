<%@ page pageEncoding="UTF-8"%>
</main>
<div id="server-error-message" hidden>
	<c:out value="${errorMessage}" />
</div>
<dialog id="notice-modal" class="modal small-modal"
	aria-labelledby="notice-title">
<div class="modal-header">
	<h2 id="notice-title">案内</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="閉じる">×</button>
</div>
<div class="modal-body">
	<p id="notice-message"></p>
</div>
<div class="modal-footer">
	<button type="button" class="btn btn-primary" data-modal-close>確認</button>
</div>
</dialog>
<dialog id="picker-modal" class="modal" aria-labelledby="picker-title">
<div class="modal-header">
	<h2 id="picker-title">選択</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="閉じる">×</button>
</div>
<div class="modal-body">
	<form id="picker-search" class="toolbar">
		<input name="keyword" aria-label="選択一覧の検索語"
			placeholder="入力後に［Enter］">
		<button class="btn btn-primary">検索</button>
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
	<button type="button" class="btn" data-modal-close>閉じる</button>
</div>
</dialog>
<dialog id="source-modal" class="modal wide-modal"
	aria-labelledby="source-title">
<div class="modal-header">
	<h2 id="source-title">元伝票 取り込み</h2>
	<button type="button" class="icon-button" data-modal-close
		aria-label="閉じる">×</button>
</div>
<div class="modal-body">
	<form id="source-search" class="toolbar">
		<input name="keyword" aria-label="元伝票の検索語" placeholder="入力後に［Enter］"><select
			name="progressStatus" aria-label="元伝票 進捗状況"><option value="">すべて</option></select>
		<button class="btn btn-primary">検索</button>
	</form>
	<div class="table-wrap">
		<table class="data-table">
			<thead>
				<tr>
					<th>日付-No.</th>
					<th>取引先</th>
					<th>担当者</th>
					<th>品目</th>
					<th id="source-due-head" hidden>納期日</th>
					<th id="source-amount-head">金額合計</th>
					<th>進捗状況</th>
					<th>明細</th>
				</tr>
			</thead>
			<tbody id="source-body"></tbody>
		</table>
	</div>
	<div id="source-pages" class="pagination"></div>
	<div id="source-detail" hidden>
		<h3>取込品目の選択</h3>
		<div class="table-wrap">
			<table class="data-table">
				<thead>
					<tr>
						<th><input type="checkbox"
							data-check-all="#source-lines input[type=checkbox]"
							aria-label="元伝票明細をすべて選択"></th>
						<th>品目コード</th>
						<th>品目名</th>
						<th>規格</th>
						<th>数量</th>
						<th>適用可能数量</th>
					</tr>
				</thead>
				<tbody id="source-lines"></tbody>
			</table>
		</div>
	</div>
</div>
<div class="modal-footer">
	<button type="button" class="btn btn-primary" id="source-apply">残数量を適用</button>
	<button type="button" class="btn" data-modal-close>閉じる</button>
</div>
</dialog>
<c:if test="${multiFilterPage}">
	<%@ include file="multi-filter-modal.jspf"%>
</c:if>
<%-- 共通スクリプトで使うjQueryを先に1回だけ読み込む。 --%>
<%-- JSを変更したら?v=の番号を更新し、ブラウザーが古いキャッシュではなく新しいファイルを取得するようにする。 --%>
<script src="${ctx}/resources/js/vendor/jquery-3.7.1.min.js"></script>
<script src="${ctx}/resources/js/decimal.js?v=3"></script>
<script src="${ctx}/resources/js/common.js?v=4"></script>
<script src="${ctx}/resources/js/layout.js?v=1"></script>
<script src="${ctx}/resources/js/reference-selector.js?v=3"></script>
<script src="${ctx}/resources/js/document.js?v=3"></script>
<c:if test="${not empty pageScript}">
	<script src="${ctx}/resources/js/${pageScript}?v=5"></script>
</c:if>
<c:if test="${multiFilterPage}">
	<script src="${ctx}/resources/js/multi-filter.js?v=2"></script>
</c:if>
</body>
</html>
