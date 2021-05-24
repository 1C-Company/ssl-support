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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

import com._1c.g5.v8.dt.bsl.model.BslFactory;
import com._1c.g5.v8.dt.bsl.model.BslPackage;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.VariablesScopeSpec;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.CommonModule()} that returns
 * actual common module object by given name.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionCommonModuleManagerTypesComputer
    extends AbstractCommonModuleCommonFunctionTypesComputer
{

    //@formatter:off
    private static final Map<String, QualifiedName> MANAGER_MODULE_BASE = ImmutableMap.<String, QualifiedName>builder()
        .put("Catalogs".toLowerCase(), QualifiedName.create("CatalogManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Справочники".toLowerCase(), QualifiedName.create("CatalogManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Documents".toLowerCase(), QualifiedName.create("DocumentManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Документы".toLowerCase(), QualifiedName.create("DocumentManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartsOfCharacteristicTypes".toLowerCase(), QualifiedName.create("ChartsOfCharacteristicTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланыВидовХарактеристик".toLowerCase(), QualifiedName.create("ChartsOfCharacteristicTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartsOfCalculationTypes".toLowerCase(), QualifiedName.create("ChartsOfCalculationTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланыВидовРасчета".toLowerCase(), QualifiedName.create("ChartsOfCalculationTypeManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ExchangePlans".toLowerCase(), QualifiedName.create("ExchangePlanManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланыОбмена".toLowerCase(), QualifiedName.create("ExchangePlanManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Enums".toLowerCase(), QualifiedName.create("EnumManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Перечисления".toLowerCase(), QualifiedName.create("EnumManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("InformationRegisters".toLowerCase(), QualifiedName.create("InformationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрыСведений".toLowerCase(), QualifiedName.create("InformationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("AccumulationRegisters".toLowerCase(), QualifiedName.create("AccumulationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрыНакопления".toLowerCase(), QualifiedName.create("AccumulationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Constants".toLowerCase(), QualifiedName.create("ConstantManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Константы".toLowerCase(), QualifiedName.create("ConstantManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Sequences".toLowerCase(), QualifiedName.create("SequenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Последовательности".toLowerCase(), QualifiedName.create("SequenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("DocumentJournals".toLowerCase(), QualifiedName.create("DocumentJournalManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ЖурналыДокументов".toLowerCase(), QualifiedName.create("DocumentJournalManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Reports".toLowerCase(), QualifiedName.create("ReportManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Отчеты".toLowerCase(), QualifiedName.create("ReportManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("DataProcessors".toLowerCase(), QualifiedName.create("DataProcessorManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Обработки".toLowerCase(), QualifiedName.create("DataProcessorManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Tasks".toLowerCase(), QualifiedName.create("TaskManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Задачи".toLowerCase(), QualifiedName.create("TaskManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("FilterCriteria".toLowerCase(), QualifiedName.create("FilterCriterionManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("КритерииОтбора".toLowerCase(), QualifiedName.create("FilterCriterionManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("SettingsStorages".toLowerCase(), QualifiedName.create("SettingsStorageManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ХранилищаНастроек".toLowerCase(), QualifiedName.create("SettingsStorageManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("BusinessProcesses".toLowerCase(), QualifiedName.create("BusinessProcesManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("БизнесПроцессы".toLowerCase(), QualifiedName.create("BusinessProcesManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("WSReferences".toLowerCase(), QualifiedName.create("WSReferenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("WSСсылки".toLowerCase(), QualifiedName.create("WSReferenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("CalculationRegisters".toLowerCase(), QualifiedName.create("CalculationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрыРасчета".toLowerCase(), QualifiedName.create("CalculationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ChartsOfAccounts".toLowerCase(), QualifiedName.create("ChartsOfAccountManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ПланыСчетов".toLowerCase(), QualifiedName.create("ChartsOfAccountManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("AccountingRegisters".toLowerCase(), QualifiedName.create("AccountingRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрыБухгалтерии".toLowerCase(), QualifiedName.create("AccountingRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .build();
    //@formatter:on

    public static final String INVOCATION_NAME = "CommonModule"; //$NON-NLS-1$

    public static final String INVOCATION_NAME_RU = "ОбщийМодуль"; //$NON-NLS-1$

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().isEmpty())
            return Collections.emptyList();

        String paramContent = getExpressionContent(inv.getParams().get(0));

        if (inv.getParams().size() != 1 || paramContent == null)
            return Collections.emptyList();

        if (isValidAnyModuleNameInvocation(inv))
        {
            return computeTypes(inv, paramContent);
        }
        return Collections.emptyList();
    }

    protected List<TypeItem> computeTypes(Invocation inv, String paramContent)
    {
        if (paramContent.split(System.lineSeparator()).length == 1)
        {
            String content = paramContent.trim();
            String[] parts = content.split("\\."); //$NON-NLS-1$
            if (parts.length == 1)
            {

                VariablesScopeSpec spec = BslFactory.eINSTANCE.createVariablesScopeSpec();
                Module module = EcoreUtil2.getContainerOfType(inv, Module.class);
                Method method = EcoreUtil2.getContainerOfType(inv, Method.class);
                spec.setModule(module);
                spec.setMethod(method);
                Environmental environmental = EcoreUtil2.getContainerOfType(inv, Environmental.class);
                spec.setEnvironments(environmental.environments());

                IScope scope = getBslScopeProvider().getScope(spec, BslPackage.Literals.FAKE_REFERENCE__PROPERTY);
                IEObjectDescription elem = scope.getSingleElement(QualifiedName.create(parts[0]));
                if (elem != null && elem.getEClass() == McorePackage.Literals.DERIVED_PROPERTY)
                {
                    DerivedProperty result = (DerivedProperty)EcoreUtil.resolve(elem.getEObjectOrProxy(), inv);
                    return result.getTypes();
                }
            }
            else if (parts.length == 2 && MANAGER_MODULE_BASE.containsKey(parts[0].toLowerCase()))
            {

                IScope scope = getBslScopeProvider().getScope(inv, TYPE_DESCRIPTION__TYPES);
                QualifiedName fqn = MANAGER_MODULE_BASE.get(parts[0].toLowerCase()).append(parts[1]);
                IEObjectDescription elem = scope.getSingleElement(fqn);
                if (elem != null && elem.getEClass() == McorePackage.Literals.TYPE)
                {
                    Type type = (Type)EcoreUtil.resolve(elem.getEObjectOrProxy(), inv);
                    return Lists.newArrayList(type);
                }
            }
        }

        return Collections.emptyList();
    }
}
