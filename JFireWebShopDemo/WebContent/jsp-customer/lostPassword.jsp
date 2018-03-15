<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.customerregistration" />
<c:url value="/customer/" var="lostAction" >
	<c:param name="action" value="sendPassword" />
</c:url>
<div style="padding-left:10px;padding-top:10px;">
<div class="title"><h3><fmt:message key="customer.lostpasswordheader" /></h3></div>
<div><b><fmt:message key="customer.enteremail" /></b></div>

<form action="${lostAction}" method="post">
<table id="loginFormular" style="cellpadding:4px;">
	<tr>
   		 <td class="formLabel"><fmt:message key="customer.email" />:*</td>
		<td class="formInput"><input type="text" name="customerEmail"> </td>
    </tr>
    <tr> 
        <td colspan="2" class="formSubmit"><input type="submit" name="<fmt:message key="customer.requestnewpassword" />"></td>
    </tr>
</table>
</form>   
</div>