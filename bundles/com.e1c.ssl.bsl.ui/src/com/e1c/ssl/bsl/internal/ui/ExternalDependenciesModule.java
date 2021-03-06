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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.xtext.naming.IQualifiedNameProvider;

import com._1c.g5.wiring.AbstractServiceAwareModule;

/**
 * Guice module with external services bindings.
 *
 * @author Dmitriy Marmyshev
 *
 */
class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{

    ExternalDependenciesModule(Plugin plugin)
    {
        super(plugin);
    }

    @Override
    protected void doConfigure()
    {
        bind(IQualifiedNameProvider.class).toService();
    }

}
