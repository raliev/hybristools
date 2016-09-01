<%@page language="java"%>
<%@page import="java.io.*"%>
<%@page import="java.util.Enumeration"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="de.hybris.platform.jalo.JaloSession"%>
<%@page import="java.net.URLDecoder"%>
<%--
 [y] hybris Platform
 
 Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 All rights reserved.
 
 This software is the confidential and proprietary information of SAP
 ("Confidential Information"). You shall not disclose such Confidential
 Information and shall use it only in accordance with the terms of the
 license agreement you entered into with SAP.
--%>

<%

String osName = System.getProperty("os.name");
String s = null;

String aspell_prog = (String)JaloSession.getCurrentSession().getAttribute("cockpit.aspell_prog");
String langiso = (String)JaloSession.getCurrentSession().getAttribute("cockpit.current_editor_language");

String aspell_opts = "-a --lang=" + langiso + " --encoding=utf-8 -H --rem-sgml-check=alt";
String spellercss = "../spellerStyle.css";
String word_win_src = "../wordWindow.js";

String input_separator = "A";
String bodyclass = "normal";
%>



<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css" href="<%=spellercss %>" />
<script language="javascript" src="<%=word_win_src %>"></script>
<script language="javascript">
var suggs = new Array();
var words = new Array();
var textinputs = new Array();
var error;

<%
//get parameter
String textinputs = request.getParameter("textinputs[]");
final int ONE_MEGABYTE = 1 << 20;
textinputs = textinputs.substring(0, Math.min(ONE_MEGABYTE, textinputs.length()));
out.write("textinputs[0] = decodeURIComponent(\"" + StringEscapeUtils.escapeEcmaScript(textinputs) + "\");\n");
textinputs = URLDecoder.decode(textinputs,"UTF-8");

//System.out.println("\n\n###################################\nTEXTINPUTS: " + textinputs);

//get temporary servlet directory
ServletContext context=request.getSession().getServletContext();
File tmpdir=(File)context.getAttribute("javax.servlet.context.tempdir");
String tmpfile="aspell_data_";

try
{
	PrintWriter printout = new PrintWriter (new BufferedWriter (new FileWriter (tmpdir.getPath() + File.separator + tmpfile)));
	printout.print (textinputs);
	printout.flush ();
	printout.close ();
}
catch (IOException e)
{
	out.write("Cannot Create File Temporary " + e.toString()) ;
}

try
{
	String shell = null;
	String execFlag = null;
	String outputFile = new File(tmpdir, tmpfile).getPath();

	Process p1 = null;
	String shellopts = " < \"" + outputFile + "\" 2>&1";
	if(osName.toLowerCase().contains("win"))
	{
		shell = "cmd.exe";
		execFlag ="/c";
	}
	else //assuming that there is sh interpreter
	{
		shell = "sh";
		execFlag ="-c";
		aspell_opts += shellopts;
		shellopts = "";
	}
	File aspell_exe = new File(aspell_prog);
	if(aspell_exe.isAbsolute())
	{
		final ProcessBuilder pb = new ProcessBuilder(shell, execFlag, aspell_exe.getName() + " " + aspell_opts, shellopts);
		if (aspell_exe.getParentFile() != null)
			pb.directory(aspell_exe.getParentFile());
		pb.redirectErrorStream(true);
		p1 = pb.start();

		try
		{
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			int index = 0;
			int text_input_idx = -1;
			int line=0;

			while ((s = stdInput.readLine()) != null)
			{
				if (!s.startsWith("@") && s.length() > 0)
				{
					if (s.startsWith("&") || s.startsWith("#"))
					{
						if (text_input_idx == -1)
						{
							text_input_idx++;
							out.write("words[" + text_input_idx + "] = [];\n");
							out.write("suggs[" + text_input_idx + "] = [];\n");
						}

						String word[] = s.replace("\'", "").split(":");
						String wordleft[] = word[0].split(" ");

						out.write ("words[" + text_input_idx + "][" + index + "] = '" + wordleft[1].replace("\'", "\\'") + "';\n");
						if ( word.length > 1)
						{
							String suggs[] = word[1].split(", ");
							out.write("suggs[" + text_input_idx + "][" + index + "] = [");

							for (int i=0; i< suggs.length ; i++)
							{
								out.write( "'" + suggs[i].trim().replace("\'","") + "'");
								if (i < suggs.length-1 )
								{
									out.write(",");
								}
							}
							out.write("];\n");
						}
						index++;
					}
					line++;
				}
			}
		}
		catch(final Throwable t)
		{
			Logger.getLogger(this.getClass()).debug(t.getMessage(), t);
		}

		final int exitValue = SystemTools.waitForProcess(p1, 50000);

		if (exitValue != 0)
		{
			Logger.getLogger(this.getClass()).warn("ERROR in spellchecker.jsp: Error executing the following command: " + shell + " " + execFlag + " " + shellopts);
			bodyclass = "error";
		}
	}
	else{

		Logger.getLogger(this.getClass()).warn("ERROR in spellchecker.jsp: Error executing the following command: " + shell + " Path to command might be not absolute!");
	}
}
catch (final Exception e)
{
	Logger.getLogger(this.getClass()).error(e.getMessage(), e);
}

%>

var wordWindowObj = new wordWindow();
wordWindowObj.originalSpellings = words;
wordWindowObj.suggestions = suggs;
wordWindowObj.textInputs = textinputs;

function init_spell() {
	// check if any error occured during server-side processing
	if( error ) {
		alert( error );
	} else {
		// call the init_spell() function in the parent frameset
		if (parent.frames.length) {
			parent.init_spell( wordWindowObj );
		} else {
			alert('This page was loaded outside of a frameset. It might not display properly');
		}
	}
}

</script>

</head>
<!-- <body onLoad="init_spell();"> by FredCK -->
<body onLoad="init_spell();" bgcolor="#ffffff" class="<%=bodyclass %>">

<script type="text/javascript">

wordWindowObj.writeBody();
</script>
</body>

<%@page import="de.hybris.platform.cockpit.util.SystemTools"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
</html>
