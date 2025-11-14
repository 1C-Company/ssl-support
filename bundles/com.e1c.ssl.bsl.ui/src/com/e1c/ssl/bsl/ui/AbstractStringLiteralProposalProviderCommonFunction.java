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
package com.e1c.ssl.bsl.ui;

import static com._1c.g5.v8.dt.mcore.McorePackage.Literals.DERIVED_PROPERTY__SOURCE;
import static com.e1c.ssl.bsl.AbstractCommonModuleCommonFunctionTypesComputer.COMMON_MODULE_NAME;
import static com.e1c.ssl.bsl.AbstractCommonModuleCommonFunctionTypesComputer.COMMON_MODULE_NAME_RU;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.bsl.ui.contentassist.stringliteral.AbstractStringLiteralProposalProvider;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.TypeSet;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.md.resource.MdTypeUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com.google.common.base.Strings;

/**
 * Abstract class for string literal proposal provider of common module functions.
 *
 * @author Dmitriy Marmyshev
 *
 */
public abstract class AbstractStringLiteralProposalProviderCommonFunction
    extends AbstractStringLiteralProposalProvider
{
    protected static final Set<String> MODULE_NAMES =
        Set.of(COMMON_MODULE_NAME.toLowerCase(), COMMON_MODULE_NAME_RU.toLowerCase());

    private final TypesComputer typesComputer;

    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;

    public AbstractStringLiteralProposalProviderCommonFunction()
    {
        super();
        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.typesComputer = rsp.get(TypesComputer.class);
        this.dynamicFeatureAccessComputer = rsp.get(DynamicFeatureAccessComputer.class);
    }

    /**
     * Gets actual instanse of {@link TypesComputer  types computer}.
     *
     * @return the types computer
     */
    protected TypesComputer getTypesComputer()
    {
        return typesComputer;
    }

    /**
     * Gets actual instanse of {@link DynamicFeatureAccessComputer dynamic feature access computer}.
     *
     * @return the dynamic feature access computer
     */
    protected DynamicFeatureAccessComputer getDynamicFeatureAccessComputer()
    {
        return dynamicFeatureAccessComputer;
    }

    /**
     * Checks if the type item is user's object ref type, but not AnyRef.
     *
     * @param item the item, cannot be {@code null}.
     * @return true, if item is user's object ref type
     */
    protected boolean isRefType(TypeItem item)
    {
        String category = McoreUtil.getTypeCategory(item);
        return !Strings.isNullOrEmpty(category) && MdTypeUtil.getRefTypeNames().contains(category)
            && !category.equals("AnyRef"); //$NON-NLS-1$
    }

    /**
     * Gets the return ref types of an expression.
     *
     * @param expr the expression to compute types, cannot be {@code null}.
     * @return the return ref types, cannot return {@code null}
     */
    protected List<TypeItem> getReturnRefTypes(Expression expr)
    {
        Environmental environmental = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> paramTypes = getTypesComputer().computeTypes(expr, environmental.environments());
        return paramTypes.stream().filter(this::isRefType).collect(Collectors.toList());
    }

    /**
     * Gets the return ref types containing in array type of expression.
     *
     * @param expr the expression to get ref types from expression's array type, cannot be {@code null}.
     * @return the return ref types from array type
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
     * Gets all properties from ref types with heirarhy of property names split by dot "RefAttribute1.RefAttribue2" etc.
     *
     * @param refTypes the ref types, cannot be {@code null}.
     * @param properties the heirarhy of properties, cannot be {@code null}.
     * @param context the context, cannot be {@code null}.
     * @return the properties
     */
    protected Collection<Property> getProperties(Collection<TypeItem> refTypes, String[] properties, EObject context)
    {
        Collection<TypeItem> allTypes = new ArrayDeque<>();
        for (TypeItem type : refTypes)
        {
            if (type instanceof TypeSet)
            {
                allTypes.addAll(((TypeSet)type).types(context));
            }
            else
            {
                allTypes.add(type);
            }
        }
        Collection<Pair<Collection<Property>, TypeItem>> all =
            getDynamicFeatureAccessComputer().getAllProperties(allTypes, context.eResource());

        if (properties.length > 1)
        {
            Set<TypeItem> result = new HashSet<>();
            String propertyName = properties[0];
            for (Pair<Collection<Property>, TypeItem> pair : all)
            {
                for (Property entry : pair.getFirst())
                {
                    if (!entry.getTypes().isEmpty() && (propertyName.equalsIgnoreCase(entry.getNameRu())
                        || propertyName.equalsIgnoreCase(entry.getName())))
                    {
                        result.addAll(entry.getTypes());
                    }
                }
            }
            if (result.isEmpty())
            {
                return Collections.emptyList();
            }

            String[] other = new String[properties.length - 1];
            System.arraycopy(properties, 1, other, 0, properties.length - 1);
            return getProperties(result, other, context);
        }
        else
        {
            List<Property> result = new ArrayList<>();
            for (Pair<Collection<Property>, TypeItem> pair : all)
            {
                for (Property property : pair.getFirst())
                {
                    result.add(property);
                }
            }
            return result;
        }
    }

    /**
     * Checks if the property is real user's derived property, means property has source.
     *
     * @param property the property, cannot be {@code null}.
     * @return true, if the property is real user's derived property
     */
    protected boolean isDerivedProperty(Property property)
    {
        return property instanceof DerivedProperty && property.eGet(DERIVED_PROPERTY__SOURCE, false) != null;
    }

}
