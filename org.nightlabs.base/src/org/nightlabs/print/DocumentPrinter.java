/* ********************************************************************
 * NightLabsBase - Utilities by NightLabs                             *
 * Copyright (C) 2004-2008 NightLabs GmbH - http://NightLabs.org      *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.print;

import java.awt.print.PrinterException;
import java.io.File;

/**
 * DocumentPrinter is the implementation of {@link PrinterInterface} to be used
 * when you need to print documents generically. Instances are created
 * by the {@link PrinterInterfaceFactory}
 * for one print job and thrown away
 * afterwards.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface DocumentPrinter extends PrinterInterface {

	public void printDocument(File file) throws PrinterException;

	// Restricted DocumentPrinter to local files
//	public void printDocument(InputStream inputStream) throws PrinterException;
//
//	public void printDocument(URL url) throws PrinterException;
}
