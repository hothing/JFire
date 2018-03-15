JFire - Free / Open Source ERP System (www.jfire.org)

1. Login

When your are using the demo organisation (ChezFrancois) either on a local server 
or on the JFire demo server, you can use the following parameters for login.

Wenn Sie die Demo Organisation (ChezFrancois) benutzen, entweder auf einem lokalen Server 
oder auf dem JFire Demo Server, dann k�nnen Sie die folgenden Parameter zum anmelden benutzen.

- Login
User:		francois
Password:	test
Workstation:	

- Details
organisation:	chezfrancois.jfire.org
ServerURL:	jnp://localhost:1099 (local server) or
		jnp://demo.jfire.org:1099 (jfire demo server)
InitialContextFactory: org.jboss.security.jndi.LoginInitialContextFactory

2. Application Startup Problems

When you are using an Sun Java Version before 1.5.0_07 or an JVM from another vendor, 
you will properly get an error when you are trying to start the application. This is because of 
the VM parameters -XX:+UnlockDiagnosticVMOptions -XX:+UnsyncloadClass defined in the file jfire.ini, 
because without these parameters the risk of an application freeze is quite high.
In this case we strongly recommend you to update your Java to an Sun JVM version 1.5.0_07 or above.
You can download the newest java version from www.java.com (JRE) or java.sun.com (JDK).
You can try to check your java version by typing java -version in the command line.

Wenn Sie eine eine Sun Java Version vor 1.5.0_07 oder eine JVM von einem anderen Hersteller haben,
werden Sie wahrscheinlich einen Fehler beim Applikationsstart erhalten. Dies liegt an den VM Parametern
-XX:+UnlockDiagnosticVMOptions -XX:+UnsyncloadClass welche in der Datei jfire.ini definiert sind,
da ohne diese Parameter die Wahrscheinlichkeit das die Applikation sich aufh�ngt erh�ht ist.
In diesem Fall raten wir Ihnen dringend dazu ihr Java auf eine Sun JVM version 1.5.0_07 oder h�her upzudaten. 
Sie k�nnen die neuste Java version unter www.java.com (JRE) oder java.sun.com (JDK) herunterladen.
Um ihre java version zu erfahren, k�nnen Sie probieren in der Kommandozeile java -version einzutippen.

