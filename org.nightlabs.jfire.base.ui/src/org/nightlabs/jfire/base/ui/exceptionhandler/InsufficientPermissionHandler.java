/**
 *
 */
package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBAccessException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorDialogFactory;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerParam;
import org.nightlabs.base.ui.exceptionhandler.IExceptionHandler;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.MissingRoleException;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.id.RoleID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 */
public class InsufficientPermissionHandler implements IExceptionHandler
{
	private Logger logger = Logger.getLogger(InsufficientPermissionHandler.class);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window.IExceptionHandler#handleException(java.lang.Throwable)
	 */
	@Override
	public boolean handleException(ExceptionHandlerParam handlerParam) {
		logger.info("handleException: called for this triggerException: " + handlerParam.getTriggerException()); //$NON-NLS-1$

		if (handlerParam.getTriggerException() instanceof SecurityException) {
			try {
				RemoteExceptionClassHandler handler = new RemoteExceptionClassHandler();
				if (handler.handleException(handlerParam.getThread(), handlerParam.getThrownException(), (SecurityException) handlerParam.getTriggerException()))
					return true;
			} catch (NoClassDefFoundError x) {
				// ignore and use fallback-dialog below
			}
		}
		else if (handlerParam.getTriggerException() instanceof EJBAccessException) {
			// Unfortunately, the JBoss does not reveal any detailed information, i.e. what access rights exactly are missing anymore, for EJB3.
			// Therefore, we cannot (yet) do the same as we did with the SecurityException before: Parsing the missing access rights and
			// show a specific error message. Thus, we simply use the fallback-dialog below (for now).
			// See: https://www.jfire.org/modules/bugs/view.php?id=1292
		}
		else
			logger.error("triggerException is neither an instance of SecurityException nor EJBAccessException! Instead it is: " + (handlerParam.getTriggerException() == null ? null : handlerParam.getTriggerException().getClass().getName()), handlerParam.getThrownException()); //$NON-NLS-1$


		ErrorDialogFactory.showError(DefaultErrorDialog.class,
				Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler.dialog.insufficentPermissions.messgae"),  //$NON-NLS-1$
				Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler.dialog.insufficentPermissions.description"),  //$NON-NLS-1$
				new ExceptionHandlerParam(handlerParam.getThrownException(), handlerParam.getTriggerException()));

		return true;
	}

	// We handle the MissingRoleException in a subclass, because it might happen that we cannot load
	// classes needed for this and thus keep the InsufficientPermissionHandler even workable if this subclass is not functional.
	private static class RemoteExceptionClassHandler
	{
		private static Logger logger = Logger.getLogger(RemoteExceptionClassHandler.class);

