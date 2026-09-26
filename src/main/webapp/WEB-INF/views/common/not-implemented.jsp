<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="pageTitle" value="요청 처리 안내" />
<%@ include file="header.jsp"%>
<section class="form-panel">
	<h1>요청을 처리하지 못했습니다.</h1>
	<p>
		<c:out value="${errorMessage}" />
	</p>
	<button class="btn" type="button" data-history-back>이전 화면</button>
</section>
<%@ include file="footer.jsp"%>
