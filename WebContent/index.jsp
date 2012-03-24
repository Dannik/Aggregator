<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>News Aggregator</title>
</head>
<body>
	<form action="SearchServlet" method="get">
		<input type="text" name="query" /><br />
		<input type="submit" />
	</form>

<c:if test="${not empty results.results}">
Query: ${query} <br />
Results: <br />
<c:forEach var="result" items="${results.results}">
--------------<br />
${result.title}<br />
${result.date}<br />
${result.link}<br />
${result.description}<br />
</c:forEach>
</c:if>
</body>
</html>