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
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.BooleanLiteral;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.CopyRecursive()} and
 * {@code CommonClient.CopyRecursive()} that returns the same type like first param if second param not set.
 * Returns fixed type if second is {@code true} or unfixed type if second param is {@code false}.
 *
 * @author Artem Iliukhin
 * @author Popov Vitalii - task #52
 *
 */
public class CommonFunctionCopyRecursiveTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{

    private final TypesComputer typesComputer;

    private final TypesComputerHelper typesComputerHelper;

    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;

    @Inject
    public CommonFunctionCopyRecursiveTypesComputer(TypesComputerHelper typesComputerHelper,
        TypesComputer typesComputer, DynamicFeatureAccessComputer dynamicFeatureAccessComputer)
    {
        super(typesComputer);
        this.typesComputer = typesComputer;
        this.typesComputerHelper = typesComputerHelper;
        this.dynamicFeatureAccessComputer = dynamicFeatureAccessComputer;

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
            return typesComputer.computeTypes(expr, envs.environments());
        }

        // Transform to fixed collection
        return transformTypes(expr, inv, needTransformCollectionType.get());
    }

    private List<TypeItem> transformTypes(Expression expr, Invocation context, boolean isResultFixData)
    {

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        if (types.isEmpty())
        {
            return Collections.emptyList();
        }

        // @formatter:off
        return types.stream()
            .map(it -> transformType(it, envs, context, isResultFixData))
            .collect(Collectors.toList());
        //@formatter:on
    }

    private TypeItem transformType(TypeItem type, Environmental envs, Invocation context, boolean isResultFixData)
    {

        TypeItem resultType;

        Collection<Pair<Collection<Property>, TypeItem>> props;
        switch (McoreUtil.getTypeName(type))
        {
        case IEObjectTypeNames.MAP:
            resultType = typesComputerHelper.transformMap(type, context, isResultFixData);
            break;
        case IEObjectTypeNames.STRUCTURE:
            props = dynamicFeatureAccessComputer.getAllProperties(Collections.singletonList(type), envs.eResource());
            resultType = typesComputerHelper.transformStructure(type, props, isResultFixData, context);
            break;
        case IEObjectTypeNames.ARRAY:
            resultType = typesComputerHelper.transformArray(type, context, isResultFixData);
            break;

        case IEObjectTypeNames.FIXED_MAP:
            resultType = typesComputerHelper.transformMap(type, context, isResultFixData);
            break;

        case IEObjectTypeNames.FIXED_STRUCTURE:
            props = dynamicFeatureAccessComputer.getAllProperties(Collections.singletonList(type), envs.eResource());
            resultType = typesComputerHelper.transformStructure(type, props, isResultFixData, context);
            break;

        case IEObjectTypeNames.FIXED_ARRAY:
            resultType = typesComputerHelper.transformArray(type, context, isResultFixData);
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

        if (!(expression instanceof BooleanLiteral))
            return Optional.empty();

        BooleanLiteral returnFixDataParam = (BooleanLiteral)expression;
        return Optional.of(returnFixDataParam.isIsTrue());
    }

}
