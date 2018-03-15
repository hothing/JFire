package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * This interface defines an object that is able to find out the parent's <code>securingAuthorityID</code>, if the
 * {@link SecuredObject} managed by the authority-page (see {@link AbstractAuthorityPage}) supports data inheritance
 * (see {@link InheritanceManager}).
 * <p>
 * This interface also serves to initially determine whether the {@link Authority} of the currently edited
 * {@link SecuredObject} was inherited from its parent.
 * </p>
 * <p>
 * For details about how to use this, please read
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/UI%20for%20editing%20the%20Authority%20of%20a%20SecuredObject">UI for editing the Authority of a SecuredObject</a>
 * in our wiki.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface InheritedSecuringAuthorityResolver {

	AuthorityID getInheritedSecuringAuthorityID(ProgressMonitor monitor);
	/**
	 * Return the {@link SecuredObject} that implements {@link Inheritable} 
	 * which will be used to determine whether the securing authority was inherited.
	 * <p>
	 * Note, that after the authority assignment has been stored, this information might have
	 * changed on the server and the information is re-retrieved using this method.
	 * Your implementation should not values itself, it should rather relay on the {@link Cache}.
	 * </p>
	 * @param monitor The monitor to report progress.
	 */
	Inheritable retrieveSecuredObjectInheritable(ProgressMonitor monitor);
}
