package org.nightlabs.installer.base.ui.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.nightlabs.installer.Logger;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.Navigator.Navigation;
import org.nightlabs.installer.base.ui.WebUI;
import org.nightlabs.installer.base.ui.WebUI.Progress;
import org.nightlabs.installer.pages.ui.web.WebPageUI;
import org.nightlabs.installer.util.Util;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebInstallerHTTPD extends NanoHTTPD
{
	private static final int minPort = 1024;
	private static final int maxPort = 65535;
	private static final int maxPortTries = 10;
	private int port;
	
	private WebInstallerHTTPD(int port) throws IOException
	{
		// TODO: listen to all (0.0.0.0)
//		super(port, InetAddress.getByName("127.0.0.1"));
		super(port, InetAddress.getByAddress(new byte[] {0, 0, 0, 0}));
		this.port = port;
	}
	
	public static WebInstallerHTTPD createInstance(int preferredPort) throws IOException
	{
		WebInstallerHTTPD instance = null;
		Random rand = new Random();
		IOException ex = null;
		for(int i=0; i<=maxPortTries; i++) {
			int port;
			if(i == 0 && preferredPort > 0)
				port = preferredPort;
			else
				port = rand.nextInt(maxPort - minPort + 1) + minPort;
			try {
				instance = new WebInstallerHTTPD(port);
				Logger.out.println("Using port "+port);
				break;
			} catch(IOException e) {
				instance = null;
				ex = e;
				Logger.err.println("Port "+port+" unusable: "+e);
			}
		}
		if(instance == null)
			throw new IOException("bindError", ex);
		return instance;
	}

	/**
	 * Get the port.
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}
	
	private static class RequestParameters
	{
		private Properties props;

		/**
		 * Create a new RequestParameters instance.
		 * @param props
		 */
		public RequestParameters(Properties props)
		{
			this.props = props;
		}
		
		public String getParameter(String name)
		{
			return props.getProperty(name);
		}
	}
	
