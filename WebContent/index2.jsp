<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
<head>
<title>News Aggregator</title>

<link rel="stylesheet" type="text/css" href="style.css" />

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>

<script>
	function fixQuery(q) {
		document.getElementById('query').value = q;
		document.forms["mainForm"].submit();
	}

	$(document).ready(function() {
		$(".topicToggle").click(function() {
			var numResults = 0;//parseInt($('#numResults').text());
			var x = 'cluster' + $(this).attr('id').substring(5);

			$('.innertube > div').each(function(i) {
				if ($(this).attr('class') != x) {
					$(this).fadeOut();
				} else {
					$(this).fadeIn();
					numResults += 1;
				}
			});

			$('#numResults').text(numResults);
		});

		$("#allTopics").click(function() {
			var numResults = 0;//parseInt($('#numResults').text());

			$('.innertube > div').each(function(i) {
				if ($(this).attr('class').match("cluster") != null) {
					$(this).fadeIn();
					numResults += 1;
				}
			});

			$('#numResults').text(numResults);
		});
	});
</script>

</head>
<body>
	<div id="maincontainer">
		<div id="topsection">
			<div class="innertube">
				<form name="mainForm" id="mainForm" action="SearchServlet"
					method="get">
					<input type="text" name="query" id="query" value="${query}" />
					Synonymize <input type="checkbox" name="isSynonym" id="isSynonym" />
					Cluster <input type="checkbox" name="isCluster" id="isCluster" /><br />
					<c:if test="${misspelled}">
						Did you mean:
						<a onclick="fixQuery('${newQuery}')" href="javascript:void(0);">
							<c:out value="${newQuery}" /><br />
						</a>
					</c:if>
					<input type="submit" />
				</form>
			</div>
		</div>
		<div id="contentwrapper">
			<div id="contentcolumn">
				<div class="innertube">
					<c:if test="${not empty results.results}">
					Results: <span id="numResults">${resultsNum}</span>
						<c:forEach var="result" varStatus="status"
							items="${results.results}">
							<div class="cluster${results.docToCluster[status.count - 1]}">
								<span class="title"> <a href="${result.link}">${result.title}</a>
								</span> <br /> <span class="date"> ${result.date} </span> <br />
								<span class="contents">
									<c:if test="${not empty result.image}">
										<img src="${result.image}" style="float: left; padding-right: 5px;" />
									</c:if>
									${result.contents}
									<span class="similar">
										<a href="?like=${result.id}">similar</a>
									</span>
								</span>
								<br />
								<br />
							</div>
						</c:forEach>
					</c:if>
				</div>
			</div>
		</div>

		<div id="leftcolumn">
			<div class="innertube">
				<input type="button" id="allTopics" value="All Topics" />
				<c:forEach var="cluster" varStatus="status"
					items="${results.clusters}">
					<input type="button" id="topic${status.count -1}"
						class="topicToggle" value="${cluster}" />
					<br />
				</c:forEach>
			</div>
		</div>

		<br />
		<div id="footer"></div>

	</div>
</body>
</html>