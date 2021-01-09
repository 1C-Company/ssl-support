/**
 * Copyright (C) 2021, 1C
 */
package com.e1c.ssl.bsl;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.typesystem.ValueTableDynamicContextDefProvider;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionObjectPropertiesDetailsComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{
    @Inject
    protected IRuntimeVersionSupport versionSupport;

    @Inject
    private ValueTableDynamicContextDefProvider valueTableDynamicContextDefProvider;

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() < 2)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = inv.getParams().get(0);

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

        List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());

        EList<Property> properties = ((Type)types.get(0)).getContextDef().allProperties();

        String propetiesName = getExpressionContent(inv.getParams().get(1));

        return computeTypes(inv, propetiesName, properties);
    }

    protected List<TypeItem> computeTypes(Invocation inv, String propetiesName, EList<Property> properties)
    {
        String content = propetiesName.trim();

        String[] parts = content.split(","); //$NON-NLS-1$

        IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

        TypeItem type = provider.getProxy(IEObjectTypeNames.VALUE_TABLE);

        TypeItem columnPropertyType = provider.getProxy("ValueTableColumn"); //$NON-NLS-1$

        Type valueTable = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(type, inv));

        Type derivedType = valueTableDynamicContextDefProvider.computeDerivedValueTableType(inv, valueTable);

        Type valueTableRowType = (Type)derivedType.getCollectionElementTypes().allTypes().get(0);

        Type valueTableColumnType =
            (Type)ValueTableDynamicContextDefProvider.getColumnCollectionType(derivedType).allTypes().get(0);

        for (String part : parts)
        {
            part = part.trim();

            for (Property property : properties)
            {
                if (property.getName().equalsIgnoreCase(part) || property.getNameRu().equalsIgnoreCase(part))
                {
                    Property newProperty = EcoreUtil2.cloneWithProxies(property);

                    newProperty.setWritable(true);

                    valueTableRowType.getContextDef().getProperties().add(newProperty);

                    Property columnProperty = EcoreUtil2.cloneWithProxies(property);

                    columnProperty.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());

                    ((TypeContainerRef)columnProperty.getTypeContainer()).getTypes().add(columnPropertyType);

                    valueTableColumnType.getContextDef().getProperties().add(columnProperty);

                    break;
                }
            }
        }

        return Lists.newArrayList(derivedType);
    }

}
