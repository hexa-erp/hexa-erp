<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="リクエスト処理の案内" />
<%@ include file="header.jsp"%>
<section class="form-panel">
	<h1>リクエストを処理できませんでした。</h1>
	<p>
		<c:out value="${errorMessage}" />
	</p>
	<button class="btn" type="button" data-history-back>前の画面</button>
</section>
<%@ include file="footer.jsp"%>
