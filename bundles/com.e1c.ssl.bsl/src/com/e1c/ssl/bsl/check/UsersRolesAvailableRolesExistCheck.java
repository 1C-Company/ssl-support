/*******************************************************************************
 * Copyright (C) 2023, 1C-Soft LLC and others.
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

package com.e1c.ssl.bsl.check;

import static com._1c.g5.v8.dt.bsl.model.BslPackage.Literals.INVOCATION;
import static com._1c.g5.v8.dt.bsl.model.BslPackage.Literals.STRING_LITERAL__LINES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.CONFIGURATION__ROLES;
import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.ROLE;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;

import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StaticFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.core.naming.ITopObjectFqnGenerator;
import com.e1c.g5.v8.dt.check.CheckComplexity;
import com.e1c.g5.v8.dt.check.ICheckParameters;
import com.e1c.g5.v8.dt.check.components.BasicCheck;
import com.e1c.g5.v8.dt.check.settings.IssueSeverity;
import com.e1c.g5.v8.dt.check.settings.IssueType;
import com.google.inject.Inject;

/**
 * Check the function Users.RolesAvailable. First param must contains exist roles.
 * @author Vadim Goncharov
 */
public class UsersRolesAvailableRolesExistCheck
    extends BasicCheck
{

    private static final String CHECK_ID = "users-roles-available-role-exist"; //$NON-NLS-1$

    private static final String COMMONMODULE_USERS_NAME = "Users"; //$NON-NLS-1$

    private static final String COMMONMODULE_USERS_NAME_RU = "Пользователи"; //$NON-NLS-1$

    private static final String METHOD_ISINROLES_NAME = "RolesAvailable"; //$NON-NLS-1$

    private static final String METHOD_ISINROLES_NAME_RU = "РолиДоступны"; //$NON-NLS-1$

    private final IQualifiedNameConverter qualifiedNameConverter;

    private final ITopObjectFqnGenerator topObjectFqnGenerator;

    private final IScopeProvider scopeProvider;

    /**
     * Instantiates a new users roles available roles exist check.
     *
     * @param scopeProvider the scope provider
     * @param qualifiedNameConverter the qualified name converter
     * @param topObjectFqnGenerator the top object fqn generator
     */
    @Inject
    public UsersRolesAvailableRolesExistCheck(IScopeProvider scopeProvider,
        IQualifiedNameConverter qualifiedNameConverter, ITopObjectFqnGenerator topObjectFqnGenerator)
    {
        super();
        this.qualifiedNameConverter = qualifiedNameConverter;
        this.topObjectFqnGenerator = topObjectFqnGenerator;
        this.scopeProvider = scopeProvider;
    }

    @Override
    public String getCheckId()
    {
        return CHECK_ID;
    }

    @Override
    protected void configureCheck(CheckConfigurer builder)
    {
        //TODO добавить extension CommonSenseCheckExtension
        builder.title(Messages.UsersRolesAvailableRolesExistCheck_title)
            .description(Messages.UsersRolesAvailableRolesExistCheck_description)
            .complexity(CheckComplexity.NORMAL)
            .severity(IssueSeverity.MAJOR)
            .issueType(IssueType.WARNING)
            .module()
            .checkedObjectType(INVOCATION);
    }

    @Override
    protected void check(Object object, ResultAcceptor resultAcceptor, ICheckParameters parameters,
        IProgressMonitor monitor)
    {
        Invocation inv = (Invocation)object;
        if (monitor.isCanceled() || !isValidInvocation(inv))
        {
            return;
        }

        EList<Expression> params = inv.getParams();
        if (monitor.isCanceled() || params.isEmpty() || !(params.get(0) instanceof StringLiteral))
        {
            return;
        }

        StringLiteral literal = (StringLiteral)params.get(0);
        String content = String.join("", literal.lines(true)); //$NON-NLS-1$
        if (monitor.isCanceled() || content.isBlank())
        {
            return;
        }

        String[] roles = content.replace(" ", "").split(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        IScope scope = scopeProvider.getScope(inv, CONFIGURATION__ROLES);

        for (String role : roles)
        {
            if (monitor.isCanceled())
            {
                return;
            }

            String fqn = topObjectFqnGenerator.generateStandaloneObjectFqn(ROLE, role);
            if (scope.getSingleElement(qualifiedNameConverter.toQualifiedName(fqn)) == null)
            {
                String message = MessageFormat.format(Messages.UsersRolesAvailableRolesExistCheck_Role_not_exist, role);
                resultAcceptor.addIssue(message, literal, STRING_LITERAL__LINES);
            }

        }

    }

    private boolean isValidInvocation(Invocation invocation)
    {

        if (invocation.getMethodAccess() instanceof DynamicFeatureAccess)
        {
            DynamicFeatureAccess dfa = (DynamicFeatureAccess)invocation.getMethodAccess();
            Expression source = dfa.getSource();
            if (source instanceof StaticFeatureAccess && isSslUsersMethod((StaticFeatureAccess)source, dfa))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isSslUsersMethod(StaticFeatureAccess sfa, DynamicFeatureAccess dfa)
    {
        return (sfa.getName().equalsIgnoreCase(COMMONMODULE_USERS_NAME)
            || sfa.getName().equalsIgnoreCase(COMMONMODULE_USERS_NAME_RU))
            && (dfa.getName().equalsIgnoreCase(METHOD_ISINROLES_NAME)
                || dfa.getName().equalsIgnoreCase(METHOD_ISINROLES_NAME_RU));
    }
}
