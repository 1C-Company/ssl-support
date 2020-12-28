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
package com.e1c.ssl.bsl.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;

import com._1c.g5.modeling.xtext.scoping.IIndexSlicePredicateService;
import com._1c.g5.modeling.xtext.scoping.ISliceFilter;
import com._1c.g5.modeling.xtext.scoping.ISlicedScope;
import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.ui.contentassist.stringliteral.AbstractStringLiteralProposalProvider;
import com._1c.g5.v8.dt.lcore.naming.LowerCaseQualifiedName;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals;
import com.e1c.ssl.bsl.CommonFunctionCommonModuleManagerTypesComputer;
import com.e1c.ssl.bsl.internal.ui.BslStringLiteralProposalImageProviderForMdObject;

/**
 * String literal proposal proivder for function "SubsystemExist" of common module "Common" or "CommonClient".
 * Propose names of MD sybsystems with hierarchy of them.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class StringLiteralProposalProviderSubsystemExist
    extends AbstractStringLiteralProposalProvider
{

    private static final Collection<String> INVOCATION_NAMES =
        Set.of("subsystemexist", "подсистемасуществует"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final Set<String> MODULE_NAMES =
        Set.of(CommonFunctionCommonModuleManagerTypesComputer.COMMON_MODULE_NAME.toLowerCase(),
            CommonFunctionCommonModuleManagerTypesComputer.COMMON_MODULE_NAME_RU.toLowerCase(),
            CommonFunctionCommonModuleManagerTypesComputer.COMMON_CLIENT_MODULE_NAME.toLowerCase(),
            CommonFunctionCommonModuleManagerTypesComputer.COMMON_CLIENT_MODULE_NAME_RU.toLowerCase());

    @Override
    public boolean isAppropriate(Triple<EObject, List<Expression>, Integer> context)
    {
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() != 1)
        {
            return false;
        }
        else if (!this.checkForTypeMethod(context.getFirst(), INVOCATION_NAMES, context.getThird(), 0))
        {
            return false;
        }
        else
        {
            FeatureAccess feature = ((Invocation)context.getFirst()).getMethodAccess();
            if (!(feature instanceof DynamicFeatureAccess))
            {
                return false;
            }
            else if (!(((DynamicFeatureAccess)feature).getSource() instanceof FeatureAccess))
            {
                return false;
            }
            else
            {
                String parentFeatureName =
                    ((FeatureAccess)((DynamicFeatureAccess)feature).getSource()).getName().toLowerCase();
                return MODULE_NAMES.contains(parentFeatureName);
            }
        }
    }

    @Override
    public Collection<QualifiedName> getExportedName(Triple<EObject, List<Expression>, Integer> context,
        IScopeProvider scopeProvider)
    {
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() == 1)
        {
            String content = literal.lines(true).get(0).toLowerCase();
            String[] parts = content.split("\\."); //$NON-NLS-1$
            if (parts.length > 0)
            {
                Collection<QualifiedName> result = new ArrayList<>();
                List<String> names = new ArrayList<>();
                for (int i = 0; i < parts.length; i++)
                {
                    String part = parts[i];
                    names.add(Literals.SUBSYSTEM.getName());
                    names.add(part);
                    result.add(LowerCaseQualifiedName.create(names));
                }
                return result;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<IReferenceDescription> getReferenceDescriptions(Triple<EObject, List<Expression>, Integer> context,
        IScopeProvider scopeProvider)
    {
        List<IReferenceDescription> descriptions = new ArrayList<>();
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() == 1)
        {
            String content = literal.getLines().get(0).toLowerCase();
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split("\\."); //$NON-NLS-1$

            List<QualifiedName> result = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0; i < parts.length; i++)
            {
                String part = parts[i];
                names.add(Literals.SUBSYSTEM.getName());
                names.add(part);
                result.add(QualifiedName.create(names));
            }

            IScope scope = scopeProvider.getScope(context.getFirst(), Literals.CONFIGURATION__SUBSYSTEMS);
            for (int index = result.size() - 1; index >= 0; index--)
            {
                IEObjectDescription topObject = scope.getSingleElement(result.get(index));
                if (topObject != null)
                {
                    String renameObjectUri = topObject.getUserData("renameObjectUri"); //$NON-NLS-1$
                    if (renameObjectUri != null)
                    {
                        descriptions.add(new DefaultReferenceDescription(EcoreUtil.getURI(literal),
                            URI.createURI(renameObjectUri), null, index, literal.eResource().getURI()));
                    }
                }
            }
        }
        return descriptions;
    }

    @Override
    public List<Triple<String, String, IBslStringLiteralProposalImageProvider>> computeProposals(
        Triple<EObject, List<Expression>, Integer> context, String content, IScopeProvider scopeProvider,
        IIndexSlicePredicateService slicePredicateService, boolean isRussian)
    {
        List<Triple<String, String, IBslStringLiteralProposalImageProvider>> proposals = new ArrayList<>();
        String[] segments = content.split("\\.", -1); //$NON-NLS-1$

        BslStringLiteralProposalImageProviderForMdObject imgProvider =
            new BslStringLiteralProposalImageProviderForMdObject(Literals.SUBSYSTEM);

        int totalSegments = segments.length * 2;

        List<String> fqnParts = new ArrayList<>();
        for (int i = 0; i < segments.length; i++)
        {
            String segment = segments[i];
            if (segment != null && !segment.isBlank())
            {
                fqnParts.add(Literals.SUBSYSTEM.getName());
                fqnParts.add(segment);
            }
        }
        QualifiedName fqn = QualifiedName.create(fqnParts);

        IScope scope = scopeProvider.getScope(context.getFirst(), Literals.CONFIGURATION__SUBSYSTEMS);
        List<ISliceFilter> filters = new ArrayList<>();
        filters.add(slicePredicateService.getPredicate("indexSliceScriptIntnl")); //$NON-NLS-1$
        filters.add(input -> input.getQualifiedName().getSegmentCount() == totalSegments
            && (totalSegments < 3 || input.getQualifiedName().startsWith(fqn)));

        Iterator<IEObjectDescription> iterator = ((ISlicedScope)scope).getAllElements(filters).iterator();

        while (iterator.hasNext())
        {
            IEObjectDescription object = iterator.next();
            if (segments.length > 0)
            {
                segments[segments.length - 1] = object.getQualifiedName().getLastSegment();
            }
            String name = String.join(".", segments); //$NON-NLS-1$
            proposals.add(Tuples.create(this.addQuoteToBegin(name), name, imgProvider));
        }
        return proposals;
    }

}