		public boolean handleException(Thread thread, Throwable thrownException, SecurityException securityException) {
			Set<RoleID> requiredRoleIDs = null;
			Set<Role> requiredRoles = null;
			String authorityName = null;

			if (securityException instanceof MissingRoleException) {
				requiredRoleIDs = ((MissingRoleException)securityException).getRequiredRoleIDs();
				requiredRoles = ((MissingRoleException)securityException).getRequiredRoles();
				Authority authority = ((MissingRoleException)securityException).getAuthority();
				if (authority != null)
					authorityName = authority.getName().getText();
			}
			else {
				// check if the securityException comes from the server by scanning thrownException for a remote exception
				if (ExceptionUtils.indexOfThrowable(thrownException, java.rmi.AccessException.class) < 0) {
					logger.info("There is no java.rmi.AccessException in the stack trace, hence we don't try to parse the exception message and fall back to the default dialog."); //$NON-NLS-1$
					return false;
				}

				// If we come here, the exception is thrown by the JavaEE server when checking the EJB method permissions.
				// So we try to parse the exception message.

				// TODO WARNING: This is JBoss dependent code. When we support another JavaEE server, we very likely have to extend this code!
				//
				// JBoss 4.2.2: We search for a message like this:
				//   Insufficient method permissions, principal=marco@chezfrancois.jfire.org?sessionID=LKsC9cN-tCN9&workstationID=ws00&, ejbName=jfire/ejb/JFireBaseBean/JFireSecurityManager, method=getUserIDs, interface=REMOTE, requiredRoles=[org.nightlabs.jfire.security.accessRightManagement, TestTestTest], principalRoles=[_Guest_]
				//
				// Hence, we search for this to parse the role-ids:
				//   requiredRoles=[org.nightlabs.jfire.security.accessRightManagement, TestTestTest]

				String exceptionMessage = securityException.getMessage();
				if (exceptionMessage == null) {
					logger.info("The SecurityException's message is null, hence we cannot parse it and fall back to the default dialog."); //$NON-NLS-1$
					return false;
				}

				String requiredRolesBeginToken = "requiredRoles=["; //$NON-NLS-1$
				int indexOfRequiredRolesBegin = exceptionMessage.indexOf(requiredRolesBeginToken);
				if (indexOfRequiredRolesBegin < 0) {
					logger.info("The SecurityException's message does not contain begin-token \"" + requiredRolesBeginToken + "\" => falling back to default dialog."); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}

				indexOfRequiredRolesBegin += requiredRolesBeginToken.length();

				// now, indexOfRequiredRolesBegin points to the first character of the first roleID

				String requiredRolesEndToken = "]"; //$NON-NLS-1$
				int indexOfRequiredRolesEnd = exceptionMessage.indexOf(requiredRolesEndToken, indexOfRequiredRolesBegin);
				if (indexOfRequiredRolesEnd < 0) {
					logger.info("The SecurityException's message does not contain end-token \"" + requiredRolesEndToken + "\" after begin-token \"" + requiredRolesBeginToken + "\" => falling back to default dialog."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}

				// get the substring within the 2 tokens
				String requiredRolesString = exceptionMessage.substring(indexOfRequiredRolesBegin, indexOfRequiredRolesEnd);
				requiredRolesString = requiredRolesString.trim();
				if ("".equals(requiredRolesString)) { //$NON-NLS-1$
					logger.info("The SecurityException's message does not contain any role between begin-token \"" + requiredRolesBeginToken + "\" and end-token \"" + requiredRolesEndToken + "\" => falling back to default dialog."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}

				// split it, because it might be multiple separated by "," (and having spaces => trim)
				String[] requiredRoleIDStrings;
				if (requiredRolesString.contains(",")) { //$NON-NLS-1$
					requiredRoleIDStrings = requiredRolesString.split(","); //$NON-NLS-1$
					for (int i = 0; i < requiredRoleIDStrings.length; i++) {
						requiredRoleIDStrings[i] = requiredRoleIDStrings[i].trim();
					}
				}
				else
					requiredRoleIDStrings = new String[] { requiredRolesString.trim() };

				requiredRoleIDs = new HashSet<RoleID>(requiredRoleIDStrings.length);
				for (String roleIDString : requiredRoleIDStrings) {
					requiredRoleIDs.add(RoleID.create(roleIDString));
				}
			}

			InsufficientPermissionDialogContext context = new InsufficientPermissionDialogContext(requiredRoleIDs, requiredRoles);
			InsufficientPermissionDialog.setInsufficientPermissionDialogContext(context);
			try {

				String message;

				if (authorityName == null)
					message = Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler.notEnoughPermission.message"); //$NON-NLS-1$
				else
					message = String.format(
							Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler.notEnoughPermissionAuthority.message"), //$NON-NLS-1$
							authorityName);

				ErrorDialogFactory.showError(InsufficientPermissionDialog.class,
						Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionHandler.dialog.insufficientPermissions.message"),  //$NON-NLS-1$
						message,
						new ExceptionHandlerParam(thrownException, securityException));

			} finally {
				InsufficientPermissionDialog.removeInsufficientPermissionDialogContext();
			}

//			debug_showErrorDialogDelayed(thread, thrownException, securityException, context);

			return true;
		}

//		private void debug_showErrorDialogDelayed(
//				final Thread thread,
//				final Throwable thrownException,
//				final SecurityException securityException,
//				final InsufficientPermissionDialogContext context
//		)
//		{
//			final Display display = Display.getCurrent();
//
//			Thread runner = new Thread() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep((long)(Math.random() * 5000) + 3000);
//					} catch (InterruptedException e) {
//						// ignore
//					}
//
//					display.asyncExec(new Runnable() {
//						public void run() {
//							InsufficientPermissionDialog.setInsufficientPermissionDialogContext(context);
//							try {
//
//								ErrorDialogFactory.showError(InsufficientPermissionDialog.class,
//										"Insufficient permissions",
//										"You don't have enough permissions for the requested action. Contact your boss or your administrator.\n\nYou need at least one of the following role groups:",
//										thrownException, securityException);
//
//							} finally {
//								InsufficientPermissionDialog.removeInsufficientPermissionDialogContext();
//							}
//						}
//					});
//				}
//			};
//			runner.start();
//		}
	}
}
