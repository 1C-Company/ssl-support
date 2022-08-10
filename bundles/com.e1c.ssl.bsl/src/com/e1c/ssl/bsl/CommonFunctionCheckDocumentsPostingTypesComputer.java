/*******************************************************************************
 * Copyright (C) 2021, 1C-Soft LLC and others.
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
package com.e1c.ssl.bsl;

import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.CheckDocumentsPosting()} that
 * returns the typed array.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionCheckDocumentsPostingTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{
    private final TypesComputer typesComputer;

    @Inject
    public CommonFunctionCheckDocumentsPostingTypesComputer(TypesComputer typesComputer,
        IRuntimeVersionSupport versionSupport, DynamicFeatureAccessComputer dynamicFeatureAccessComputer)
    {
        super(typesComputer, versionSupport, dynamicFeatureAccessComputer);
        this.typesComputer = typesComputer;
    }

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {

        if (inv.getParams().size() != 1)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = inv.getParams().get(0);
        if (expr != null)
        {
            Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
            return typesComputer.computeTypes(expr, envs.environments());
        }

        return Collections.emptyList();

    }

}