//	private static String getHeader() throws IOException
//	{
//		InputStream in = WebInstallerHTTPD.class.getResourceAsStream("resource/header.html");
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		Util.transferStreamData(in, out);
//		in.close();
//		return out.toString();
//	}
//	
//	private static String getFooter() throws IOException
//	{
//		InputStream in = WebInstallerHTTPD.class.getResourceAsStream("resource/footer.html");
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		Util.transferStreamData(in, out);
//		in.close();
//		return out.toString();
//	}
	
	private static class ShutdownThread extends Thread
	{
		private static long waitTimeInMillis = 5000;
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			try {
				Thread.sleep(waitTimeInMillis);
				System.exit(0);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.installer.web.NanoHTTPD#serve(java.lang.String, java.lang.String, java.util.Properties, java.util.Properties)
	 */
	@Override
	public Response serve(String uri, String method, Properties header,	Properties parms)
	{
		Logger.out.println("uri: "+uri);
		
		if(!"/".equals(uri)) {
			
			if("/installer-wizard-icon.png".equals(uri) || "/installer-wizard-icon.jpg".equals(uri) || "/installer-wizard-icon.gif".equals(uri)) {
				return handleWizardIcon();
			} else if("/progress.xml".equals(uri)) {
				return handleProgressXml();
			} else if("/executionpage.html".equals(uri)) {
				return handleExecutionPageHtml();
			} else {
				return handleResource(uri);
			}
		}
		
		// translate navigation from localized string to valid enum value
		String navigationLocalized = parms.getProperty("navigation_localized");
		if(navigationLocalized != null) {
			Logger.out.println("Navigation Localized: "+parms.getProperty("navigation_localized"));
			if(navigationLocalized.equals(Messages.getString("WebUI.navigation.next")))
				parms.setProperty("navigation", Navigation.next.toString());
			else if(navigationLocalized.equals(Messages.getString("WebUI.navigation.back")))
				parms.setProperty("navigation", Navigation.back.toString());
			Logger.out.println("Navigation: "+parms.getProperty("navigation"));
		}
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(bout);
		WebUI.sharedInstance().informAboutRequest(new RequestParameters(parms), w);
		WebUI.sharedInstance().waitForRendering();
		if(WebUI.sharedInstance().isExecuting()) {
			Response response = new Response(HTTP_FOUND, MIME_PLAINTEXT, "");
			response.header.setProperty("Location", "executionpage.html");
			return response;
		} else {
			try {
				w.flush();
				InputStream bin = new ByteArrayInputStream(bout.toByteArray());
				Response response = new Response(HTTP_OK, MIME_HTML, bin);
				// add this for IE:
				response.header.setProperty("Expires", formatHeaderDate(new Date(System.currentTimeMillis() - 1000*60)));
				response.header.setProperty("Pragma", "No-Cache");
				return response;
			} catch (IOException e) {
				e.printStackTrace();
				return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "Error: "+e.getMessage());
			} finally {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * @return
	 */
	private Response handleExecutionPageHtml()
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(bout);
		try {
			WebPageUI.renderHeader(w, "Installation progress", null);
			InputStream in = WebInstallerHTTPD.class.getResourceAsStream("resource/executionpage.html");
			w.flush();
			Util.transferStreamData(in, bout);
			WebPageUI.renderFooter(w);
			w.flush();
			InputStream bin = new ByteArrayInputStream(bout.toByteArray());
			Response response = new Response(HTTP_OK, MIME_HTML, bin);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "Error: "+e.getMessage());
		} finally {
			try {
				w.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * @param uri
	 * @return
	 */
	private Response handleResource(String uri)
	{
		InputStream in = getClass().getResourceAsStream("resource/"+uri.substring(1));
		if(in == null)
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "not found");
		else {
			Response response = new Response(HTTP_OK, getMimeTypeByName(uri), in);
			response.header.setProperty("Expires", formatHeaderDate(new Date(System.currentTimeMillis() + 1000*60*60*24*256)));
			return response;
		}
	}

	/**
	 * @return
	 */
	private Response handleProgressXml()
	{
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"");
		xml.append(getCharset().name());
		xml.append("\"?>\n");
		xml.append("<progress>\n");
		
		Progress progress = WebUI.sharedInstance().getAndResetProgress();
		if(progress != null) {
			xml.append("<workDone>");
			xml.append(progress.workDone);
			xml.append("</workDone>\n");
			xml.append("<workTotal>");
			xml.append(progress.workTotal);
			xml.append("</workTotal>\n");
			xml.append("<done>");
			xml.append(progress.done);
			xml.append("</done>\n");
			for (String message : progress.messages) {
				xml.append("<message>");
				xml.append(message);
				xml.append("</message>\n");
			}
			if(progress.done)
				new ShutdownThread().start();
		}
		xml.append("</progress>\n");
		
		//Logger.out.println("PROGRESS RESULT: "+xml.toString());
		
		Response response = new Response(HTTP_OK, MIME_XML, xml.toString());
		// add this for IE:
		response.header.setProperty("Expires", formatHeaderDate(new Date(System.currentTimeMillis() - 1000*60)));
		response.header.setProperty("Pragma", "no-cache");
		return response;
	}

	/**
	 * @return
	 */
	private Response handleWizardIcon()
	{
		URL installerWizardIconURL = WebUI.sharedInstance().getInstallerWizardIconURL();
		if(installerWizardIconURL != null) {
			try {
				InputStream in = installerWizardIconURL.openStream();
				return new Response(HTTP_OK, getMimeTypeByName(installerWizardIconURL.toString()), in);
			} catch (IOException e) {
				Logger.err.println("Failed to open wizard icon stream: "+installerWizardIconURL);
			}
		}
		return new Response(HTTP_NOTFOUND, MIME_DEFAULT_BINARY, "");
	}
}
