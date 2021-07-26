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

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com.google.inject.Inject;

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

    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;

    @Inject
    protected IRuntimeVersionSupport versionSupport;

    protected AbstractCommonModuleObjectAttributeValueTypesComputer()
    {
        super();
        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.dynamicFeatureAccessComputer = rsp.get(DynamicFeatureAccessComputer.class);

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
        List<TypeItem> paramTypes = getTypesComputer().computeTypes(expr, environmental.environments());
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

    /**
     * Creates the custom MAP type where kay and value has specific types.
     *
     * @param keyTypes the key types
     * @param valueTypes the value types
     * @param context the context
     * @return the list of types
     */
    protected List<TypeItem> createCustomMapType(List<TypeItem> keyTypes, List<TypeItem> valueTypes, Invocation context)
    {

        TypesComputerHelper typesComputerUtil = new TypesComputerHelper(versionSupport);
        return typesComputerUtil.createCustomMapType(keyTypes, valueTypes, context);
    }

}
