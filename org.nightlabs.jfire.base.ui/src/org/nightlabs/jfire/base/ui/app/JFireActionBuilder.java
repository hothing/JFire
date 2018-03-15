/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.app;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.ui.application.IActionBarConfigurer;
import org.nightlabs.base.ui.app.DefaultActionBuilder;

/**
 * Creates the Menu for the JFire Application
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Daniel Mazurek <daniel[AT]nightlabs[DOT]de>
 *
 */
public class JFireActionBuilder
extends DefaultActionBuilder
{
//	public JFireActionBuilder(IActionBarConfigurer configurer) {
//		super(configurer);
//		// by default JFire Applications don't use the eclipse help system
//		if (menuBarItems != null) {
//			menuBarItems.remove(ActionBarItem.Help);
//			menuBarItems.remove(ActionBarItem.Update);
//			menuBarItems.remove(ActionBarItem.Import);
//			menuBarItems.remove(ActionBarItem.Export);
//			menuBarItems.remove(ActionBarItem.Properties);
//			menuBarItems.remove(ActionBarItem.Back_History);
//			menuBarItems.remove(ActionBarItem.Forward_History);
//		}
//	}

	public JFireActionBuilder(IActionBarConfigurer configurer) {
		super(configurer,
				new ArrayList<ActionBarItem>(
						Arrays.asList(new ActionBarItem[] {
								ActionBarItem.New,
								ActionBarItem.Open,
								ActionBarItem.Close,
								ActionBarItem.CloseAll,
								ActionBarItem.Quit,
//								ActionBarItem.Back_History,
//								ActionBarItem.Forward_History,
								ActionBarItem.About,
								ActionBarItem.Intro,
								ActionBarItem.KeyAssist,
								ActionBarItem.Perspectives,
								ActionBarItem.Views,
								ActionBarItem.Print,
								ActionBarItem.RecentFiles,
								ActionBarItem.Save,
								ActionBarItem.SaveAs,
								ActionBarItem.Preferences
						})
				),
				new ArrayList<ActionBarItem>(
						Arrays.asList(new ActionBarItem[] {
								ActionBarItem.Save
						})
				)
		);
	}

}
