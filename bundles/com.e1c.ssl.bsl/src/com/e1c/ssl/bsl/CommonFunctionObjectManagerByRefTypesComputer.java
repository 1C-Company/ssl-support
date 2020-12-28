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

import static com._1c.g5.v8.dt.mcore.McorePackage.Literals.TYPE_DESCRIPTION__TYPES;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com.google.common.collect.ImmutableMap;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectManagerByRef()} that returns
 * actual MD object manager module by reference type of the MD object.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectManagerByRefTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{

    //@formatter:off
    protected static final Map<String, QualifiedName> REF_MANAGER_MODULE_BASE =
        ImmutableMap.<String, QualifiedName>builder()
        .put("Catalog".toLowerCase(), QualifiedName.create("CatalogManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Справочник".toLowerCase(), QualifiedName.create("CatalogManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Document".toLowerCase(), QualifiedName.create("DocumentManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Документ".toLowerCase(), QualifiedName.create("DocumentManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartOfCharacteristicTypes".toLowerCase(), QualifiedName.create("ChartsOfCharacteristicTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланВидовХарактеристик".toLowerCase(), QualifiedName.create("ChartsOfCharacteristicTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartOfCalculationTypes".toLowerCase(), QualifiedName.create("ChartsOfCalculationTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланВидовРасчета".toLowerCase(), QualifiedName.create("ChartsOfCalculationTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ExchangePlan".toLowerCase(), QualifiedName.create("ExchangePlanManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланОбмена".toLowerCase(), QualifiedName.create("ExchangePlanManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Enum".toLowerCase(), QualifiedName.create("EnumManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Перечисление".toLowerCase(), QualifiedName.create("EnumManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Task".toLowerCase(), QualifiedName.create("TaskManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Задача".toLowerCase(), QualifiedName.create("TaskManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("BusinessProcess".toLowerCase(), QualifiedName.create("BusinessProcesManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("БизнесПроцесс".toLowerCase(), QualifiedName.create("BusinessProcesManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartOfAccounts".toLowerCase(), QualifiedName.create("ChartsOfAccountManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланСчетов".toLowerCase(), QualifiedName.create("ChartsOfAccountManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .build();
    //@formatter:on

    private static final String REF = "ref"; //$NON-NLS-1$

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() != 1 || !isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        List<TypeItem> refTypes = getReturnRefTypes(inv.getParams().get(0));
        if (!refTypes.isEmpty())
        {
            //@formatter:off
            return refTypes.stream()
                .map(t -> computeTypes(inv, t))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            //@formatter:on
        }
        return Collections.emptyList();
    }

    private TypeItem computeTypes(Invocation inv, TypeItem type)
    {
        String name = McoreUtil.getTypeName(type);
        String[] parts = name.split("\\."); //$NON-NLS-1$
        if (parts.length != 2)
            return null;

        String key = parts[0].toLowerCase();
        if (key.endsWith(REF))
            key = key.substring(0, key.length() - REF.length());
        if (REF_MANAGER_MODULE_BASE.containsKey(key))
        {
            QualifiedName fqn = REF_MANAGER_MODULE_BASE.get(key).append(parts[1]);

            return getTypesByFqn(fqn, inv);
        }

        return null;
    }

    protected TypeItem getTypesByFqn(QualifiedName fqn, EObject context)
    {
        IScope scope = getBslScopeProvider().getScope(context, TYPE_DESCRIPTION__TYPES);
        IEObjectDescription elem = scope.getSingleElement(fqn);
        if (elem != null && elem.getEClass() == McorePackage.Literals.TYPE)
        {
            Type type = (Type)EcoreUtil.resolve(elem.getEObjectOrProxy(), context);
            return type;
        }
        return null;
    }

}
