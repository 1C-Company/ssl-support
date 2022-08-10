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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.bsl.typesystem.IInvocationTypesComputerExtension;
import com._1c.g5.v8.dt.bsl.typesystem.IInvocationTypesComputerParametersDependent;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.md.resource.MdTypeUtil;
import com.google.common.base.Strings;

/**
 * The abstract implementation of invocation types computer that dependent on invocation parameter types.
 *
 * @author Dmitriy Marmyshev
 */
public abstract class AbstractCommonModuleCommonFunctionTypesComputer
    implements IInvocationTypesComputerExtension, IInvocationTypesComputerParametersDependent
{

    public static final String COMMON_MODULE_NAME = "Common"; //$NON-NLS-1$

    public static final String COMMON_MODULE_NAME_RU = "ОбщегоНазначения"; //$NON-NLS-1$

    public static final String COMMON_CLIENT_MODULE_NAME = "CommonClient"; //$NON-NLS-1$

    public static final String COMMON_CLIENT_MODULE_NAME_RU = "ОбщегоНазначенияКлиент"; //$NON-NLS-1$

    public static final String COMMON_CLIENT_SERVER_MODULE_NAME = "CommonClientServer"; //$NON-NLS-1$

    public static final String COMMON_CLIENT_SERVER_MODULE_NAME_RU = "ОбщегоНазначенияКлиентСервер"; //$NON-NLS-1$

    private final TypesComputer typesComputer;

    /**
     * Instantiates a new abstract common module common function types computer.
     *
     * @param typesComputer the types computer, cannot be {@code null}.
     */
    protected AbstractCommonModuleCommonFunctionTypesComputer(TypesComputer typesComputer)
    {
        this.typesComputer = typesComputer;
    }

    /**
     * Checks if the invocation is for valid module name "Common".
     *
     * @param inv the invocation
     * @return true, if it is valid module name invocation
     */
    protected boolean isValidModuleNameInvocation(Invocation inv)
    {
        return isValidModuleNameInvocation(inv, COMMON_MODULE_NAME, COMMON_MODULE_NAME_RU);
    }

    /**
     * Checks if the invocation is for valid module name "CommonClient".
     *
     * @param inv the invocation
     * @return true, if it is valid module name invocation
     */
    protected boolean isValidClientModuleNameInvocation(Invocation inv)
    {
        return isValidModuleNameInvocation(inv, COMMON_CLIENT_MODULE_NAME, COMMON_CLIENT_MODULE_NAME_RU);
    }

    /**
     * Checks if the invocation is for valid module name "CommonClientServer".
     *
     * @param inv the invocation
     * @return true, if it is valid module name invocation
     */
    protected boolean isValidClientServerModuleNameInvocation(Invocation inv)
    {
        return isValidModuleNameInvocation(inv, COMMON_CLIENT_SERVER_MODULE_NAME, COMMON_CLIENT_SERVER_MODULE_NAME_RU);
    }

    private boolean isValidModuleNameInvocation(Invocation inv, String name, String nameRu)
    {

        if (inv.getMethodAccess() instanceof DynamicFeatureAccess)
        {
            DynamicFeatureAccess dfa = (DynamicFeatureAccess)inv.getMethodAccess();
            if (dfa.getSource() instanceof StaticFeatureAccess)
            {
                String moduleName = ((StaticFeatureAccess)dfa.getSource()).getName();
                if (moduleName != null && (moduleName.equalsIgnoreCase(nameRu) || moduleName.equalsIgnoreCase(name)))
                {
                    return true;
                }
            }
        }
        else if (inv.getMethodAccess() instanceof StaticFeatureAccess)
        {
            URI uri = EcoreUtil.getURI(inv);
            String moduleName = uri.segment(uri.segmentCount() - 1);
            if (nameRu.equalsIgnoreCase(moduleName) || name.equalsIgnoreCase(moduleName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the type is ref type.
     *
     * @param item the type item to check, cannot be {@code null}.
     * @return true, if the type is ref type
     */
    protected boolean isRefType(TypeItem item)
    {
        String category = McoreUtil.getTypeCategory(item);
        return !Strings.isNullOrEmpty(category) && MdTypeUtil.getRefTypeNames().contains(category)
            && !category.equals("AnyRef"); //$NON-NLS-1$
    }

    /**
     * Gets the return only ref types of given expression.
     *
     * @param expr the expression to compute types, cannot be {@code null}.
     * @return the return ref types, cannot return {@code null}.
     */
    protected List<TypeItem> getReturnRefTypes(Expression expr)
    {
        Environmental environmental = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> paramTypes = typesComputer.computeTypes(expr, environmental.environments());
        return paramTypes.stream().filter(this::isRefType).collect(Collectors.toList());
    }

}
