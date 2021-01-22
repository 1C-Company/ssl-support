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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ValueTableRowToStructure()} that
 * returns the typed properties of the structure according to the column names of a table row.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionValueTableRowToStructure
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() != 1)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
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

            return computeTypes(inv, all.getFirst());
        }
        else
        {
            return Collections.emptyList();
        }

    }

    private List<TypeItem> computeTypes(Invocation inv, Collection<Property> properties)
    {
        IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

        TypeItem structure = provider.getProxy(IEObjectTypeNames.STRUCTURE);

        Type structureType = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(structure, inv));

        for (Property prop : properties)
        {
            if (prop instanceof DerivedProperty)
            {
                DerivedProperty property = McoreFactory.eINSTANCE.createDerivedProperty();
                property.setName(prop.getName());
                property.setNameRu(prop.getNameRu());
                property.setReadable(true);
                property.setWritable(true);
                property.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());

                ((TypeContainerRef)property.getTypeContainer()).getTypes().addAll(prop.getTypes());

                property.setSource(((DerivedProperty)prop).getSource());

                structureType.getContextDef().getProperties().add(property);
            }
        }

        List<TypeItem> collectionTypes = Lists.newArrayList();
        collectionTypes.add(structureType);

        return collectionTypes;
    }

}
