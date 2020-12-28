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

import static com._1c.g5.v8.dt.bsl.documentation.comment.LinkPart.MD_OBJECT_MANAGERS;
import static com._1c.g5.v8.dt.bsl.documentation.comment.LinkPart.MD_OBJECT_MANAGERS_RU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
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
import com._1c.g5.modeling.xtext.scoping.ISlicedScope;
import com._1c.g5.v8.dt.bsl.model.DynamicFeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.FeatureAccess;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.bsl.ui.contentassist.stringliteral.AbstractStringLiteralProposalProvider;
import com._1c.g5.v8.dt.lcore.naming.LowerCaseQualifiedName;
import com._1c.g5.v8.dt.lcore.util.CaseInsensitiveString;
import com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals;
import com.e1c.ssl.bsl.CommonFunctionCommonModuleManagerTypesComputer;
import com.e1c.ssl.bsl.internal.ui.BslStringLiteralProposalImageProviderForMdObject;
import com.google.common.collect.Lists;

/**
 * String literal proposal proivder for function "CommonModule" of common module "Common" or "CommonClient".
 * Propose names of common modules or full names of MD object manager modules.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class StringLiteralProposalProviderCommonModuleManager
    extends AbstractStringLiteralProposalProvider
{

    private static final Collection<String> INVOCATION_NAMES =
        Set.of(CommonFunctionCommonModuleManagerTypesComputer.INVOCATION_NAME.toLowerCase(),
            CommonFunctionCommonModuleManagerTypesComputer.INVOCATION_NAME_RU.toLowerCase());

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
    public List<Triple<String, String, IBslStringLiteralProposalImageProvider>> computeProposals(
        Triple<EObject, List<Expression>, Integer> context, String content, IScopeProvider scopeProvider,
        IIndexSlicePredicateService slicePredicateService, boolean isRussian)
    {
        List<Triple<String, String, IBslStringLiteralProposalImageProvider>> proposals = new ArrayList<>();
        String[] segments = content.split("\\.", -1); //$NON-NLS-1$

        Map<CaseInsensitiveString, EReference> managers = isRussian ? MD_OBJECT_MANAGERS_RU : MD_OBJECT_MANAGERS;

        String managerSegment = null;
        if (segments.length > 0 && segments[0] != null && !segments[0].isBlank())
            managerSegment = segments[0];

        EReference ref = null;
        if (managerSegment != null)
            ref = managers.get(new CaseInsensitiveString(segments[0]));

        if (ref == null)
            ref = Literals.CONFIGURATION__COMMON_MODULES;

        BslStringLiteralProposalImageProviderForMdObject imgProvider =
            new BslStringLiteralProposalImageProviderForMdObject(getEClassByRef(ref));

        IScope scope = scopeProvider.getScope(context.getFirst(), ref);
        Iterator<IEObjectDescription> iterator =
            ((ISlicedScope)scope).getAllElements(this.getFilterScope(slicePredicateService, isRussian)).iterator();

        while (iterator.hasNext())
        {
            IEObjectDescription object = iterator.next();
            if (managerSegment == null)
            {
                proposals.add(Tuples.create(this.addQuoteToBegin(object.getQualifiedName().getLastSegment()),
                    object.getQualifiedName().getLastSegment(), imgProvider));
            }
            else
            {
                String preffix = String.join(".", managerSegment, object.getQualifiedName().getLastSegment()); //$NON-NLS-1$
                proposals.add(Tuples.create(this.addQuoteToBegin(preffix), preffix, imgProvider));
            }
        }

        if (managerSegment == null)
        {
            for (Entry<CaseInsensitiveString, EReference> entry : managers.entrySet())
            {
                EClass eClass = getEClassByRef(entry.getValue());
                imgProvider = new BslStringLiteralProposalImageProviderForMdObject(eClass);
                String preffix = entry.getKey().getString() + "."; //$NON-NLS-1$
                proposals.add(Tuples.create(this.addQuoteToBegin(preffix), preffix, imgProvider));
            }
        }

        return proposals;
    }

    private EClass getEClassByRef(EReference ref)
    {
        if (ref == null)
            return null;
        EClassifier eType = ref.getEType();
        if (eType instanceof EClass)
            return (EClass)eType;
        return null;
    }

    @Override
    public Collection<QualifiedName> getExportedName(Triple<EObject, List<Expression>, Integer> context,
        IScopeProvider scopeProvider)
    {
        StringLiteral literal = (StringLiteral)(context.getSecond()).get(context.getThird());
        if (literal.getLines().size() == 1)
        {
            String content = literal.getLines().get(0).toLowerCase();
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split("\\."); //$NON-NLS-1$
            if (parts.length == 1)
            {
                return Lists.newArrayList(LowerCaseQualifiedName.create(Literals.COMMON_MODULE.getName(), parts[0]));
            }

            if (parts.length == 2)
            {
                CaseInsensitiveString managerName = new CaseInsensitiveString(parts[0]);
                EReference ref = null;
                if (MD_OBJECT_MANAGERS_RU.containsKey(managerName))
                {
                    ref = MD_OBJECT_MANAGERS_RU.get(managerName);
                }
                else if (MD_OBJECT_MANAGERS.containsKey(managerName))
                {
                    ref = MD_OBJECT_MANAGERS.get(managerName);
                }
                EClass eClass = getEClassByRef(ref);
                if (eClass != null)
                {
                    return Lists.newArrayList(LowerCaseQualifiedName.create(eClass.getName(), parts[1]));
                }
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
            String content = literal.getLines().get(0);
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split("\\."); //$NON-NLS-1$

            EReference ref = null;
            String objectName = null;
            int index = -1;
            EClass eClass = null;

            if (parts.length == 1)
            {
                ref = Literals.CONFIGURATION__COMMON_MODULES;
                eClass = Literals.COMMON_MODULE;
                objectName = parts[0];
                index = 0;
            }
            else if (parts.length == 2)
            {
                CaseInsensitiveString managerName = new CaseInsensitiveString(parts[0]);

                if (MD_OBJECT_MANAGERS_RU.containsKey(managerName))
                {
                    ref = MD_OBJECT_MANAGERS_RU.get(managerName);
                }
                else if (MD_OBJECT_MANAGERS.containsKey(managerName))
                {
                    ref = MD_OBJECT_MANAGERS.get(managerName);
                }
                eClass = getEClassByRef(ref);
                objectName = parts[1];
                index = 1;
            }

            if (ref != null && eClass != null && objectName != null)
            {
                List<String> names = new ArrayList<>();
                names.add(eClass.getName());
                names.add(objectName);
                QualifiedName moduleFqn = QualifiedName.create(names);

                IScope scope = scopeProvider.getScope(context.getFirst(), ref);
                IEObjectDescription moduleObject = scope.getSingleElement(moduleFqn);
                if (moduleObject != null)
                {
                    addDescription(moduleObject, descriptions, literal, index);
                }
                else
                {
                    for (IEObjectDescription element : scope.getAllElements())
                    {
                        QualifiedName fqn = element.getQualifiedName();
                        if (fqn != null && moduleFqn.equalsIgnoreCase(fqn))
                        {
                            addDescription(element, descriptions, literal, index);
                            break;
                        }
                    }
                }
            }
        }

        return descriptions;
    }

    private void addDescription(IEObjectDescription element, List<IReferenceDescription> descriptions,
        StringLiteral literal, int index)
    {
        String renameObjectUri = element.getUserData("renameObjectUri"); //$NON-NLS-1$
        if (renameObjectUri != null)
        {
            descriptions.add(new DefaultReferenceDescription(EcoreUtil.getURI(literal), URI.createURI(renameObjectUri),
                null, index, literal.eResource().getURI()));
        }
        else if (element.getEObjectURI() != null)
        {
            descriptions.add(new DefaultReferenceDescription(EcoreUtil.getURI(literal), element.getEObjectURI(), null,
                index, literal.eResource().getURI()));
        }
    }

}
