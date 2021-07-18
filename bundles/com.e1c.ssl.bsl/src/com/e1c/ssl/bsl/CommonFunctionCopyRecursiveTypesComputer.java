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

import com._1c.g5.v8.dt.bsl.model.BooleanLiteral;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.CopyRecursive()} and
 * {@code CommonClient.CopyRecursive()} that returns the same type like first param.
 *
 * @author Artem Iliukhin
 * @author Popov Vitalii - task #52
 *
 */
public class CommonFunctionCopyRecursiveTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{

    @Inject
    protected IRuntimeVersionSupport versionSupport;

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        EList<Expression> params = inv.getParams();
        if (params.isEmpty() || params.size() > 2)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv) && !isValidClientModuleNameInvocation(inv)
            && !isValidClientServerModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = params.get(0);
        if (expr == null)
            return Collections.emptyList();

        // Without type transfrom
        if (params.size() == 1 || !returnFixCollection(params))
        {

            Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
            return this.getTypesComputer().computeTypes(expr, envs.environments());
        }

        // Transform to fixed collection
        if (returnFixCollection(params))
        {
            return computeFixCollectionType(expr, inv);
        }

        return Collections.emptyList();
    }

    private List<TypeItem> computeFixCollectionType(Expression expr, Invocation context)
    {
        TypesComputerUtils typesComputerUtils = new TypesComputerUtils(versionSupport);

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());
        if (types.isEmpty())
        {
            return Collections.emptyList();
        }

        List<TypeItem> fixCollectionType;
        switch (types.get(0).getName())
        {
        case IEObjectTypeNames.MAP:

            fixCollectionType =
                typesComputerUtils.createCustomMapWithType(IEObjectTypeNames.FIXED_MAP, expr, context);
            break;
        case IEObjectTypeNames.STRUCTURE:
            fixCollectionType = typesComputerUtils
                .tranformToStructureType(IEObjectTypeNames.FIXED_STRUCTURE, expr, context);
            break;
        case IEObjectTypeNames.ARRAY:
            fixCollectionType = typesComputerUtils.transformToFixArray(expr, context);
            break;
        default:
            fixCollectionType = types;
        }

        return fixCollectionType;
    }

    private boolean returnFixCollection(EList<Expression> params)
    {
        if (params.size() != 2)
            return false;

        Expression expression = params.get(1);
        BooleanLiteral returnFixStructureParam = EcoreUtil2.getContainerOfType(expression, BooleanLiteral.class);

        if (returnFixStructureParam == null)
            return false;

        return returnFixStructureParam.isIsTrue();
    }

}
