<%@ page pageEncoding="UTF-8"%>

<div class="pagination" role="navigation" aria-label="一覧のページ移動">
	<span class="muted pagination-summary">全 <c:out
			value="${totalCount}" />件・1ページ <c:out value="${pageSize}" />件
	</span>
	<div class="pagination-pages">
		<c:forEach var="p" begin="1" end="${totalPages}">
			<c:url var="pageUrl" value="${basePath}">
				<c:forEach var="s" items="${search}">
					<c:if test="${s.key ne 'page'}">
						<c:param name="${s.key}" value="${s.value}" />
					</c:if>
				</c:forEach>
				<%@ include file="multi-filter-params.jspf"%>
				<c:param name="page" value="${p}" />
			</c:url>
			<a class="page-button ${page eq p ? 'is-active' : ''}"
				href="<c:out value='${pageUrl}'/>"
				${page eq p ? 'aria-current="page"' : ''}><c:out value="${p}" /></a>
		</c:forEach>
	</div>
</div>
