/*
 * Copyright (C) 2003, 2004  Pascal Essiembre, Essiembre Consultant Inc.
 *
 * This file is part of Essiembre ResourceBundle Editor.
 *
 * Essiembre ResourceBundle Editor is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * Essiembre ResourceBundle Editor is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Essiembre ResourceBundle Editor; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */
package com.essiembre.eclipse.rbe.ui.editor.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.essiembre.eclipse.rbe.model.workbench.files.PropertiesFileCreator;
import com.essiembre.eclipse.rbe.model.workbench.files.StandardPropertiesFileCreator;

/**
 * Responsible for creating resources related to a standard
 * directory structure.
 * @author Pascal Essiembre (essiembre@users.sourceforge.net)
 * @version $Author: essiembre $ $Revision: 1.5 $ $Date: 2005/07/31 01:20:32 $
 */
public class StandardResourceFactory extends ResourceFactory {

    private final Map<Locale, SourceEditor> sourceEditors;
    private final PropertiesFileCreator fileCreator;
    private final String displayName;
    private final IEditorSite site;

    /**
     * Constructor.
     * @param site editor site
     * @param file file used to open all related files
     * @throws CoreException problem creating factory
     */
    protected StandardResourceFactory(IEditorSite site, IFile file)
             throws CoreException {
        super();
        this.site = site;
        sourceEditors = new HashMap<Locale, SourceEditor>();
        String bundleName = getBundleName(file);
        String regex = ResourceFactory.getPropertiesFileRegEx(file);
        IResource[] resources = StandardResourceFactory.getResources(file);

        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            String resourceName = resource.getName();
            // Build local title
            String localeText =
                    resourceName.replaceFirst(regex, "$2"); //$NON-NLS-1$
            StringTokenizer tokens =
                new StringTokenizer(localeText, "_"); //$NON-NLS-1$
            List<String> localeSections = new ArrayList<String>();
            while (tokens.hasMoreTokens()) {
                localeSections.add(tokens.nextToken());
            }
            Locale locale = null;
            switch (localeSections.size()) {
            case 1:
                locale = new Locale(localeSections.get(0));
                break;
            case 2:
                locale = new Locale(
                        localeSections.get(0),
                        localeSections.get(1));
                break;
            case 3:
                locale = new Locale(
                        localeSections.get(0),
                        localeSections.get(1),
                        localeSections.get(2));
                break;
            default:
                break;
            }
            SourceEditor sourceEditor =
                    createEditor(site, resource, locale);
            if (sourceEditor != null) {
                sourceEditors.put(sourceEditor.getLocale(), sourceEditor);
            }
        }
        fileCreator = new StandardPropertiesFileCreator(
                file.getParent().getFullPath().toString(),
                bundleName,
                file.getFileExtension());
        displayName = bundleName
                + "[...]." + file.getFileExtension(); //$NON-NLS-1$
    }

    /**
     * @see com.essiembre.eclipse.rbe.ui.editor.resources.ResourceFactory
     *         #getEditorDisplayName()
     */
    @Override
		public String getEditorDisplayName() {
        return displayName;
    }

    /**
     * @see com.essiembre.eclipse.rbe.ui.editor.resources.ResourceFactory
     *         #getSourceEditors()
     */
    @Override
		public SourceEditor[] getSourceEditors() {
        // Java 5 would be better here
        SourceEditor[] editors = new SourceEditor[sourceEditors.size()];
        int i = 0;
        for (Iterator<SourceEditor> iter = sourceEditors.values().iterator(); iter.hasNext();) {
            SourceEditor editor = iter.next();
            editors[i++] = editor;
        }
        return editors;
    }

    /**
     * @see com.essiembre.eclipse.rbe.ui.editor.resources.ResourceFactory
     *         #getPropertiesFileCreator()
     */
    @Override
		public PropertiesFileCreator getPropertiesFileCreator() {
        return fileCreator;
    }

    protected static IResource[] getResources(IFile file)
        throws PartInitException {

        String regex = ResourceFactory.getPropertiesFileRegEx(file);
        IResource[] resources = null;
        try {
            resources = file.getParent().members();
        } catch (CoreException e) {
            throw new PartInitException(
                   "Can't initialize resource bundle editor.", e); //$NON-NLS-1$
        }
        Collection<IResource> validResources = new ArrayList<IResource>();
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            String resourceName = resource.getName();
            if (resource instanceof IFile && resourceName.matches(regex)) {
                validResources.add(resource);
            }
        }
        return validResources.toArray(new IResource[]{});
    }

    @Override
    public SourceEditor addResource(IResource resource, Locale locale) throws PartInitException {
        if (sourceEditors.containsKey(locale))
            throw new IllegalArgumentException("ResourceFactory already contains a resource for locale "+locale);
        SourceEditor editor = createEditor(site, resource, locale);
        sourceEditors.put(editor.getLocale(), editor);
        return editor;
    }
}
