<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>

<style>
.highlight {background: yellow;}
</style>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>News Aggregator</title>
</head>
<body>
	<form action="SearchServlet" method="get">
		<input type="text" name="query" /><br />
		Synonymize: <input type="checkbox" name="isSynonym" /><br />
		<input type="submit" />
	</form>

<c:if test="${not empty results.results}">
Query: ${query} <br />
Results: ${resultsNum}<br />
<c:forEach var="result" items="${results.results}">
<br/><br/>
<b>${result.title}</b><br />
${result.date}<br />
<a href="${result.link}">link</a><br />
<c:if test="${not empty result.image}"><img src="${result.image}" /></c:if><br />
${result.contents}<br />
</c:forEach>
</c:if>
</body>
</html>