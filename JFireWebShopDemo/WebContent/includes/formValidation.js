function checkInput(caseNumber) {
	switch (caseNumber) {
		case 0:
		// Namecheck requires at least 1 character  
		var name = document.registerForm.username.value;
		if (name.length== 0) {
			notify("No UserName specified");
			return false;
		} else return true; 
		case 1:
		var password = document.registerForm.password.value;
		//password requires at least 4 characters 
		if (password.length < 4) {
			if(password.length == 0) 
				notify("No password specified");
	  		else 
	  			notify("password needs to be at least 4 characters long");
			return false;
		} else return true; 
		case 2:
		// check if the original password is set and display if needed its message
		if(!checkInput(1)) return false; 		
		var password = document.registerForm.password.value;
		var password2 = document.registerForm.passwordConfirm.value;
		if(password  != password2) {
			notify("passwords do not match");
			return false;
		} else return true;
		default:
		var submitOk = true;
		for(var a=0; a<=2; a++) {
			if(!checkInput(a))submitOk= false;
		}
		return submitOk;	  
		
	}  
}
function checkLogin(caseNumber) {
	switch (caseNumber) {
		case 0:
		// Namecheck requires at least 1 character  
		var name = document.loginForm.username.value;
		if (name.length == 0) {
			notify("No UserName specified");
			return false;
		} else return true; 
		case 1:
		var password = document.loginForm.password.value;
		//password requires at least 4 characters 
		if (password.length < 4) {
			if(password.length == 0) 
				notify("No password specified");
	  		else 
	  			notify("password needs to be at least 4 characters long");
			return false;
		} else return true; 
		default:
		var submitOk = true;
		for(var a=0; a<=1; a++) {
			if(!checkLogin(a)) {
				submitOk = false;
			}
		}
		return submitOk;
		  
	}  
	
}
function notify(str) {

	alert(str);
}
function switchMode(str) {
	if(str == "log") {
	document.getElementById("registerFormular").style.display ="none";
	document.getElementById("loginFormular").style.display ="inline";
	}
	if(str == "new") {
	document.getElementById("registerFormular").style.display ="inline";
	document.getElementById("loginFormular").style.display ="none";
	}	
}