<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/shoppingcart/" var="orderURL" />
<c:url value="/customer/" var="editCustomerDataURL" >
	<c:param name="action" value="showEditDataPage" />
</c:url>

CUSTOMER DATA

<table align="center">
  <tr>
    <td>Name</td>
    <td>${customerData["firstname"]}</td>
  </tr>
  <tr>
    <td>Last Name</td>
    <td>${customerData["name"]}</td>
  </tr>
  <tr>
    <td>Address</td>
    <td>${customerData["address"]}</td>
  </tr>
  <tr>
    <td>City</td>
    <td>${customerData["city"]}</td>
  </tr>
  <tr>
    <td>Country</td>
    <td>${customerData["country"]}</td>
  </tr>
  <tr>
    <td>Post Code</td>
    <td>${customerData["postcode"]}</td>
  </tr>
  <tr>
    <td>Email</td>
    <td>${customerData["email"]}</td>
  </tr>
  <tr>
    <td>Phone Number</td>
    <td>${customerData["phone"]}</td>
    <td><a href="${editCustomerDataURL}">Change My Data</a></td>
  </tr>
</table>


<br/><br/>
<a href="${orderURL}">go on...</a>