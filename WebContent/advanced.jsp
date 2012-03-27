<html>
<head>
<meta name="Author" content="Kelvin Tan">
<title>Lucene Query Constructor Demo and Introduction</title>
<script type="text/javascript" src="script/luceneQueryConstructor.js"></script>
<script type="text/javascript" src="script/luceneQueryValidator.js"></script>
<script>
      submitForm = false // necessary for luceneQueryConstructor not to submit the form upon query construction
      function doSubmitForm(frm)
      {
        if(frm["noField-phrase-input"].value.length > 0)
          frm["noField-phrase"].value = quote(frm["noField-phrase-input"].value)
        else if(frm["noField-phrase"].value.length > 0)
          frm["noField-phrase"].value = ''
        doMakeQuery(frm.query);
      }
    </script>
</head>

<body>
	<form>
		<table width="100%" border="0" cellspacing="1" cellpadding="5">
			<tr>
				<th></th>
				<td width="25%"></td>
			<tr>
				<th><input name="noField-andModifier" value="+|0" type="hidden"><b>Find
						results</b></th>
				<td class="bodytext">With <b>all</b> of the words
				</td>
				<td class="bodytext"><input type="text" name="noField-and"
					size="25"> <select name="resultsPerPage">
						<option value="10">10 results
						<option value="20">20 results
						<option value="50" selected>50 results
				</select></td>
			</tr>
			<tr>
				<th><input name="noField-phraseModifier" value="+|+"
					type="hidden"></th>
				<td class="bodytext">With the <b>exact phrase</b></td>
				<td class="bodytext"><input type="text"
					name="noField-phrase-input" size="25"> <input type="hidden"
					name="noField-phrase"></td>
			</tr>
			<tr>
				<th><input name="noField-orModifier" value=" |+" type="hidden">
				</th>
				<td class="bodytext">With <b>at least</b> one of the words
				</td>
				<td class="bodytext"><input type="text" name="noField-or"
					size="25"></td>
			</tr>
			<tr>
				<th><input name="noField-notModifier" value="-|0" type="hidden">
				</th>
				<td class="bodytext"><b>Without</b> the words</td>
				<td class="bodytext"><input type="text" name="noField-not"
					size="25"></td>
			</tr>
			<tr>
				<th><b>File Format</b></th>
				<td class="bodytext"><select name="fileNameModifier"><option
							value="And" selected>Only</option>
						<option value="Not">Don't</option></select> return results of the file
					format</td>
				<td class="bodytext"><select name="fileName"><option
							value="" selected>any format
						<option value="pdf">Adobe Acrobat PDF (.pdf)
						<option value="doc">Microsoft Word (.doc)
						<option value="xls">Microsoft Excel (.xls)
						<option value="ppt">Microsoft Powerpoint (.ppt)</select></td>
			</tr>
			<tr>
				<th><b>Date</b></th>
				<td class="bodytext">Return results updated in the</td>
				<td class="bodytext"><select name="date"><option
							value="" selected>anytime
						<option value="3">past 3 months
						<option value="6">past 6 months
						<option value="12">past year</select> <input type="hidden"
					name="fromDate"> <input type="hidden" name="toDate">
					<input type="hidden" name="dateRangeField" value="lastModifiedDate">
				</td>
			</tr>
			<tr>

				<input type="hidden" name="query">
			<tr>
				<td>&nbsp;
			</tr>
			<tr>
				<th><p>Current Query:</th>
				<td><pre id="curQuery"></pre>
					<pre id="curQueryValid"></pre></td>
				<td><input type="button" name="Update" value="Update Query"
					onClick="doSubmitForm(this.form); document.getElementById('curQuery').innerHTML = this.form.query.value" />
					<input type="button" name="Validate" value="Validate"
					onClick="doCheckLuceneQuery(this.form.query); getElementById('curQueryValid').innerHTML = 'Query is valid'" />
				</td>
		</table>
</body>
</html>