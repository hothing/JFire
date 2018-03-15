<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.customerregistration" />

<script type="text/javascript">
	function setRedBorder(name) {
		document.getElementsByName(name)[0].style.border= "1px solid red";
	} 
</script>

<div class="loginregister">
	<div class="content">
		
		<!--  the loginBox -->
		<jsp:include flush="true" page="loginBox.jsp"></jsp:include>
		
		<div class="or">&ndash; <fmt:message key="customer.or" /> &ndash;</div>
		<!--  the registerBox -->
		<jsp:include flush="true" page="registerBox.jsp"></jsp:include>
	</div>
</div>
   