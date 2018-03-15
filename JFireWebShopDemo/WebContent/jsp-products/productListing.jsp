<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/images/cart_small.png" var="addToShoppingcartImage"/>

<script src="<c:url value="/includes/imageScaler.js"/>" type="text/javascript"></script>

<jsp:include page="../jsp-includes/shoppingcartBox.jsp"></jsp:include>

<div class="productListing">
	<table class="content">
		<tr>
			<c:forEach items="${simpleProductTypesAndPrices}" var="productType" varStatus="productLoopStatus">
				<c:url value="/simpleproduct/details/" var="detailsURL">
					<c:param name="productTypeID" value="${productType.key.objectId}"/>
				</c:url>
				<c:url value="/product/imageproperty/productTypeID;${objectIdEnc}/structFieldID;${structFieldIdEnc}" var="propertyURL">
					<c:param name="productTypeID" value="${productType.key.objectId}"/>
					<c:param name="structFieldID" value="${structFieldIDSmallImage}"/>
				</c:url>
				<td class="product">
					<div class="name">${productType.key.name.text}</div>
					<div class="image">
						<a class="detailLink" href="${detailsURL}">
							<!--  <img src="${propertyURL}" alt="" border="0" onLoad="scaleImage(this,164,100)"> -->
							<img src="${propertyURL}" alt="${productType.key.name.text}" />
						</a>
					</div>
					<div class="prices">
						${structFieldIDDescriptionShort[productType.key].i18nText.text}
						<c:forEach items="${productType.value}" var="tariffPricePair">
							<div class="price">
								<c:url value="/shoppingcart/" var="addURL">
									<c:param name="action" value="add"/>
									<c:param name="productTypeID" value="${productType.key.objectId}"/>
									<c:param name="tariffID" value="${tariffPricePair.tariff.objectId}"/>
								</c:url>
								<label class="tariffName">${tariffPricePair.tariff.name.text}</label>
								<label class="amount">
									<fmt:formatNumber 
										currencyCode="${tariffPricePair.price.currency.currencyID}" 
										minFractionDigits="${tariffPricePair.price.currency.decimalDigitCount}" 
										type="currency" 
										value="${tariffPricePair.price.amountAsDouble}"/>
								</label>
								<a class="addToShoppingCartLink" href="${addURL}"><img src="${addToShoppingcartImage}" border="0"></a>
							</div>
						</c:forEach>
					</div>
				</td>
				<c:if test="${productLoopStatus.count % 2 == 0}"><c:out escapeXml="false" value="</tr><tr>"/></c:if>
			</c:forEach>
		</tr> 
		<tr>
			<c:forEach items="${voucherTypesAndPrices}" var="productType" varStatus="productLoopStatus">
				<c:url value="/voucher/details/" var="detailsURL">
					<c:param name="productTypeID" value="${productType.key.objectId}"/>
				</c:url>
				<c:url value="/product/imageproperty/productTypeID;${objectIdEnc}/structFieldID;${structFieldIdEnc}" var="propertyURL">
					<c:param name="productTypeID" value="${productType.key.objectId}"/>
				</c:url>
				<td class="product">
					<div class="name">${productType.key.name.text}</div>
					<div class="image">
						<a class="detailLink" href="${detailsURL}">
							${productType.key.name.text}
						</a>
					</div>
					<div class="prices">
							<div class="price" align="center">
								<c:url value="/shoppingcart/" var="addURL">
									<c:param name="action" value="addvoucher"/>
									<c:param name="productTypeID" value="${productType.key.objectId}"/>
								</c:url>
								<label class="amount">
									<fmt:formatNumber 
										currencyCode="${productType.value.currency.currencyID}" 
										minFractionDigits="${productType.value.currency.decimalDigitCount}" 
										type="currency" 
										value="${productType.value.amountAsDouble}"/>
								</label>
								<a class="addToShoppingCartLink" href="${addURL}"><img src="${addToShoppingcartImage}" border="0"></a>
							</div>
					</div>
				</td>
				<c:if test="${productLoopStatus.count % 2 == 0}"><c:out escapeXml="false" value="</tr><tr>"/></c:if>
			</c:forEach>
		</tr> 
	</table>
</div>	