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

import java.util.Locale;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.essiembre.eclipse.rbe.model.workbench.RBEPreferences;
import com.essiembre.eclipse.rbe.model.workbench.files.PropertiesFileCreator;

/**
 * Responsible for creating resources related to a given file structure.
 * @author Pascal Essiembre (essiembre@users.sourceforge.net)
 * @version $Author: essiembre $ $Revision: 1.4 $ $Date: 2005/08/03 03:15:59 $
 */
public abstract class ResourceFactory {

    /** Class name of Properties file editor (Eclipse 3.1). */
    protected static final String PROPERTIES_EDITOR_CLASS_NAME =
            "org.eclipse.jdt.internal.ui.propertiesfileeditor." //$NON-NLS-1$
          + "PropertiesFileEditor"; //$NON-NLS-1$

    /** Token to replace in a regular expression with a bundle name. */
    private static final String TOKEN_BUNDLE_NAME = "BUNDLENAME"; //$NON-NLS-1$
    /** Token to replace in a regular expression with a file extension. */
    private static final String TOKEN_FILE_EXTENSION =
            "FILEEXTENSION"; //$NON-NLS-1$
    /** Regex to match a properties file. */
    private static final String PROPERTIES_FILE_REGEX =
            "^(" + TOKEN_BUNDLE_NAME + ")"  //$NON-NLS-1$//$NON-NLS-2$
          + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})" //$NON-NLS-1$
          + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\." //$NON-NLS-1$
          + TOKEN_FILE_EXTENSION + ")$"; //$NON-NLS-1$
    
    /**
     * Gets the editor display name.
     * @return editor display name
     */
    public abstract String getEditorDisplayName();
    
    /**
     * Gets the source editors associated with this RBE.
     * @return source editors
     */
    public abstract SourceEditor[] getSourceEditors();

    /**
     * Adds the given resource to the factory.
     * 
     * @param resource The resource to add.
     * @param locale The locale of the resource.
     */
    public abstract SourceEditor addResource(IResource resource, Locale locale) throws PartInitException;
    
    /**
     * Gets a properties file creator.
     * @return properties file creator
     */
    public abstract PropertiesFileCreator getPropertiesFileCreator();
    
    /**
     * Creates a resource factory based on given arguments.
     * @param site eclipse editor site
     * @param file file used to create factory
     * @return resource factory
     * @throws CoreException problem creating factory
     */
    public static ResourceFactory createFactory(IEditorSite site, IFile file)
            throws CoreException {
        if (isNLResource(file)) {
            return new NLResourceFactory(site, file);
        }
        return new StandardResourceFactory(site, file);
    }
    
    protected SourceEditor createEditor(
            IEditorSite site, IResource resource, Locale locale)
            throws PartInitException {
        
        ITextEditor textEditor = null;
        if (resource != null && resource instanceof IFile) {
            IEditorInput newEditorInput =
                    new FileEditorInput((IFile) resource);
            textEditor = null;
            try {
                // Use PropertiesFileEditor if available
                textEditor = (TextEditor) Class.forName(
                        PROPERTIES_EDITOR_CLASS_NAME).newInstance();
            } catch (Exception e) {
                // Use default editor otherwise
                textEditor = new TextEditor();
            }
            textEditor.init(site, newEditorInput);
        }
        if (textEditor != null) {
            return new SourceEditor(textEditor, locale, (IFile) resource);
        }
        return null;
    }

    
    private static boolean isNLResource(IFile file)
            throws PartInitException {
        /*
         * Check if NL is supported.
         */
        if (!RBEPreferences.getSupportNL()) {
            return false;
        }

        /*
         * Check if there is an NL directory
         */
        IContainer container = file.getParent();
        IResource nlDir = null;
        while (container != null
                && (nlDir == null || !(nlDir instanceof Folder))) {
            nlDir = container.findMember("nl"); //$NON-NLS-1$
            container = container.getParent();
        }
        if (nlDir == null || !(nlDir instanceof Folder)) {
            return false;
        }

        /*
         * Ensures NL directory is part of file path, or that file dir
         * is parent of NL directory.
         */
        IPath filePath = file.getFullPath();
        IPath nlDirPath = nlDir.getFullPath();
        if (!nlDirPath.isPrefixOf(filePath)
                && !filePath.removeLastSegments(1).isPrefixOf(nlDirPath)) {
            return false;
        }
        
        /*
         * Ensure that there are no other files which could make a standard
         * resource bundle.
         */
        if (StandardResourceFactory.getResources(file).length > 1) {
             return false;
        }
        return true;
    }
    
    protected static String getBundleName(IFile file) {
        String name = file.getName();
        String regex = "^(.*?)" //$NON-NLS-1$
                + "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})" //$NON-NLS-1$
                + "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\." //$NON-NLS-1$
                + file.getFileExtension() + ")$"; //$NON-NLS-1$
        return name.replaceFirst(regex, "$1"); //$NON-NLS-1$
    }
    protected static String getPropertiesFileRegEx(IFile file) {
        String bundleName = getBundleName(file);
        return PROPERTIES_FILE_REGEX.replaceFirst(
                TOKEN_BUNDLE_NAME, bundleName).replaceFirst(
                        TOKEN_FILE_EXTENSION, file.getFileExtension());
    }

    
}
