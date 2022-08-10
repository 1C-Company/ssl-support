/*******************************************************************************
 * Copyright (C) 2022, 1C-Soft LLC and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
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
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The computer allow to get various types of string static content from code.
 * This computer supports only simple binary operations and variable assignments, so it returns only single
 * and statically computed string content.
 * In case of cycles or some variable computing logic of string concatenation this computer should brake
 * and return {@code null}.
 *
 *
 * @author Dmitriy Marmyshev
 */
@Singleton
public class ExpressionValueComputer
{

    private static final String METHOD_ARRAY_ADD_RU = "Добавить"; //$NON-NLS-1$

    private static final String METHOD_ARRAY_ADD = "Add"; //$NON-NLS-1$

    private static final String METHOD_STRUCTURE_INSERT_RU = "Вставить"; //$NON-NLS-1$

    private static final String METHOD_STRUCTURE_INSERT = "Insert"; //$NON-NLS-1$

    private final TypesComputer typesComputer;

    /**
     * Instantiates a new expression value computer.
     *
     * @param typesComputer the types computer, cannot be {@code null}.
     */
    @Inject
    public ExpressionValueComputer(TypesComputer typesComputer)
    {
        this.typesComputer = typesComputer;
    }

    /**
     * Gets the static string content of the expression.
     *
     * @param expression the expression, may be {@code null}.
     * @return the expression string content and its source string literals, may return {@code null}
     * if the expression is {@code null} or computing logic of content is not static.
     */
    public Pair<String, Collection<StringLiteral>> getExpressionContent(EObject expression)
    {
        if (expression instanceof StringLiteral)
        {
            StringLiteral literal = (StringLiteral)expression;
            String content = String.join(System.lineSeparator(), literal.lines(false));
            return Tuples.create(content, Arrays.asList(literal));
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
    protected Pair<String, Collection<StringLiteral>> getExpressionContent(StaticFeatureAccess fa)
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
     * @return the expression string content and source string literals, may return {@code null}.
     */
    protected Pair<String, Collection<StringLiteral>> getExpressionContent(BinaryExpression bo)
    {
        if (!bo.getOperation().equals(BinaryOperation.PLUS))
            return null;

        Collection<StringLiteral> literals = new ArrayList<>();
        String leftContent = ""; //$NON-NLS-1$
        Pair<String, Collection<StringLiteral>> left = getExpressionContent(bo.getLeft());
        if (left != null)
        {
            leftContent = left.getFirst();
            literals.addAll(left.getSecond());
        }

        String rightContent = ""; //$NON-NLS-1$
        Pair<String, Collection<StringLiteral>> right = getExpressionContent(bo.getRight());
        if (right != null)
        {
            rightContent = right.getFirst();
            literals.addAll(right.getSecond());
        }

        return Tuples.create(leftContent + rightContent, literals);
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

    /**
     * Gets the 1C structure (aka {@code Map<String, String>}) with string content in keys and values
     * for the given expression.
     * This method computes all insertion of to the structure in current method.
     *
     * @param expression the expression, may be {@code null}.
     * @return the structure expression string content, where map entry key is structure key and map entry value is
     * triple of string literal of the structure key, structure string value for this key and string literal of the
     * structure value, cannot return {@code null}.
     */
    public Map<String, Triple<StringLiteral, String, StringLiteral>> getStructureExpressionContent(EObject expression)
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
                && typesComputer
                    .compute(first, EcoreUtil2.getContainerOfType(first, Environmental.class).environments())
                    .stream()
                    .map(McoreUtil::getTypeName)
                    .anyMatch(IEObjectTypeNames.STRUCTURE::equals))
            {
                return getStructureExpressionContent((StaticFeatureAccess)first);
            }
            else
            {
                String content = getExpressionStringContent(first);
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
                            String paramValue = getExpressionStringContent(valueExpr);
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
        List<String> types = typesComputer.computeTypes(fa, envs.environments())
            .stream()
            .map(McoreUtil::getTypeName)
            .collect(Collectors.toList());
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
                        String key = getExpressionStringContent(param);
                        StringLiteral keyLiteral = null;
                        if (param instanceof StringLiteral)
                            keyLiteral = (StringLiteral)param;
                        String path = key;
                        StringLiteral valueLiteral = null;
                        if (inv.getParams().size() > 1)
                        {
                            param = inv.getParams().get(1);
                            path = getExpressionStringContent(param);
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
     * Gets the list of string content for the expression that is {@code Array} in 1C code
     * with adding strings to the array.
     *
     * @param expression the expression, may be {@code null}.
     * @return the list of string content, cannot return {@code null}.
     */
    public List<Pair<String, StringLiteral>> getArrayExpressionContent(EObject expression)
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
            List<String> types = typesComputer.computeTypes(fa, envs.environments())
                .stream()
                .map(McoreUtil::getTypeName)
                .collect(Collectors.toList());
            if (types.contains(IEObjectTypeNames.STRING))
            {
                Pair<String, Collection<StringLiteral>> content = getExpressionContent(fa);
                if (content != null)
                {
                    StringLiteral literal =
                        content.getSecond().size() == 1 ? content.getSecond().iterator().next() : null;
                    result.add(Tuples.create(content.getFirst(), literal));
                }
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
                String content = getExpressionStringContent(param);
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
                        String content = getExpressionStringContent(param);
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

    private String getExpressionStringContent(EObject expression)
    {
        Pair<String, Collection<StringLiteral>> content = getExpressionContent(expression);
        if (content != null)
        {
            return content.getFirst();
        }
        return null;
    }

}
