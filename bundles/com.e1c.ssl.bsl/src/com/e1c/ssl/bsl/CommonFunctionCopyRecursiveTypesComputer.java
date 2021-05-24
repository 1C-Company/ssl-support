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

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.CopyRecursive()} and
 * {@code CommonClient.CopyRecursive()} that returns the same type like first param.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionCopyRecursiveTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{
    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        EList<Expression> params = inv.getParams();
        if (params.isEmpty() || params.size() > 2)
            return Collections.emptyList();

        if (!isValidAnyModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = params.get(0);
        if (expr != null)
        {
            Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
            return this.getTypesComputer().computeTypes(expr, envs.environments());
        }

        return Collections.emptyList();
    }

}
