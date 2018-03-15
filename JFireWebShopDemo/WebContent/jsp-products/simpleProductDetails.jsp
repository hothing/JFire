<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />
<fmt:message key="shoppingcart.continueshopping" var="msg_continueshopping"/>

<c:url value="/product/list/" var="productlistURL"/>
<c:url value="/product/imageproperty/" var="imagePropertyURL">
	<c:param name="productTypeID" value="${productType.objectId}"/>
	<c:param name="structFieldID" value="${structFieldIDLargeImage}"/>
</c:url>
<c:url value="/images/cart_small.png" var="addToShoppingcartImage"/>

<jsp:include page="../jsp-includes/shoppingcartBox.jsp"></jsp:include>

<div class="productDetails">
 	<div class="continueShopping">
		<a href="${productlistURL}">${msg_continueshopping}</a>
	</div>

	<div class="content">
		<div class="image">
			<a href="${imagePropertyURL}">
				<img src="${imagePropertyURL}">
			</a>
		</div>
		<div class="description">
			<div class="name">
				${productType.name.text}
			</div>
			<div class="text">
				${longDescription.i18nText.text}
			</div>
		</div>
		<div class="prices">
			<c:forEach items="${tariffPricePairs}" var="tariffPricePair">
				<c:url value="/shoppingcart/" var="addToShoppingcartURL">
					<c:param name="action" value="add"/>
					<c:param name="productTypeID" value="${productType.objectId}"/>
					<c:param name="tariffID" value="${tariffPricePair.tariff.objectId}"/>
				</c:url>
				<div class="price">
					<label class="tariffName">${tariffPricePair.tariff.name.text}:</label>
					<label class="amount">
						<fmt:formatNumber
								currencyCode="${tariffPricePair.price.currency.currencyID}" 
								minFractionDigits="${tariffPricePair.price.currency.decimalDigitCount}" 
								type="currency" 
								value="${tariffPricePair.price.amountAsDouble}"/>
					</label>
					<a class="addToShoppingCartLink" href="${addToShoppingcartURL}">
						<img src="${addToShoppingcartImage}" alt="${productType.name.text}" />
					</a>
				</div>
			</c:forEach>
		</div>
		<div class="xinfo">
			<div class="text">
				${xInfoText}
			</div>
		</div>

	</div>

</div>