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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.ExtendedCollectionType;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.bsl.typesystem.util.TypeSystemUtil;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.UnloadColumn()} that
 * returns an typed array from table column type.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionUnloadColumnTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    private final TypesComputer typesComputer;

    private final ExpressionValueComputer expressionValueComputer;

    @Inject
    public CommonFunctionUnloadColumnTypesComputer(TypesComputer typesComputer, IRuntimeVersionSupport versionSupport,
        DynamicFeatureAccessComputer dynamicFeatureAccessComputer, ExpressionValueComputer expressionValueComputer)
    {
        super(typesComputer, versionSupport, dynamicFeatureAccessComputer);
        this.typesComputer = typesComputer;
        this.expressionValueComputer = expressionValueComputer;
    }

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() != 2 && inv.getParams().size() != 3)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression dataExpression = inv.getParams().get(0);
        if (dataExpression == null)
            return Collections.emptyList();

        Expression nameExpression = inv.getParams().get(1);
        if (nameExpression == null)
            return Collections.emptyList();

        Environmental envs = EcoreUtil2.getContainerOfType(dataExpression, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(dataExpression, envs.environments());
        if (types.isEmpty())
            return Collections.emptyList();

        TypeItem type = types.get(0);
        if (type instanceof Type && ((Type)type).getCollectionElementTypes() != null
            && ((Type)type).getCollectionElementTypes().allTypes().size() == 1
            && ((Type)type).getCollectionElementTypes().allTypes().get(0) instanceof Type)
        {
            Pair<Collection<Property>, TypeItem> all = this.getDynamicFeatureAccessComputer()
                .getAllProperties(((Type)type).getCollectionElementTypes().allTypes(), envs.eResource())
                .stream()
                .findFirst()
                .orElse(null);

            if (all == null)
                return Collections.emptyList();

            Collection<Property> properties = all.getFirst();
            for (Property property : properties)
            {
                Pair<String, Collection<StringLiteral>> expressionContent =
                    expressionValueComputer.getExpressionContent(nameExpression);
                if (expressionContent != null && property.getName().equals(expressionContent.getFirst()))
                {
                    IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
                        versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

                    List<TypeItem> collectionTypes = Lists.newArrayList();
                    collectionTypes.addAll(property.getTypes());

                    ExtendedCollectionType extendedType =
                        TypeSystemUtil.createExtendedArrayType(collectionTypes, provider, inv);

                    return Lists.newArrayList(extendedType);
                }
            }
        }

        return Collections.emptyList();
    }

}
