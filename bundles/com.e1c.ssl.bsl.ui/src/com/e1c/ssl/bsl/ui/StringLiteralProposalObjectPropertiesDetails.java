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
package com.e1c.ssl.bsl.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.util.Arrays;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;

import com._1c.g5.modeling.xtext.scoping.IIndexSlicePredicateService;
import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage;
import com.e1c.ssl.bsl.internal.ui.BslStringLiteralProposalImageProviderForMdObject;
import com.google.common.collect.Lists;

/**
 * @author Artem Iliukhin
 *
 */
public class StringLiteralProposalObjectPropertiesDetails
    extends AbstractStringLiteralProposalProviderCommonFunction
{
    private static final Collection<String> INVOCATION_NAMES =
        Set.of("objectpropertiesdetails", "описаниесвойствобъекта"); //$NON-NLS-1$ //$NON-NLS-2$

    @Override
    public boolean isAppropriate(Triple<EObject, List<Expression>, Integer> context)
    {
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() != 1)
        {
            return false;
        }
        else if (!this.checkForTypeMethod(context.getFirst(), INVOCATION_NAMES, context.getThird(), 1))
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
            String content = literal.lines(true).get(0);
            String[] properties = content.split("\\."); //$NON-NLS-1$

            if (properties.length > 0)
            {
                List<String> names = new ArrayList<>();

                for (int i = 0; i < properties.length; i++)
                {
                    String propertyName = properties[i];
                    names.add(propertyName);

                    Optional<Property> property =
                        getProperties(context)
                            .stream()
                            .filter(p -> propertyName.equalsIgnoreCase(p.getNameRu())
                                || propertyName.equalsIgnoreCase(p.getName()))
                            .findAny();
                    if (property.isPresent())
                    {
                        URI uri = EcoreUtil.getURI(property.get());
                        descriptions.add(new DefaultReferenceDescription(EcoreUtil.getURI(literal), uri, null, i,
                            literal.eResource().getURI()));
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

        List<Triple<String, String, IBslStringLiteralProposalImageProvider>> proposals = Lists.newArrayList();
        BslStringLiteralProposalImageProviderForMdObject imgProvider =
            new BslStringLiteralProposalImageProviderForMdObject(MdClassPackage.Literals.DB_OBJECT_ATTRIBUTE);

        String[] properties = content.split("\\,", -1); //$NON-NLS-1$
        for (Property property : getProperties(context))
        {
            String name = isRussian ? property.getNameRu() : property.getName();
            if (Arrays.contains(properties, name))
                continue;

            properties[properties.length - 1] = name;
            String proposal = String.join(",", properties); //$NON-NLS-1$
            proposals.add(Tuples.create(this.addQuoteToBegin(proposal), name, imgProvider));
        }

        return proposals;
    }

    private Collection<Property> getProperties(Triple<EObject, List<Expression>, Integer> context)
    {
        Expression expr = context.getSecond().get(0);
        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

        List<TypeItem> types = this.getTypesComputer().computeTypes(expr, envs.environments());

        Pair<Collection<Property>, TypeItem> all = this.getDynamicFeatureAccessComputer()
            .getAllProperties(types, envs.eResource())
            .stream()
            .findFirst()
            .orElse(null);

        if (all == null)
            return Collections.emptyList();

        Property prop = all.getFirst()
            .stream()
            .filter(p -> p.getName().equalsIgnoreCase("Attributes")) //$NON-NLS-1$
            .findFirst()
            .orElse(null);

        if (prop == null)
            return Collections.emptyList();

        all = this.getDynamicFeatureAccessComputer()
            .getAllProperties(((Type)(prop.getTypes().get(0))).getCollectionElementTypes().allTypes(), envs.eResource())
            .stream()
            .findFirst()
            .orElse(null);

        if (all == null)
            return Collections.emptyList();

        return all.getFirst();
    }

}
