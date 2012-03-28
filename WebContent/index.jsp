<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>

<script>
function fixQuery(q) {
	document.getElementById('query').value = q;
	document.forms["mainForm"].submit();
}
</script>

<style>
.highlight {
	background: yellow;
}
</style>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>News Aggregator</title>
</head>
<body>
	<form name="mainForm" id="mainForm" action="SearchServlet" method="get">
		<input type="text" name="query" id="query" /><br /> Synonymize: <input
			type="checkbox" name="isSynonym" id="isSynonym" /><br /> <input type="submit" />
	</form>
	<c:if test="${misspelled}">
			Did you mean:
			<a onclick="fixQuery('${newQuery}')" href="javascript:void(0);">
				<c:out value="${newQuery}" /><br />
			</a>
	</c:if>
	<c:if test="${not empty results.results}">
Query: ${query} <br />
		<br />

Results: ${resultsNum}<br />
		<c:forEach var="result" items="${results.results}">
			<br />
			<br />
			<b>${result.title}</b>
			<br />
${result.date}<br />
			<a href="${result.link}">link</a>
			<br />
			<c:if test="${not empty result.image}">
				<img src="${result.image}" />
			</c:if>
			<br />
${result.contents}<br />
		</c:forEach>
	</c:if>
</body>
</html>