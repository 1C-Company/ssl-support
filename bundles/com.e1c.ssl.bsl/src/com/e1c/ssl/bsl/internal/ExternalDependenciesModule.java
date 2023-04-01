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
package com.e1c.ssl.bsl.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.scoping.IScopeProvider;

import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.bsl.typesystem.ValueTableDynamicContextDefProvider;
import com._1c.g5.v8.dt.core.naming.ITopObjectFqnGenerator;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
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
        // V8 DT
        bind(IResourceLookup.class).toService();
        bind(IRuntimeVersionSupport.class).toService();
        bind(IBmModelManager.class).toService();
        bind(ITopObjectFqnGenerator.class).toService();
        bind(IQualifiedNameConverter.class).toService();

        URI uri = URI.createURI("*.bsl"); //$NON-NLS-1$
        final IResourceServiceProvider rsp = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(uri);

        bind(TypesComputer.class).toProvider(() -> rsp.get(TypesComputer.class));
        bind(DynamicFeatureAccessComputer.class).toProvider(() -> rsp.get(DynamicFeatureAccessComputer.class));
        bind(IScopeProvider.class).toProvider(() -> rsp.get(IScopeProvider.class));
        bind(ValueTableDynamicContextDefProvider.class)
            .toProvider(() -> rsp.get(ValueTableDynamicContextDefProvider.class));

    }

}
