<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />
<fmt:message key="shoppingcart.shoppingcart" var="msg_shoppingcart"/>
<fmt:message key="shoppingcart.ordertotal" var="msg_ordertotal"/>

<c:url value="/order/" var="doOrderURL">
	<c:param name="action" value="order" />
</c:url>

<script type="text/javascript">
<!--
function showWaitScreen()
{
	document.getElementById('toShowDuringOrder').style.display='block';
	document.getElementById('toHideDuringOrder').style.display='none';
}
-->
</script>

<div id="toHideDuringOrder">
	<div class="overview">
		<div class="shoppingCart">
			<jsp:include flush="true" page="/jsp-shoppingcart/shoppingCartContent.jsp"></jsp:include>
		</div>
	</div>
	
	<div class="order">
		<a href="${doOrderURL}" onclick="showWaitScreen();"><fmt:message key="shoppingcart.confirm"/></a>
	</div>
</div>

<div id="toShowDuringOrder" style="display: none">
	<div class="wait">Please wait...</div>
</div>