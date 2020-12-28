/*******************************************************************************
 * Copyright (C) 2020, 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     1C-Soft LLC - initial API and implementation
 *******************************************************************************/
package com.e1c.ssl.bsl.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * BSL UI bundle activator. The activator class controls the plug-in life cycle.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class UiPlugin
    extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "com.e1c.ssl.bsl.ui"; //$NON-NLS-1$

    private static UiPlugin plugin;

    private Injector injector;
    private BundleContext bundleContext;

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static UiPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Writes a status to the plugin log.
     *
     * @param status status to log, cannot be <code>null</code>
     */
    public static void log(IStatus status)
    {
        plugin.getLog().log(status);
    }

    /**
     * Writes a throwable to the plugin log as error status.
     *
     * @param throwable throwable, cannot be <code>null</code>
     */
    public static void logError(Throwable throwable)
    {
        log(createErrorStatus(throwable.getMessage(), throwable));
    }

    /**
     * Creates error status by a given message and cause throwable.
     *
     * @param message status message, cannot be <code>null</code>
     * @param throwable throwable, can be <code>null</code> if not applicable
     * @return status created error status, never <code>null</code>
     */
    public static IStatus createErrorStatus(String message, Throwable throwable)
    {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    /**
     * Creates warning status by a given message.
     *
     * @param message status message, cannot be <code>null</code>
     * @return status created warning status, never <code>null</code>
     */
    public static IStatus createWarningStatus(String message)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    /**
     * Creates warning status by a given message and cause throwable.
     *
     * @param message status message, cannot be <code>null</code>
     * @param throwable throwable, can be <code>null</code> if not applicable
     * @return status created warning status, never <code>null</code>
     */
    public static IStatus createWarningStatus(final String message, Exception throwable)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        super.start(bundleContext);

        this.bundleContext = bundleContext;
        plugin = this;

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        injector = null;
        plugin = null;
        super.stop(bundleContext);
    }

    /**
     * Returns Guice injector of the plugin
     *
     * @return Guice injector of the plugin, never <code>null</code> if plugin is started
     */
    public synchronized Injector getInjector()
    {
        if (injector == null)
        {
            injector = createInjector();
        }
        return injector;
    }

    /**
     * Returns bundle context of the plugin.
     *
     * @return bundle context of the plugin, never <code>null</code> if plugin is started
     */
    protected BundleContext getContext()
    {
        return bundleContext;
    }

    private Injector createInjector()
    {
        try
        {
            return Guice.createInjector(new ExternalDependenciesModule(this));
        }
        catch (Exception e)
        {
            log(createErrorStatus("Failed to create injector for " //$NON-NLS-1$
                + getBundle().getSymbolicName(), e));
            throw new RuntimeException("Failed to create injector for " //$NON-NLS-1$
                + getBundle().getSymbolicName(), e);
        }
    }

}
