/**
 * 
 */
package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.rmi.ConnectException;

import javax.naming.CommunicationException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorDialogFactory;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerParam;
import org.nightlabs.base.ui.exceptionhandler.IExceptionHandler;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * An Implementation of IExceptionHandler, which shows a message user friendly message
 * if a java.net.ConnectException occurs.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ConnectExceptionHandler implements IExceptionHandler {

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.exceptionhandler.IExceptionHandler#handleException(org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerParam)
	 */
	@Override
	public boolean handleException(ExceptionHandlerParam handlerParam) 
	{
		Throwable triggerException = handlerParam.getTriggerException();
		Throwable thrownException = handlerParam.getThrownException();
		if (triggerException instanceof java.net.ConnectException) {
			String message = String.format(Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.ConnectExceptionHandler.message"), getHost(thrownException));						 //$NON-NLS-1$
			ErrorDialogFactory.showError(DefaultErrorDialog.class,
				Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.ConnectExceptionHandler.title"), //$NON-NLS-1$
				message,
				handlerParam);
			return true;			
		}
		return false;		
	}

	private String getHost(Throwable thrownException) 
	{
		String host = Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.ConnectExceptionHandler.unknown"); //$NON-NLS-1$
		Throwable t = null;
		int index = ExceptionUtils.indexOfThrowable(thrownException, ConnectException.class);
		if (index != -1) {
			t = (Throwable) ExceptionUtils.getThrowableList(thrownException).get(index);
			String message = t.getMessage();
			int messageIndex = message.lastIndexOf(":"); //$NON-NLS-1$
			if (messageIndex != -1) {
				host = message.substring(index + 1);
				return host;
			}			
		}
		
		int index2 = ExceptionUtils.indexOfThrowable(thrownException, CommunicationException.class);
		if (index2 != -1) {
			t = (Throwable) ExceptionUtils.getThrowableList(thrownException).get(index2);			
			String message = t.getMessage();
			String replace = message.replace("Could not obtain connection to any of these urls:", ""); //$NON-NLS-1$ //$NON-NLS-2$
			int lastIndex = replace.lastIndexOf("and discovery failed with error"); //$NON-NLS-1$
			if (lastIndex != -1) {
				host = replace.substring(0, lastIndex);
			}
		}		
		return host;		
	}
}
