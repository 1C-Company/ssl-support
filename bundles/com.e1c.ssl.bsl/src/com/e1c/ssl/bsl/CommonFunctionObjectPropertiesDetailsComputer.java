/*******************************************************************************
 * Copyright (C), 2021 1C-Soft LLC and others.
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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Strings;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.typesystem.ValueTableDynamicContextDefProvider;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainer;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectPropertiesDetails()} that
 * returns a table with columns from the properties of the metadata object.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionObjectPropertiesDetailsComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    private final ValueTableDynamicContextDefProvider valueTableDynamicContextDefProvider;

    protected CommonFunctionObjectPropertiesDetailsComputer()
    {
        super();
        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.valueTableDynamicContextDefProvider = rsp.get(ValueTableDynamicContextDefProvider.class);
    }

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() < 2)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        String propetiesName = getExpressionContent(inv.getParams().get(1));
        if (Strings.isEmpty(propetiesName))
            return Collections.emptyList();

        Expression expr = inv.getParams().get(0);
        if (expr instanceof StaticFeatureAccess)
        {
            Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

            List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());
            if (types.isEmpty())
                return Collections.emptyList();

            Pair<Collection<Property>, TypeItem> all = this.getDynamicFeatureAccessComputer()
                .getAllProperties(types, envs.eResource())
                .stream()
                .findFirst()
                .orElse(null);

            if (all == null)
                return Collections.emptyList();

            return computeTypes(inv, propetiesName, all.getFirst());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private List<TypeItem> computeTypes(Invocation inv, String propetiesName, Collection<Property> properties)
    {
        String content = propetiesName.trim().replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$

        String[] parts = content.split(","); //$NON-NLS-1$

        IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

        TypeItem type = provider.getProxy(IEObjectTypeNames.VALUE_TABLE);
        TypeItem columnPropertyType = provider.getProxy("ValueTableColumn"); //$NON-NLS-1$

        Type valueTable = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(type, inv));

        Type derivedType = valueTableDynamicContextDefProvider.computeDerivedValueTableType(inv, valueTable);

        Type valueTableRowType = (Type)derivedType.getCollectionElementTypes().allTypes().get(0);

        TypeContainer columns = ValueTableDynamicContextDefProvider.getColumnCollectionType(derivedType);
        if (columns == null)
            return Collections.emptyList();

        Type valueTableColumnType = (Type)columns.allTypes().get(0);

        for (String part : parts)
        {
            if (Strings.isEmpty(part))
                continue;

            Property property = properties.parallelStream()
                .filter(p -> p.getNameRu().equalsIgnoreCase(part) || p.getName().equalsIgnoreCase(part))
                .findFirst()
                .orElse(null);

            if (property != null)
            {
                Property newProperty = EcoreUtil2.cloneWithProxies(property);
                newProperty.setWritable(true);
                valueTableRowType.getContextDef().getProperties().add(newProperty);

                Property columnProperty = EcoreUtil2.cloneWithProxies(property);
                columnProperty.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());
                ((TypeContainerRef)columnProperty.getTypeContainer()).getTypes().add(columnPropertyType);
                valueTableColumnType.getContextDef().getProperties().add(columnProperty);
            }
        }

        return Lists.newArrayList(derivedType);
    }

}
