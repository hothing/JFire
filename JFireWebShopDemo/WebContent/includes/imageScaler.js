function scaleImage(image,maxWidth,maxHeight) {
	var tempX = maxWidth / image.offsetWidth;
	var tempY = maxHeight / image.offsetHeight;
	if (tempY >= tempX) {
		var resultX = maxWidth ;
		var resultY = Math.round(image.offsetHeight / (image.offsetWidth / maxWidth));
	}
	else {
		var resultY = maxHeight;
		var resultX = Math.round(image.offsetWidth / (image.offsetHeight / maxHeight));
	}
	// setting the right size
	image.style.width = resultX +"px";
	image.style.height = resultY +"px";
}	