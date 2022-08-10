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
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectManagerByFullName()} that
 * returns actual MD object manager module by the full name of the MD object.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectManagerByFullNameTypesComputer
    extends CommonFunctionObjectManagerByRefTypesComputer
{
    //@formatter:off
    protected static final Map<String, QualifiedName> NON_REF_MANAGER_MODULE_BASE =
        ImmutableMap.<String, QualifiedName>builder()
        .put("InformationRegister".toLowerCase(), QualifiedName.create("InformationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрСведений".toLowerCase(), QualifiedName.create("InformationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("AccumulationRegister".toLowerCase(), QualifiedName.create("AccumulationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрНакопления".toLowerCase(), QualifiedName.create("AccumulationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Constant".toLowerCase(), QualifiedName.create("ConstantManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Константа".toLowerCase(), QualifiedName.create("ConstantManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Sequence".toLowerCase(), QualifiedName.create("SequenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Последовательность".toLowerCase(), QualifiedName.create("SequenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("DocumentJournal".toLowerCase(), QualifiedName.create("DocumentJournalManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ЖурналДокументов".toLowerCase(), QualifiedName.create("DocumentJournalManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Report".toLowerCase(), QualifiedName.create("ReportManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Отчет".toLowerCase(), QualifiedName.create("ReportManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("DataProcessor".toLowerCase(), QualifiedName.create("DataProcessorManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("Обработка".toLowerCase(), QualifiedName.create("DataProcessorManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("FilterCriterion".toLowerCase(), QualifiedName.create("FilterCriterionManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("КритерийОтбора".toLowerCase(), QualifiedName.create("FilterCriterionManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("SettingsStorage".toLowerCase(), QualifiedName.create("SettingsStorageManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("ХранилищеНастроек".toLowerCase(), QualifiedName.create("SettingsStorageManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("WSReference".toLowerCase(), QualifiedName.create("WSReferenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("WSСсылка".toLowerCase(), QualifiedName.create("WSReferenceManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("CalculationRegister".toLowerCase(), QualifiedName.create("CalculationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрРасчета".toLowerCase(), QualifiedName.create("CalculationRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("AccountingRegister".toLowerCase(), QualifiedName.create("AccountingRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .put("РегистрБухгалтерии".toLowerCase(), QualifiedName.create("AccountingRegisterManager")) //$NON-NLS-1$ //$NON-NLS-2$
        .build();
    //@formatter:on

    private final ExpressionValueComputer expressionValueComputer;

    @Inject
    public CommonFunctionObjectManagerByFullNameTypesComputer(TypesComputer typesComputer, IScopeProvider scopeProvider,
        ExpressionValueComputer expressionValueComputer)
    {
        super(typesComputer, scopeProvider);
        this.expressionValueComputer = expressionValueComputer;
    }

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().isEmpty())
            return Collections.emptyList();

        Pair<String, Collection<StringLiteral>> paramContent =
            expressionValueComputer.getExpressionContent(inv.getParams().get(0));

        if (inv.getParams().size() != 1 || paramContent == null)
            return Collections.emptyList();

        if (isValidModuleNameInvocation(inv))
        {
            return computeTypes(inv, paramContent.getFirst());
        }
        return Collections.emptyList();
    }

    protected List<TypeItem> computeTypes(Invocation inv, String paramContent)
    {
        String content = paramContent.trim();
        String[] parts = content.split("\\."); //$NON-NLS-1$
        if (parts.length == 2 && paramContent.split(System.lineSeparator()).length == 1)
        {
            String key = parts[0].toLowerCase();
            if (NON_REF_MANAGER_MODULE_BASE.containsKey(key))
            {
                QualifiedName fqn = NON_REF_MANAGER_MODULE_BASE.get(key).append(parts[1]);

                TypeItem type = getTypesByFqn(fqn, inv);
                return type == null ? Collections.emptyList() : Lists.newArrayList(type);
            }
            else if (REF_MANAGER_MODULE_BASE.containsKey(key))
            {
                QualifiedName fqn = REF_MANAGER_MODULE_BASE.get(key).append(parts[1]);

                TypeItem type = getTypesByFqn(fqn, inv);
                return type == null ? Collections.emptyList() : Lists.newArrayList(type);
            }
        }

        return Collections.emptyList();
    }
}
