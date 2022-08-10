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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectsAttributeValue()} that
 * returns actual types for the ref type of link and attribute names.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectAttributeValueTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{
    private final ExpressionValueComputer expressionValueComputer;

    @Inject
    public CommonFunctionObjectAttributeValueTypesComputer(TypesComputer typesComputer,
        IRuntimeVersionSupport versionSupport, DynamicFeatureAccessComputer dynamicFeatureAccessComputer,
        ExpressionValueComputer expressionValueComputer)
    {
        super(typesComputer, versionSupport, dynamicFeatureAccessComputer);
        this.expressionValueComputer = expressionValueComputer;
    }

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() < 2)
            return Collections.emptyList();

        Pair<String, Collection<StringLiteral>> paramContent =
            expressionValueComputer.getExpressionContent(inv.getParams().get(1));

        if (paramContent == null)
            return Collections.emptyList();

        if (isValidModuleNameInvocation(inv))
        {
            return computeTypes(inv, paramContent.getFirst());
        }
        return Collections.emptyList();
    }

    protected List<TypeItem> computeTypes(Invocation inv, String paramContent)
    {

        List<TypeItem> refTypes = getReturnRefTypes(inv.getParams().get(0));

        if (refTypes.isEmpty())
            return Collections.emptyList();

        String content = paramContent.trim().replace(System.lineSeparator(), ""); //$NON-NLS-1$
        if (content.isEmpty())
            return Collections.emptyList();

        String[] properties = content.split("\\.", -1); //$NON-NLS-1$

        return getTypeByPropertyName(refTypes, properties, inv);
    }

    protected List<TypeItem> getTypeByPropertyName(Collection<TypeItem> refTypes, String[] properties, EObject context)
    {
        Set<TypeItem> result = new HashSet<>();

        if (properties == null || properties.length == 0 || properties[0] == null)
        {
            return List.copyOf(result);
        }
        String propertyName = properties[0];

        Collection<Pair<Collection<Property>, TypeItem>> all =
            getDynamicFeatureAccessComputer().getAllProperties(refTypes, context.eResource());

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

        if (properties.length > 1)
        {
            String[] other = new String[properties.length - 1];
            System.arraycopy(properties, 1, other, 0, properties.length - 1);
            return getTypeByPropertyName(result, other, context);

        }
        else
        {
            return List.copyOf(result);
        }

    }
}
