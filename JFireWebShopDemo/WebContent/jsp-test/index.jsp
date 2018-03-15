<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Properties"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeSet"%>
<html>
<body>

<b>Application attributes</b>
<pre>
<%
Enumeration names = application.getAttributeNames();
while(names.hasMoreElements()) {
	String name = (String)names.nextElement();
%>
<%=name %> -&gt; <%=application.getAttribute(name) %>
<%	
}
%>
</pre>

<b>Application init parameters</b>
<pre>
<%
names = application.getInitParameterNames();
while(names.hasMoreElements()) {
	String name = (String)names.nextElement();
%>
<%=name %> -&gt; <%=application.getInitParameter(name) %>
<%	
}
%>
</pre>

<b>System properties</b>
<pre>
<%
Properties p = System.getProperties();
TreeSet x = new TreeSet(p.keySet());
for (Iterator iter = x.iterator(); iter.hasNext();) {
	String key = (String) iter.next();
%>
<%=key %> -&gt; <%=System.getProperty((String)key) %>
<%
}
%>
</pre>

<b>System environment</b>
<pre>
<%
Map env = System.getenv();
x = new TreeSet(env.keySet());
for (Iterator iter = x.iterator(); iter.hasNext();) {
	String key = (String) iter.next();
%>
<%=key %> -&gt; <%=System.getenv(key) %>
<%
}
%>
</pre>

</body>
</html>