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
package com.e1c.ssl.bsl;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;

/**
 * Abstract extension computer of invocation types of 1C:SSL API module functions that
 * returns actual types for the ref type of link and attribute names.
 *
 * @author Dmitriy Marmyshev
 *
 */
public abstract class AbstractCommonModuleObjectAttributeValueTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{
    private final TypesComputer typesComputer;

    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;

    protected final IRuntimeVersionSupport versionSupport;

    /**
     * Instantiates a new abstract common module object attribute value types computer.
     *
     * @param typesComputer the types computer, cannot be {@code null}.
     * @param versionSupport the version support, cannot be {@code null}.
     * @param dynamicFeatureAccessComputer the dynamic feature access computer, cannot be {@code null}.
     */
    protected AbstractCommonModuleObjectAttributeValueTypesComputer(TypesComputer typesComputer,
        IRuntimeVersionSupport versionSupport, DynamicFeatureAccessComputer dynamicFeatureAccessComputer)
    {
        super(typesComputer);
        this.typesComputer = typesComputer;
        this.dynamicFeatureAccessComputer = dynamicFeatureAccessComputer;
        this.versionSupport = versionSupport;

    }

    /**
     * Gets the dynamic feature access computer.
     *
     * @return the dynamic feature access computer
     */
    protected DynamicFeatureAccessComputer getDynamicFeatureAccessComputer()
    {
        return dynamicFeatureAccessComputer;
    }

    /**
     * Gets the returning ref types from array containing types.
     *
     * @param expr the expression to compute ref types
     * @return the return ref types
     */
    protected List<TypeItem> getReturnArrayRefTypes(Expression expr)
    {
        Environmental environmental = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> paramTypes = typesComputer.computeTypes(expr, environmental.environments());
        //@formatter:off
        return paramTypes.stream()
            .filter(Type.class::isInstance)
            .map(Type.class::cast)
            .filter(t -> (IEObjectTypeNames.ARRAY.equals(McoreUtil.getTypeName(t))
                || IEObjectTypeNames.FIXED_ARRAY.equals(McoreUtil.getTypeName(t)))
                && t.getCollectionElementTypes() != null
                && !t.getCollectionElementTypes().allTypes().isEmpty())
            .flatMap(t -> t.getCollectionElementTypes().allTypes().stream())
            .filter(this::isRefType)
            .map(TypeItem.class::cast)
            .collect(Collectors.toList());
        //@formatter:on
    }
}
