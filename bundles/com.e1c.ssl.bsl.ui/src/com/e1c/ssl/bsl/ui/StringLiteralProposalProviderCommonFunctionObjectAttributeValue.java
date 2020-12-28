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

import static com._1c.g5.v8.dt.mcore.McorePackage.Literals.DERIVED_PROPERTY__SOURCE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;

import com._1c.g5.modeling.xtext.scoping.IIndexSlicePredicateService;
import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals;
import com.e1c.ssl.bsl.internal.ui.BslStringLiteralProposalImageProviderForMdObject;
import com.google.inject.Inject;

/**
 * String literal proposal proivder for functions ObjectAttributeValue and ObjectsAttributeValue of common module
 * "Common". Propose attribute names of Ref type that can be selected from data base.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class StringLiteralProposalProviderCommonFunctionObjectAttributeValue
    extends AbstractStringLiteralProposalProviderCommonFunction
{

    private static final Collection<String> ARRAY_INVOCATION_NAMES = List.of("ObjectsAttributeValue".toLowerCase(), //$NON-NLS-1$
        "ЗначениеРеквизитаОбъектов".toLowerCase()); //$NON-NLS-1$

    private static final Collection<String> INVOCATION_NAMES =
        List.of("ObjectAttributeValue".toLowerCase(), "ЗначениеРеквизитаОбъекта".toLowerCase(), //$NON-NLS-1$ //$NON-NLS-2$
            "ObjectsAttributeValue".toLowerCase(), //$NON-NLS-1$
            "ЗначениеРеквизитаОбъектов".toLowerCase()); //$NON-NLS-1$

    @Inject
    private IQualifiedNameProvider qualifiedNameProvider;

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
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() == 1)
        {
            String content = literal.lines(true).get(0);
            String[] properties = content.split("\\.", -1); //$NON-NLS-1$
            if (properties.length > 0)
            {
                List<TypeItem> refTypes = getRefTypes(context.getFirst(), context.getSecond().get(0));

                if (refTypes.isEmpty())
                    return Collections.emptyList();

                Collection<QualifiedName> result = new ArrayList<>();
                List<String> names = new ArrayList<>();

                for (int i = 0; i < properties.length; i++)
                {
                    String propertyName = properties[i];
                    names.add(propertyName);

                    Optional<DerivedProperty> property =
                        getProperties(refTypes, names.toArray(new String[0]), context.getFirst()).stream().filter(
                            p -> isDerivedProperty(p) && (propertyName.equalsIgnoreCase(p.getNameRu())
                                || propertyName.equalsIgnoreCase(p.getName()))).findAny().map(
                                    DerivedProperty.class::cast);
                    if (property.isPresent())
                    {
                        DerivedProperty derived = property.get();
                        QualifiedName fqn = qualifiedNameProvider.getFullyQualifiedName(derived.getSource());
                        if (fqn != null && !fqn.isEmpty())
                        {
                            result.add(fqn);
                        }
                    }
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
            String content = literal.lines(true).get(0);
            String[] properties = content.split("\\."); //$NON-NLS-1$

            if (properties.length > 0)
            {
                List<TypeItem> refTypes = getRefTypes(context.getFirst(), context.getSecond().get(0));

                if (refTypes.isEmpty())
                    return Collections.emptyList();
                List<String> names = new ArrayList<>();

                for (int i = 0; i < properties.length; i++)
                {
                    String propertyName = properties[i];
                    names.add(propertyName);

                    Optional<Property> property =
                        getProperties(refTypes, names.toArray(new String[0]), context.getFirst()).stream().filter(
                            p -> propertyName.equalsIgnoreCase(p.getNameRu())
                                || propertyName.equalsIgnoreCase(p.getName())).findAny();
                    if (property.isPresent() && isDerivedProperty(property.get()))
                    {
                        DerivedProperty derived = property.map(DerivedProperty.class::cast).get();
                        URI uri = EcoreUtil.getURI((EObject)derived.eGet(DERIVED_PROPERTY__SOURCE, false));
                        descriptions.add(new DefaultReferenceDescription(EcoreUtil.getURI(literal), uri, null, i,
                            literal.eResource().getURI()));
                    }
                    else if (property.isPresent())
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
        List<Triple<String, String, IBslStringLiteralProposalImageProvider>> proposals = new ArrayList<>();

        List<TypeItem> refTypes = getRefTypes(context.getFirst(), context.getSecond().get(0));

        if (refTypes.isEmpty())
            return proposals;

        String[] properties = content.split("\\.", -1); //$NON-NLS-1$

        BslStringLiteralProposalImageProviderForMdObject imgProvider =
            new BslStringLiteralProposalImageProviderForMdObject(Literals.DB_OBJECT_ATTRIBUTE);

        for (Property property : getProperties(refTypes, properties, context.getFirst()))
        {
            String name = isRussian ? property.getNameRu() : property.getName();
            properties[properties.length - 1] = name;
            String proposal = String.join(".", properties); //$NON-NLS-1$
            proposals.add(Tuples.create(this.addQuoteToBegin(proposal), proposal, imgProvider));
        }

        return proposals;
    }

    private List<TypeItem> getRefTypes(EObject object, Expression expr)
    {
        if (checkForTypeMethod(object, ARRAY_INVOCATION_NAMES))
        {
            return getReturnArrayRefTypes(expr);
        }
        return getReturnRefTypes(expr);

    }

    protected boolean checkForTypeMethod(EObject object, Collection<String> names)
    {
        if (!(object instanceof Invocation))
        {
            return false;
        }
        FeatureAccess feature = ((Invocation)object).getMethodAccess();
        if (feature.getName() == null)
        {
            return false;
        }
        String featureName = feature.getName().toLowerCase();
        for (String methodName : names)
        {
            if (methodName.equals(featureName))
            {
                return true;
            }
        }
        return false;
    }

}
