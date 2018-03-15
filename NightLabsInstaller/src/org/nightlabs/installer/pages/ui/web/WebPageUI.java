package org.nightlabs.installer.pages.ui.web;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.nightlabs.installer.Logger;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Page;
import org.nightlabs.installer.base.UI;
import org.nightlabs.installer.base.Navigator.Navigation;
import org.nightlabs.installer.base.defaults.DefaultUI;
import org.nightlabs.installer.base.ui.NavigationUI;
import org.nightlabs.installer.base.ui.WebUI;
import org.nightlabs.installer.elements.ui.web.WebElementUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebPageUI extends DefaultUI
{
	protected String getTitle() throws InstallationException
	{
		return "Installation-Wizard";
	}
	
	protected String getAnnotation() throws InstallationException
	{
		return null;
	}
	
	protected void renderHeader() throws IOException, InstallationException
	{
		renderHeader(WebUI.sharedInstance().getRenderWriter(), getTitle(), getAnnotation());
	}
	
	public static void renderHeader(Writer w, String title, String annotation) throws IOException, InstallationException
	{
		w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		w.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		w.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		w.write("	<head>\n");
		w.write("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
		w.write("		<link rel=\"StyleSheet\" href=\"style.css\" type=\"text/css\"/>\n");
		w.write("	</head>\n");
		w.write("  <body>\n");
		w.write("		<div id=\"installerhead\" class=\"installerhead\">\n");
		w.write("			<img id=\"installericon\" class=\"installericon\" src=\"installer-wizard-icon.png\" />\n");
		if(title != null && title.length() > 0) {
			w.write("			<span id=\"installertitle\" class=\"installertitle\">");
			w.write(WebUI.htmlEntities(title));
			w.write("</span>\n");
		}
		if(annotation != null && annotation.length() > 0) {
			w.write("			<div id=\"installerannotation\" class=\"installerannotation\">\n");
			w.write(WebUI.htmlEntities(annotation));
			w.write("			</div>\n");
		}
		w.write("		</div>\n");
		w.write("		<div class=\"installer\">\n"); 
	}

	protected void renderFooter() throws IOException, InstallationException
	{
		renderFooter(WebUI.sharedInstance().getRenderWriter());
	}
	
	public static void renderFooter(Writer w) throws IOException, InstallationException
	{
		w.write("		</div>\n"); 
		w.write("		<div class=\"installertail\">\n"); 
		w.write("			<center><small>Copyright &copy; <a href=\"http://www.nightlabs.org/\">NightLabs</a> 2009</small></center>\n"); 
		w.write("		</div>\n"); 
		w.write("  </body>\n"); 
		w.write("</html>\n"); 
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		try {
			renderHeader();
			WebUI.sharedInstance().openPageDiv((Page)getInstallationEntity());
			WebUI.sharedInstance().openForm("pageform", getInstallationEntity().getId());
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#renderAfter()
	 */
	@Override
	public void renderAfter() throws InstallationException
	{
		super.renderAfter();
		try {
			renderButtons();
			WebUI.sharedInstance().closeForm();
			WebUI.sharedInstance().closeDiv();
			renderFooter();
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}
		afterRendering();
	}

	protected void afterRendering() throws InstallationException
	{
		WebUI.sharedInstance().informAboutRendering();
		WebUI.sharedInstance().waitForRequest();
		setNavigation();
		// TODO only store results if navigation is next (as in swing ui)
		storeResults();
	}

	protected void renderButtons() throws IOException
	{
		WebUI.sharedInstance().openDiv("buttonbar", "buttonbar");
		WebUI.sharedInstance().emptyInput("backbutton", "backbutton", "submit", "navigation_localized", Messages.getString("WebUI.navigation.back"), null);
		WebUI.sharedInstance().emptyInput("nextbutton", "nextbutton", "submit", "navigation_localized", Messages.getString("WebUI.navigation.next"), null);
		WebUI.sharedInstance().closeDiv();
	}
	
	protected void storeResults() throws InstallationException
	{
		List<? extends InstallationEntity> elements = getInstallationEntity().getChildren();
		for (InstallationEntity entity : elements) {
			UI ui = entity.getUI();
			if(!(ui instanceof WebElementUI))
				Logger.out.println("WARNING: have non web-ui element ui: "+ui.getClass().getName());
			else {
				WebElementUI elementUI = (WebElementUI)ui;
				elementUI.storeResult();
			}
		}
	}
	
	protected void setNavigation() throws InstallationException
	{
		String navigation = WebUI.sharedInstance().getParameterValue("navigation");
		Logger.out.println("NAVIGATION: "+navigation);
		if(navigation != null) {
			Navigation n = Navigation.valueOf(navigation);
			if(n != null) {
				Logger.out.println("setting navigation: "+n);
				NavigationUI.setNavigation(getInstallationEntity().getParent(), n);
			}
		}
	}
}
