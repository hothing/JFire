package org.nightlabs.installer.pages.ui.web;

import java.io.IOException;

import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Page;
import org.nightlabs.installer.base.ui.WebUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExecutionPageUI extends PageUI
{
//	/* (non-Javadoc)
//	 * @see org.nightlabs.installer.pages.ui.web.WebPageUI#renderButtons()
//	 */
//	@Override
//	protected void renderButtons() throws IOException
//	{
//		// do nothing. finish button comes out of html resource
//	}
//
//	/* (non-Javadoc)
//	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
//	 */
//	@Override
//	public void render() throws InstallationException
//	{
//		super.render();
//		InstallationManager.getInstallationManager().addExecutionProgressListener(WebUI.sharedInstance());
//		try {
////			WebUI.sharedInstance().write("\n<script type=\"text/javascript\">\n");
////			WebUI.sharedInstance().write("<!--\n");
////			writeFile("prototype.js");
////			writeFile("jsProgressBarHandler.js");
////			WebUI.sharedInstance().write("\n-->\n");
////			WebUI.sharedInstance().write("</script>\n\n");
//			writeFile("executionpage.html");
//		} catch (IOException e) {
//			throw new InstallationException("Rendering failed", e);
//		}
//	}
//
//	/**
//	 * @throws IOException
//	 * @throws UnsupportedEncodingException
//	 */
//	private void writeFile(String filename) throws IOException, UnsupportedEncodingException
//	{
//		InputStream in = getClass().getResourceAsStream(filename);
//		try {
//			byte[] buf = new byte[2048];
//			while(true) {
//				int read = in.read(buf);
//				if(read == -1)
//					break;
//				WebUI.sharedInstance().write(new String(buf, 0, read, "UTF-8"));
//			}
//		} finally {
//			in.close();
//		}
//	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		try {
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
		} catch (IOException e) {
			throw new InstallationException("Rendering failed", e);
		}
		InstallationManager.getInstallationManager().addExecutionProgressListener(WebUI.sharedInstance());		
		WebUI.sharedInstance().informAboutRendering();
	}

	protected void renderButtons() throws IOException
	{
		WebUI.sharedInstance().emptyInput("backbutton", "backButton", "submit", "navigation_localized", Messages.getString("WebUI.navigation.back"), null);
		WebUI.sharedInstance().emptyInput("nextbutton", "nextButton", "submit", "navigation_localized", Messages.getString("WebUI.navigation.next"), null);
	}	
}
