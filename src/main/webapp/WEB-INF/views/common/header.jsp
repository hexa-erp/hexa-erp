<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="ja_JP" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<link rel="icon" type="image/png"
	href="<c:url value='/resources/images/hexa-erp-favicon.png' />">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title><c:out value="${pageTitle}" /></title>
<link rel="stylesheet" href="${ctx}/resources/css/common.css?v=5">
<link rel="stylesheet" href="${ctx}/resources/css/editor.css?v=2">
<c:if test="${not empty pageStyle}">
	<link rel="stylesheet" href="${ctx}/resources/css/${pageStyle}?v=1">
</c:if>
<c:if test="${workspacePage}">
	<link rel="stylesheet" href="${ctx}/resources/css/workspace.css?v=2">
</c:if>
<c:if test="${multiFilterPage}">
	<link rel="stylesheet" href="${ctx}/resources/css/multi-filter.css?v=1">
</c:if>
</head>
<body data-context-path="${ctx}">
	<a class="skip-link" href="#main">本文へ移動</a>
	<header class="site-header">
		<div class="brand" aria-label="HEXA-ERP">
			<img src="<c:url value='/resources/images/hexa-erp-logo.png' />"
				alt="HEXA-ERP">
		</div>

		<nav class="quick-nav" aria-label="伝票入力ショートカット">
			<ul class="quick-links">
				<c:forEach var="group" items="${navigation}">
					<c:if test="${group.key ne 'master'}">
						<c:forEach var="entry" items="${group.entries}">
							<c:if test="${fn:endsWith(entry.path, '/form')}">
								<li><a class="quick-link" href="${ctx}${entry.path}"
									${currentPath eq entry.path ? 'aria-current="page"' : ''}><c:out
											value="${entry.label}" /></a></li>
							</c:if>
						</c:forEach>
					</c:if>
				</c:forEach>
			</ul>
		</nav>
		<span class="header-caption"> 接続時刻: <fmt:formatDate
				value="${connectedAt}" pattern="yyyy-MM-dd HH:mm:ss"
				timeZone="Asia/Tokyo" />
		</span>
	</header>
	<%@ include file="sidebar.jspf"%>
	<main id="main" class="page-shell" tabindex="-1">
