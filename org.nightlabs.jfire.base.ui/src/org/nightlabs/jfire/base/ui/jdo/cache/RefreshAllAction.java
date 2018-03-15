package org.nightlabs.jfire.base.ui.jdo.cache;

import org.eclipse.jface.action.IAction;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.ui.login.action.LSDWorkbenchWindowActionDelegate;

public class RefreshAllAction
extends LSDWorkbenchWindowActionDelegate
{
	@Override
	public void run(IAction action) {
		Cache.sharedInstance().refreshAll();
	}
}
