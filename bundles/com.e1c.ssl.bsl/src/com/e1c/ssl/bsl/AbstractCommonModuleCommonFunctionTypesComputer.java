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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;

import com._1c.g5.v8.dt.bsl.model.BinaryExpression;
import com._1c.g5.v8.dt.bsl.model.BinaryOperation;
import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.FeatureEntry;
import com._1c.g5.v8.dt.bsl.model.ImplicitVariable;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.OperatorStyleCreator;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.Statement;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.bsl.typesystem.IInvocationTypesComputerExtension;
import com._1c.g5.v8.dt.bsl.typesystem.IInvocationTypesComputerParametersDependent;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.md.resource.MdTypeUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com.google.common.base.Strings;

/**
 * @author Dmitriy Marmyshev
 *
 */
public abstract class AbstractCommonModuleCommonFunctionTypesComputer
    implements IInvocationTypesComputerExtension, IInvocationTypesComputerParametersDependent
{

    public static final String COMMON_MODULE_NAME = "Common"; //$NON-NLS-1$

    public static final String COMMON_MODULE_NAME_RU = "ОбщегоНазначения"; //$NON-NLS-1$

    protected static final String METHOD_ARRAY_ADD_RU = "Добавить"; //$NON-NLS-1$

    protected static final String METHOD_ARRAY_ADD = "Add"; //$NON-NLS-1$

    protected static final String METHOD_STRUCTURE_INSERT_RU = "Вставить"; //$NON-NLS-1$

    protected static final String METHOD_STRUCTURE_INSERT = "Insert"; //$NON-NLS-1$

    private final IScopeProvider bslScope;

    private final TypesComputer typesComputer;

    public AbstractCommonModuleCommonFunctionTypesComputer()
    {
        super();
        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.bslScope = rsp.get(IScopeProvider.class);
        this.typesComputer = rsp.get(TypesComputer.class);
    }

    /**
     * Checks if the invocation is for valid module name "Common".
     *
     * @param inv the invocation
     * @return true, if it is valid module name invocation
     */
    protected boolean isValidModuleNameInvocation(Invocation inv)
    {

        if (inv.getMethodAccess() instanceof DynamicFeatureAccess)
        {
            DynamicFeatureAccess dfa = (DynamicFeatureAccess)inv.getMethodAccess();
            if (dfa.getSource() instanceof StaticFeatureAccess)
            {
                String moduleName = ((StaticFeatureAccess)dfa.getSource()).getName();
                if (moduleName != null && (moduleName.equalsIgnoreCase(COMMON_MODULE_NAME_RU)
                    || moduleName.equalsIgnoreCase(COMMON_MODULE_NAME)))
                {
                    return true;
                }
            }
        }
        else if (inv.getMethodAccess() instanceof StaticFeatureAccess)
        {
            URI uri = EcoreUtil.getURI(inv);
            String moduleName = uri.segment(uri.segmentCount() - 1);
            if (COMMON_MODULE_NAME.equalsIgnoreCase(moduleName) || COMMON_MODULE_NAME_RU.equalsIgnoreCase(moduleName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the expression string content.
     *
     * @param expression the expression, may be {@code null}.
     * @return the expression string content, may return {@code null}.
     */
    protected String getExpressionContent(EObject expression)
    {
        if (expression instanceof StringLiteral)
        {
            StringLiteral literal = (StringLiteral)expression;
            return String.join(System.lineSeparator(), literal.lines(true));
        }
        else if (expression instanceof StaticFeatureAccess)
        {
            return getExpressionContent((StaticFeatureAccess)expression);
        }
        else if (expression instanceof BinaryExpression)
        {
            return getExpressionContent((BinaryExpression)expression);
        }
        return null;
    }

    /**
     * Gets the expression string content for static feature access.
     *
     * @param fa the static feature access, cannot be {@code null}.
     * @return the expression string content, may return {@code null}.
     */
    protected String getExpressionContent(StaticFeatureAccess fa)
    {
        Environmental envs = EcoreUtil2.getContainerOfType(fa, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(fa, envs.environments());
        if (types.stream().map(McoreUtil::getTypeName).anyMatch(IEObjectTypeNames.STRING::equals))
        {
            SimpleStatement last = getVariableStatement(fa);
            if (last != null)
            {
                return getExpressionContent(last.getRight());
            }

            String varName = fa.getName();
            if (varName == null)
                return null;

            List<? extends Statement> statements = getAllStatements(fa);
            ICompositeNode node = NodeModelUtils.getNode(fa);
            last = getVariableStatement(statements, varName, node.getOffset());
            if (last != null)
            {
                return getExpressionContent(last.getRight());
            }
        }
        return null;
    }

    /**
     * Gets the computed string content for binary expression.
     *
     * @param bo the binary expression, cannot be {@code null}.
     * @return the expression string content, may return {@code null}.
     */
    protected String getExpressionContent(BinaryExpression bo)
    {
        if (!bo.getOperation().equals(BinaryOperation.PLUS))
            return null;

        String left = getExpressionContent(bo.getLeft());
        if (left == null)
            left = ""; //$NON-NLS-1$
        String right = getExpressionContent(bo.getRight());
        if (right == null)
            right = ""; //$NON-NLS-1$
        return left + right;
    }

    private SimpleStatement getVariableStatement(StaticFeatureAccess fa)
    {
        for (FeatureEntry entry : fa.getFeatureEntries())
        {
            EObject feature = entry.getFeature();
            if (feature instanceof ImplicitVariable && feature.eContainer() instanceof SimpleStatement)
            {
                return (SimpleStatement)feature.eContainer();
            }
        }
        return null;
    }

    private SimpleStatement getVariableStatement(List<? extends Statement> allStatements, final String varName,
        int varOffset)
    {
        if (allStatements == null)
            return null;

        for (int i = allStatements.size() - 1; i >= 0; i--)
        {
            Statement statement = allStatements.get(i);
            if (statement instanceof SimpleStatement)
            {
                SimpleStatement simple = (SimpleStatement)statement;
                if (simple.getLeft() instanceof StaticFeatureAccess
                    && varName.equalsIgnoreCase(((StaticFeatureAccess)simple.getLeft()).getName())
                    && NodeModelUtils.getNode(simple).getEndOffset() < varOffset)
                {
                    return simple;
                }
            }
            else
            {
                SimpleStatement simple =
                    getVariableStatement(EcoreUtil2.eAllOfType(statement, SimpleStatement.class), varName, varOffset);
                if (simple != null)
                {
                    return simple;
                }
            }
        }
        return null;
    }

    /**
     * Gets the structure expression string content for expression
     *
     * @param expression the expression, may be {@code null}.
     * @return the structure expression string content, where map entry key is structure key and map entry value is
     * triple of string literal of the structure key, structure string value for this key and string literal of the
     * structure value, cannot return {@code null}.
     */
    protected Map<String, Triple<StringLiteral, String, StringLiteral>> getStructureExpressionContent(
        EObject expression)
    {
        if (expression instanceof OperatorStyleCreator)
        {
            return getStructureExpressionContent((OperatorStyleCreator)expression);
        }
        else if (expression instanceof StaticFeatureAccess)
        {
            return getStructureExpressionContent((StaticFeatureAccess)expression);
        }

        return new HashMap<>();
    }

    /**
     * Gets the structure expression string content for operator style creator of new structure.
     *
     * @param osc the perator style creator of new structure, cannot be {@code null}.
     * @return the structure expression string content, where map entry key is structure key and map entry value is
     * triple of string literal of the structure key, structure string value for this key and string literal of the
     * structure value, cannot return {@code null}.
     */
    protected Map<String, Triple<StringLiteral, String, StringLiteral>> getStructureExpressionContent(
        OperatorStyleCreator osc)
    {
        Map<String, Triple<StringLiteral, String, StringLiteral>> result = new HashMap<>();

        Set<String> typeName = Set.of(IEObjectTypeNames.STRUCTURE, IEObjectTypeNames.FIXED_STRUCTURE);
        if (!osc.getParams().isEmpty() && typeName.contains(McoreUtil.getTypeName(osc.getType())))
        {
            Expression first = osc.getParams().get(0);
            if (osc.getParams().size() == 1 && first instanceof StaticFeatureAccess
                && getTypesComputer().compute(first,
                    EcoreUtil2.getContainerOfType(first, Environmental.class).environments()).stream().map(
                        McoreUtil::getTypeName).anyMatch(IEObjectTypeNames.STRUCTURE::equals))
            {
                return getStructureExpressionContent((StaticFeatureAccess)first);
            }
            else
            {
                String content = getExpressionContent(first);
                if (content != null)
                {
                    StringLiteral keyLiteral = null;
                    if (first instanceof StringLiteral)
                        keyLiteral = (StringLiteral)first;
                    String[] keys = content.replace("\r", "").replace("\n", "").replace(" ", "").split(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

                    for (int i = 0; i < keys.length; i++)
                    {
                        String key = keys[i].replace(".", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        String path = keys[i];
                        StringLiteral valueLiteral = null;
                        if (i + 1 < osc.getParams().size())
                        {
                            Expression valueExpr = osc.getParams().get(i + 1);
                            String paramValue = getExpressionContent(valueExpr);
                            if (paramValue != null)
                            {
                                path = paramValue.trim();
                                if (valueExpr instanceof StringLiteral)
                                    valueLiteral = (StringLiteral)valueExpr;
                            }
                        }
                        result.put(key, Tuples.create(keyLiteral, path, valueLiteral));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the structure expression string content for static feature access of the structure.
     *
     * @param fa the static feature access, cannot be {@code null}.
     * @return the structure expression string content, where map entry key is structure key and map entry value is
     * triple of string literal of the structure key, structure string value for this key and string literal of the
     * structure value, cannot return {@code null}.
     */
    protected Map<String, Triple<StringLiteral, String, StringLiteral>> getStructureExpressionContent(
        StaticFeatureAccess fa)
    {
        Map<String, Triple<StringLiteral, String, StringLiteral>> result = new HashMap<>();

        Set<String> typeName = Set.of(IEObjectTypeNames.STRUCTURE, IEObjectTypeNames.FIXED_STRUCTURE);

        Environmental envs = EcoreUtil2.getContainerOfType(fa, Environmental.class);
        List<String> types =
            typesComputer.computeTypes(fa, envs.environments()).stream().map(McoreUtil::getTypeName).collect(
                Collectors.toList());
        if (types.stream().anyMatch(typeName::contains))
        {
            final String varName = fa.getName();
            if (varName == null)
                return result;

            List<? extends Statement> statements = getAllStatements(fa);

            SimpleStatement last = getVariableStatement(fa);
            if (last != null)
            {
                result = getStructureExpressionContent(last.getRight());
            }
            else
            {
                ICompositeNode node = NodeModelUtils.getNode(fa);
                last = getVariableStatement(statements, varName, node.getOffset());
                if (last != null)
                {
                    result = getStructureExpressionContent(last.getRight());
                }
            }
            boolean found = last == null;
            for (Statement statemnt : statements)
            {
                if (!found && statemnt.equals(last))
                    continue;
                found = true;
                if (statemnt instanceof SimpleStatement && ((SimpleStatement)statemnt).getLeft() instanceof Invocation)
                {
                    SimpleStatement simple = (SimpleStatement)statemnt;
                    Invocation inv = (Invocation)simple.getLeft();
                    FeatureAccess methodAccess = inv.getMethodAccess();
                    String name = methodAccess.getName();
                    if (!inv.getParams().isEmpty()
                        && (METHOD_STRUCTURE_INSERT_RU.equalsIgnoreCase(name)
                            || METHOD_STRUCTURE_INSERT.equalsIgnoreCase(name))
                        && methodAccess instanceof DynamicFeatureAccess
                        && ((DynamicFeatureAccess)methodAccess).getSource() instanceof StaticFeatureAccess
                        && varName.equalsIgnoreCase(
                            ((StaticFeatureAccess)((DynamicFeatureAccess)methodAccess).getSource()).getName()))
                    {
                        Expression param = inv.getParams().get(0);
                        String key = getExpressionContent(param);
                        StringLiteral keyLiteral = null;
                        if (param instanceof StringLiteral)
                            keyLiteral = (StringLiteral)param;
                        String path = key;
                        StringLiteral valueLiteral = null;
                        if (inv.getParams().size() > 1)
                        {
                            param = inv.getParams().get(1);
                            path = getExpressionContent(param);
                            if (param instanceof StringLiteral)
                                valueLiteral = (StringLiteral)param;
                        }
                        if (key != null)
                            result.put(key, Tuples.create(keyLiteral, path, valueLiteral));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the all statements in current scope: method or module statements.
     *
     * @param fa the static feature access, cannot be {@code null}.
     * @return the all statements, cannot return {@code null}.
     */
    @SuppressWarnings("unused")
    protected List<? extends Statement> getAllStatements(StaticFeatureAccess fa)
    {
        @Nullable
        Method method = EcoreUtil2.getContainerOfType(fa, Method.class);
        if (method == null)
        {
            Module module = EcoreUtil2.getContainerOfType(fa, Module.class);
            return module.allStatements();
        }
        else
        {
            return EcoreUtil2.eAllOfType(method, SimpleStatement.class);
        }
    }

    /**
     * Gets the list of string content for expression.
     *
     * @param expression the expression, may be {@code null}.
     * @return the list of string content, cannot return {@code null}.
     */
    protected List<Pair<String, StringLiteral>> getArrayExpressionContent(EObject expression)
    {
        List<Pair<String, StringLiteral>> result = new ArrayList<>();

        if (expression instanceof OperatorStyleCreator)
        {
            List<Pair<String, StringLiteral>> init = getArrayExpressionContent((OperatorStyleCreator)expression);
            if (!init.isEmpty())
                result.addAll(init);
        }
        else if (expression instanceof StaticFeatureAccess)
        {
            List<Pair<String, StringLiteral>> init = getArrayExpressionContent((StaticFeatureAccess)expression);
            if (!init.isEmpty())
                result.addAll(init);
        }
        return result;
    }

    /**
     * Gets the list of string content for operator style creater of array.
     *
     * @param osc the operator style creater of array, cannot be {@code null}.
     * @return the list of string content, cannot return {@code null}.
     */
    protected List<Pair<String, StringLiteral>> getArrayExpressionContent(OperatorStyleCreator osc)
    {
        List<Pair<String, StringLiteral>> result = new ArrayList<>();

        if (osc.getParams().size() == 1 && IEObjectTypeNames.ARRAY.equals(McoreUtil.getTypeName(osc.getType()))
            && osc.getParams().get(0) instanceof StaticFeatureAccess)
        {
            StaticFeatureAccess fa = (StaticFeatureAccess)osc.getParams().get(0);
            Environmental envs = EcoreUtil2.getContainerOfType(fa, Environmental.class);
            List<String> types =
                typesComputer.computeTypes(fa, envs.environments()).stream().map(McoreUtil::getTypeName).collect(
                    Collectors.toList());
            if (types.contains(IEObjectTypeNames.STRING))
            {
                String content = getExpressionContent(fa);
                if (content != null)
                    result.add(Tuples.create(content, null));
            }
            else if (types.contains(IEObjectTypeNames.FIXED_ARRAY))
            {
                return getArrayExpressionContent(fa);
            }
        }
        else if (!osc.getParams().isEmpty() && IEObjectTypeNames.ARRAY.equals(McoreUtil.getTypeName(osc.getType())))
        {
            for (Expression param : osc.getParams())
            {
                String content = getExpressionContent(param);
                if (content != null && param instanceof StringLiteral)
                    result.add(Tuples.create(content, (StringLiteral)param));
                else if (content != null)
                    result.add(Tuples.create(content, null));
            }
        }
        else if (osc.getParams().size() == 1
            && IEObjectTypeNames.FIXED_ARRAY.equals(McoreUtil.getTypeName(osc.getType())))
        {
            return getArrayExpressionContent(osc.getParams().get(0));
        }
        return result;
    }

    /**
     * Gets the list of string content for static featue access
     *
     * @param fa the static featue access, cannot be {@code null}.
     * @return the list of string content, cannot return {@code null}.
     */
    protected List<Pair<String, StringLiteral>> getArrayExpressionContent(StaticFeatureAccess fa)
    {
        List<Pair<String, StringLiteral>> result = new ArrayList<>();

        Environmental envs = EcoreUtil2.getContainerOfType(fa, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(fa, envs.environments());
        if (types.stream().map(McoreUtil::getTypeName).anyMatch(IEObjectTypeNames.ARRAY::equals))
        {
            final String varName = fa.getName();
            if (varName == null)
                return result;

            List<? extends Statement> statements = getAllStatements(fa);

            SimpleStatement last = getVariableStatement(fa);
            if (last != null)
            {
                result = getArrayExpressionContent(last.getRight());
            }
            else
            {
                ICompositeNode node = NodeModelUtils.getNode(fa);
                last = getVariableStatement(statements, varName, node.getOffset());
                if (last != null)
                {
                    result = getArrayExpressionContent(last.getRight());
                }
            }
            boolean found = last == null;

            for (Statement statemnt : statements)
            {
                if (!found && statemnt.equals(last))
                    continue;
                found = true;
                if (statemnt instanceof SimpleStatement && ((SimpleStatement)statemnt).getLeft() instanceof Invocation)
                {
                    SimpleStatement simple = (SimpleStatement)statemnt;
                    Invocation inv = (Invocation)simple.getLeft();
                    FeatureAccess methodAccess = inv.getMethodAccess();
                    String name = methodAccess.getName();
                    if (!inv.getParams().isEmpty()
                        && (METHOD_ARRAY_ADD_RU.equalsIgnoreCase(name) || METHOD_ARRAY_ADD.equalsIgnoreCase(name))
                        && methodAccess instanceof DynamicFeatureAccess
                        && ((DynamicFeatureAccess)methodAccess).getSource() instanceof StaticFeatureAccess
                        && varName.equalsIgnoreCase(
                            ((StaticFeatureAccess)((DynamicFeatureAccess)methodAccess).getSource()).getName()))
                    {
                        Expression param = inv.getParams().get(0);
                        String content = getExpressionContent(param);
                        if (content != null && param instanceof StringLiteral)
                            result.add(Tuples.create(content, (StringLiteral)param));
                        else if (content != null)
                            result.add(Tuples.create(content, null));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the bsl scope provider.
     *
     * @return the bsl scope provider actual instance
     */
    protected IScopeProvider getBslScopeProvider()
    {
        return bslScope;
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
        List<TypeItem> paramTypes = getTypesComputer().computeTypes(expr, environmental.environments());
        return paramTypes.stream().filter(this::isRefType).collect(Collectors.toList());
    }

    /**
     * Gets the types computer.
     *
     * @return the types computer actual instance
     */
    protected TypesComputer getTypesComputer()
    {
        return typesComputer;
    }

}
