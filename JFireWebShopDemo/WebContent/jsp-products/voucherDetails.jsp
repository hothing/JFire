<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />
<fmt:message key="shoppingcart.continueshopping" var="msg_continueshopping"/>

<c:url value="/product/list/" var="productlistURL"/>
<c:url value="/images/cart_small.png" var="addToShoppingcartImage"/>

<jsp:include page="../jsp-includes/shoppingcartBox.jsp"></jsp:include>

<div class="productDetails">
 	<div class="continueShopping">
		<a href="${productlistURL}">${msg_continueshopping}</a>
	</div>

	<div class="content">
		<div class="description">
			<div class="name">
				${productType.name.text}
			</div>
		</div>
		<div class="prices">
				<c:url value="/shoppingcart/" var="addToShoppingcartURL">
					<c:param name="action" value="addvoucher"/>
					<c:param name="productTypeID" value="${productType.objectId}"/>
				</c:url>
				<div class="price">
					<label class="amount">
						<fmt:formatNumber 
							currencyCode="${voucherPrice.currency.currencyID}" 
							minFractionDigits="${voucherPrice.currency.decimalDigitCount}" 
							type="currency" 
							value="${voucherPrice.amountAsDouble}"/>
					</label>
					<a class="addToShoppingCartLink" href="${addToShoppingcartURL}">
						<img src="${addToShoppingcartImage}" alt="${productType.name.text}" />
					</a>
				</div>
		</div>
	</div>

</div>