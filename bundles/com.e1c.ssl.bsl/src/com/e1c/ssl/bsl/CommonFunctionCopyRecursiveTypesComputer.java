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
 *     Popov vitalii - task #52
 *******************************************************************************/
package com.e1c.ssl.bsl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.BooleanLiteral;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
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

    private TypesComputerHelper typeFactory = null;
    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;

    @Inject
    public CommonFunctionCopyRecursiveTypesComputer(IRuntimeVersionSupport versionSupport)
    {
        super();
        this.typeFactory = new TypesComputerHelper(versionSupport);

        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.dynamicFeatureAccessComputer = rsp.get(DynamicFeatureAccessComputer.class);
    }

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
        Optional<Boolean> needTransformCollectionType = needTransformCollectionType(params);
        if (params.size() == 1 || needTransformCollectionType.isEmpty())
        {
            Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
            return this.getTypesComputer().computeTypes(expr, envs.environments());
        }

        // Transform to fixed collection
        return transformTypes(expr, inv, needTransformCollectionType.get());
    }

    private List<TypeItem> transformTypes(Expression expr, Invocation context, boolean isResultFixStructure)
    {

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());
        if (types.isEmpty())
        {
            return Collections.emptyList();
        }

        // @formatter:off
        return types.stream()
            .map(it -> transformType(it, envs, context, isResultFixStructure))
            .collect(Collectors.toList());
        //@formatter:on
    }

    private TypeItem transformType(TypeItem type, Environmental envs, Invocation context, boolean isResultFixStructure)
    {

        TypeItem resultType;

        Collection<Pair<Collection<Property>, TypeItem>> props;
        switch (McoreUtil.getTypeName(type))
        {
        case IEObjectTypeNames.MAP:
            resultType = typeFactory.transformMap(type, context, isResultFixStructure);
            break;
        case IEObjectTypeNames.STRUCTURE:
            props =
                dynamicFeatureAccessComputer.getAllProperties(Collections.singletonList(type), envs.eResource());
            resultType = typeFactory
                .tranformToStructureType(type, props, isResultFixStructure, context);
            break;
        case IEObjectTypeNames.ARRAY:
            resultType = typeFactory.transformArray(type, context, isResultFixStructure);
            break;

        case IEObjectTypeNames.FIXED_MAP:
            resultType = typeFactory.transformMap(type, context, isResultFixStructure);
            break;

        case IEObjectTypeNames.FIXED_STRUCTURE:
            props = dynamicFeatureAccessComputer.getAllProperties(Collections.singletonList(type), envs.eResource());
            resultType = typeFactory.tranformToStructureType(type, props, isResultFixStructure, context);
            break;

        case IEObjectTypeNames.FIXED_ARRAY:
            resultType = typeFactory.transformArray(type, context, isResultFixStructure);
            break;

        default:
            resultType = type;
        }

        return resultType;
    }

    private Optional<Boolean> needTransformCollectionType(EList<Expression> params)
    {
        if (params.size() != 2)
            return Optional.empty();

        Expression expression = params.get(1);
        BooleanLiteral returnFixStructureParam = EcoreUtil2.getContainerOfType(expression, BooleanLiteral.class);

        if (returnFixStructureParam == null)
            return Optional.empty();

        return Optional.of(returnFixStructureParam.isIsTrue());
    }

}
