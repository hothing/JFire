package org.nightlabs.installer.base.ui;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.installer.ExecutionProgressEvent;
import org.nightlabs.installer.ExecutionProgressListener;
import org.nightlabs.installer.Logger;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.ExecutionProgressEvent.Type;
import org.nightlabs.installer.base.Element;
import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Page;
import org.nightlabs.installer.base.ui.web.WebInstallerHTTPD;
import org.nightlabs.installer.pages.ui.web.WebPageUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebUI implements ExecutionProgressListener
{
	private static WebUI sharedInstance = null;
	
	public static WebUI sharedInstance()
	{
		if(sharedInstance == null)
			sharedInstance = new WebUI();
		return sharedInstance;
	}
	
	private boolean serverStarted = false;
	private boolean haveRequest;
	private Object requestData;
	private Writer renderWriter;
	
	private WebPageUI activePage;
	
	private boolean haveRendering = false;
	private boolean executing = false;
	
	private int indentLevel = 5;
	private static final int indent = 2;

	// ---
	
	private Object progressMutex = new Object();
	public static class Progress {
		public List<String> messages = new LinkedList<String>();
		public int workDone;
		public int workTotal;
		public boolean done;
	}
	private Progress progress;
	
	public void executionProgress(ExecutionProgressEvent e)
	{
		executing = true;
		synchronized (progressMutex) {
			if(progress == null)
				progress = new Progress();
			progress.messages.add(e.getDescription());
			Logger.out.println("* "+e.getDescription()); //$NON-NLS-1$
			progress.workDone = e.getWorkDone();
			progress.workTotal = e.getSource().getTotalWork();
			if(!progress.done)
				progress.done = e.getType() == Type.done;
		}
	}
	public Progress getAndResetProgress()
	{
		synchronized (progressMutex) {
			Progress result = progress;
			if(result != null) {
				progress = new Progress();
				progress.workDone = result.workDone;
				progress.workTotal = result.workTotal;
				progress.done = result.done;
			}
			return result;
		}
	}

	// ---
	
	/**
	 * Get the executing.
	 * @return the executing
	 */
	public boolean isExecuting()
	{
		return executing;
	}
	
	public void startServer() throws InstallationException
	{
		if(!serverStarted) {
			serverStarted = true;
			// start server:
			try {
				WebInstallerHTTPD httpd = WebInstallerHTTPD.createInstance(8081);
				System.out.println(String.format(Messages.getString("WebUI.webServerRunning"), httpd.getPort())); //$NON-NLS-1$
			} catch (IOException e) {
				serverStarted = false;
				throw new InstallationException(Messages.getString("WebUI.errorStartingWebServer"), e); //$NON-NLS-1$
			}
			
			waitForRequest();
		}
	}
	
	public void waitForRequest()
	{
		haveRequest = false;
		Logger.out.println("waiting for request. Thread: "+Thread.currentThread().toString()); //$NON-NLS-1$
		while(!haveRequest) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		Logger.out.println("Have request"); //$NON-NLS-1$
	}
	
	public void informAboutRequest(Object requestData, Writer renderWriter)
	{
		haveRequest = true;
		this.requestData = requestData;
		this.renderWriter = renderWriter;
	}

	public void waitForRendering()
	{
		haveRendering = false;
		Logger.out.println("waiting for rendering. Thread: "+Thread.currentThread().toString()); //$NON-NLS-1$
		while(!executing && !haveRendering) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		Logger.out.println("Have rendering"); //$NON-NLS-1$
	}

	public void informAboutRendering()
	{
		haveRendering = true;
	}
	
//	/**
//	 * Get the webController.
//	 * @return the webController
//	 */
//	public WebControllerTest getWebController()
//	{
//		if(webController == null)
//			throw new IllegalStateException("No web controller set");
//		return webController;
//	}

	/**
	 * Get the activePage.
	 * @return the activePage
	 */
	public WebPageUI getActivePage()
	{
		return activePage;
	}

	/**
	 * Set the activePage.
	 * @param activePage the activePage to set
	 */
	public void setActivePage(WebPageUI activePage)
	{
		this.activePage = activePage;
	}

	/**
	 * Get the renderWriter.
	 * @return the renderWriter
	 */
	public Writer getRenderWriter()
	{
		return renderWriter;
	}

	/**
	 * Get the requestData.
	 * @return the requestData
	 */
	public Object getRequestData()
	{
		return requestData;
	}
	
	
	public String getParameterValue(String name) throws InstallationException
	{
		try {
			Object servletRequest = getRequestData();
			Method method = servletRequest.getClass().getMethod("getParameter", new Class[] {String.class}); //$NON-NLS-1$
			method.setAccessible(true);
			return (String)method.invoke(servletRequest, new Object[] { name });
		} catch(Throwable e) {
			throw new InstallationException("Getting parameter from servlet request failed", e); //$NON-NLS-1$
		}
	}
	
	
	
	
	// HTML FUNCTIONS
	
	public void write(String text) throws IOException
	{
		getRenderWriter().write(text);
	}
	
	protected void indent()
	{
		indentLevel++;
	}
	
	protected void unindent()
	{
		indentLevel--;
		if(indentLevel < 0)
			indentLevel = 0;
	}
	
	public void writeIndent() throws IOException
	{
		write(getIndent());
	}
	
	public String getIndent() throws IOException
	{
		StringBuffer b = new StringBuffer();
		for(int i=0; i<indentLevel*indent; i++)
			b.append(" "); //$NON-NLS-1$
		return b.toString();
	}
	
	public void writeText(String text) throws IOException
	{
		writeIndent();
		String htmlText = htmlEntities(text.trim());
		String indent = getIndent();
		write(htmlText.replace("\n", "\n"+indent)+"\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void writeFormattedText(String text) throws IOException
	{
		writeIndent();
		boolean inPre = true;
		for(int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if(inPre) {
				if(c == ' ') {
					write("&nbsp;"); //$NON-NLS-1$
				} else if(c == '\t') {
					write("&nbsp;&nbsp;&nbsp;&nbsp;"); //$NON-NLS-1$
				} else {
					inPre = false;
				}
			}
			if(!inPre) {
				if(c == '\n') {
					write("<br/>\n"); //$NON-NLS-1$
					writeIndent();
					inPre = true;
				} else {
					write(htmlEntities(String.valueOf(c)));
				}
			}
		}
		write("\n"); //$NON-NLS-1$
	}
	
	public static String htmlEntities(String text)
	{
		return text
				.replace("&", "&amp;") //$NON-NLS-1$ //$NON-NLS-2$
				.replace("<", "&lt;") //$NON-NLS-1$ //$NON-NLS-2$
				.replace(">", "&gt;") //$NON-NLS-1$ //$NON-NLS-2$
				.replace("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static class ElementAttribute 
	{
		private String attributeName;
		private String attributeValue;
		
		/**
		 * Create a new ElementAttribute instance.
		 * @param attributeName
		 * @param attributeValue
		 */
		public ElementAttribute(String attributeName, String attributeValue)
		{
			this.attributeName = attributeName;
			this.attributeValue = attributeValue;
		}
		
		/**
		 * Get the attributeName.
		 * @return the attributeName
		 */
		public String getAttributeName()
		{
			return attributeName;
		}
		
		/**
		 * Get the attributeValue.
		 * @return the attributeValue
		 */
		public String getAttributeValue()
		{
			return attributeValue;
		}
	}
	
	private void openElement(String elementName, ElementAttribute... attributes) throws IOException
	{
		if(elementName == null || elementName.length() == 0 || !elementName.matches("[a-zA-Z]+")) //$NON-NLS-1$
			throw new IllegalArgumentException("Invalid HTML element name: '"+elementName+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		writeIndent();
		write("<"); //$NON-NLS-1$
		write(elementName);
		for(ElementAttribute attribute : attributes) {
			if(attribute == null)
				continue;
			String attributeName = attribute.getAttributeName();
			String attributeValue = attribute.getAttributeValue();
			if(attributeName == null || attributeValue == null)
				continue;
			write(" "); //$NON-NLS-1$
			write(attributeName);
			write("=\""); //$NON-NLS-1$
			write(htmlEntities(attributeValue));
			write("\""); //$NON-NLS-1$
		}
		write(">\n"); //$NON-NLS-1$
		indent();
	}
	
	private void openElement(String elementName, String styleClass, String id) throws IOException
	{
		openElement(elementName, new ElementAttribute("class", styleClass), new ElementAttribute("id", id)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void closeElement(String elementName) throws IOException
	{
		if(elementName == null || elementName.length() == 0 || !elementName.matches("[a-zA-Z]+")) //$NON-NLS-1$
			throw new IllegalArgumentException("Invalid HTML element name: '"+elementName+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		unindent();
		writeIndent();
		write("</"); //$NON-NLS-1$
		write(elementName);
		write(">\n"); //$NON-NLS-1$
	}
	
	public void openForm(String styleClass, String id) throws IOException
	{
		openElement("form", new ElementAttribute("class", styleClass), new ElementAttribute("id", id), new ElementAttribute("method", "POST")); //$NON-NLS-1$
	}
	
	public void closeForm() throws IOException
	{
		closeElement("form"); //$NON-NLS-1$
	}
	
	public void openDiv(String styleClass, String id) throws IOException
	{
		openElement("div", styleClass, id); //$NON-NLS-1$
	}

	public void closeDiv() throws IOException
	{
		closeElement("div"); //$NON-NLS-1$
	}
	
	public void openEntityDiv(InstallationEntity entity, String additionalStyleClass) throws IOException
	{
		openDiv(entity.getClass().getSimpleName().toLowerCase()+" "+additionalStyleClass, entity.getId()); //$NON-NLS-1$
	}

	public void openPageDiv(Page page) throws IOException
	{
		openEntityDiv(page, "page"); //$NON-NLS-1$
	}
	
	public void openElementDiv(Element element) throws IOException
	{
		openEntityDiv(element, "element"); //$NON-NLS-1$
	}

	public void openElementPartDiv(String elementPartStyleClass) throws IOException
	{
		openDiv(elementPartStyleClass+" elementpart", null); //$NON-NLS-1$
	}
	
	public void openInput(String styleClass, String id, String type, String name, String value, ElementAttribute additionalAttribute) throws IOException //, String additionalAttributes) throws IOException
	{
		openElement("input",  //$NON-NLS-1$
				new ElementAttribute("class", styleClass),  //$NON-NLS-1$
				new ElementAttribute("id", id),  //$NON-NLS-1$
				new ElementAttribute("type", type),  //$NON-NLS-1$
				new ElementAttribute("name", name),  //$NON-NLS-1$
				new ElementAttribute("value", value), //$NON-NLS-1$
				additionalAttribute
				);
		
//		writeIndent();
//		write("<input");
//		if(styleClass != null)
//			write(" class=\""+htmlEntities(styleClass)+"\"");
//		if(id != null)
//			write(" id=\""+htmlEntities(id)+"\"");
//		if(type != null)
//			write(" type=\""+htmlEntities(type)+"\"");
//		if(name != null)
//			write(" name=\""+htmlEntities(name)+"\"");
//		if(value != null)
//			write(" value=\""+htmlEntities(value)+"\"");
//		if(additionalAttributes != null)
//			write(" "+additionalAttributes);
//		write(">\n");
//		indent();
	}

	public void closeInput() throws IOException
	{
		unindent();
		writeIndent();
		write("</input>\n"); //$NON-NLS-1$
	}
	
	public void emptyInput(String styleClass, String id, String type, String name, String value, ElementAttribute additionalAttribute) throws IOException //, String additionalAttributes) throws IOException
	{
		openInput(styleClass, id, type, name, value, additionalAttribute);
		closeInput();
	}
	
	private URL installerWizardIconURL;
	
	public void setInstallerWizardIcon(URL url)
	{
		this.installerWizardIconURL = url;
	}
	
	public URL getInstallerWizardIconURL()
	{
		return installerWizardIconURL;
	}
}
