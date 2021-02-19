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

import static com._1c.g5.v8.dt.mcore.McorePackage.Literals.TYPE_ITEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;

import com._1c.g5.v8.dt.bsl.model.BslDerivedPropertySource;
import com._1c.g5.v8.dt.bsl.model.BslFactory;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.StringLiteral;
import com._1c.g5.v8.dt.mcore.ContextDef;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectProvider.Registry;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectAttributesValues()} that
 * returns actual types for the ref type of link and attribute names.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectAttributesValuesTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() < 2)
            return Collections.emptyList();

        if (!isValidModuleNameInvocation(inv))
            return Collections.emptyList();

        Environmental envs = EcoreUtil2.getContainerOfType(inv, Environmental.class);
        //@formatter:off
        List<String> types = getTypesComputer()
            .computeTypes(inv.getParams().get(1), envs.environments())
            .stream().map(McoreUtil::getTypeName)
            .collect(Collectors.toList());
        //@formatter:on

        List<TypeItem> refTypes = getReturnRefTypes(inv.getParams().get(0));

        if (refTypes.isEmpty())
            return Collections.emptyList();

        if (types.contains(IEObjectTypeNames.STRING))
        {
            return computeTypesByString(inv, refTypes);
        }
        else if (types.contains(IEObjectTypeNames.STRUCTURE) || types.contains(IEObjectTypeNames.FIXED_STRUCTURE))
        {
            return computeTypesByStructure(inv, refTypes);
        }
        else if (types.contains(IEObjectTypeNames.ARRAY) || types.contains(IEObjectTypeNames.FIXED_ARRAY))
        {
            return computeTypesByArray(inv, refTypes);

        }
        return Collections.emptyList();
    }

    private List<TypeItem> computeTypesByArray(Invocation inv, List<TypeItem> refTypes)
    {
        List<Pair<String, StringLiteral>> paramContent = getArrayExpressionContent(inv.getParams().get(1));
        if (paramContent.isEmpty())
            return Collections.emptyList();
        Map<String, Pair<String, EObject>> paramStructure = new HashMap<>();
        for (Pair<String, StringLiteral> field : paramContent)
        {
            String key = field.getFirst().replace(".", ""); //$NON-NLS-1$ //$NON-NLS-2$
            EObject source = null;
            if (!key.equalsIgnoreCase(field.getFirst()) && field.getSecond() != null)
            {
                StringLiteral literal = field.getSecond();
                String literalText = field.getFirst();

                BslDerivedPropertySource bslSource = createLiteralSource(literal, literalText, 0);
                source = bslSource;
            }
            paramStructure.put(key, Tuples.create(field.getFirst(), source));
        }
        return computeTypes(inv, refTypes, paramStructure);
    }

    private List<TypeItem> computeTypesByStructure(Invocation inv, List<TypeItem> refTypes)
    {
        Map<String, Triple<StringLiteral, String, StringLiteral>> paramContent =
            getStructureExpressionContent(inv.getParams().get(1));
        if (paramContent.isEmpty())
            return Collections.emptyList();

        Map<String, Pair<String, EObject>> paramStructure = new HashMap<>();
        for (Entry<String, Triple<StringLiteral, String, StringLiteral>> entry : paramContent.entrySet())
        {
            EObject source = null;
            String path = entry.getValue().getSecond() == null ? entry.getKey() : entry.getValue().getSecond();
            if (entry.getValue().getSecond() != null && !entry.getKey().equalsIgnoreCase(entry.getValue().getSecond())
                && entry.getValue().getFirst() != null)
            {
                source = createLiteralSource(entry.getValue().getFirst(), entry.getKey(), 0);
            }
            paramStructure.put(entry.getKey(), Tuples.create(path, source));
        }
        return computeTypes(inv, refTypes, paramStructure);
    }

    private List<TypeItem> computeTypesByString(Invocation inv, List<TypeItem> refTypes)
    {
        String paramContent = getExpressionContent(inv.getParams().get(1));

        if (paramContent == null)
            return Collections.emptyList();

        return computeTypes(inv, refTypes, paramContent);
    }

    private BslDerivedPropertySource createLiteralSource(StringLiteral literal, String literalText,
        int literalStatiPosition)
    {
        BslDerivedPropertySource bslSource = BslFactory.eINSTANCE.createBslDerivedPropertySource();
        Module module = EcoreUtil2.getContainerOfType(literal, Module.class);
        bslSource.setModuleUri(EcoreUtil.getURI(module).toString());
        ICompositeNode node = NodeModelUtils.getNode(literal);
        int localOffset = node.getOffset() + node.getText().indexOf(literalText, literalStatiPosition);
        String methodName = ""; //$NON-NLS-1$
        Method method = EcoreUtil2.getContainerOfType(literal, Method.class);
        if (method != null)
        {
            methodName = method.getName();
            ICompositeNode methodNode = NodeModelUtils.findActualNodeFor(method);
            localOffset = localOffset - methodNode.getTotalOffset();
        }

        bslSource.setLocalOffset(localOffset);
        bslSource.setMethodName(methodName);
        return bslSource;
    }

    protected List<TypeItem> computeTypes(Invocation inv, List<TypeItem> refTypes, String paramContent)
    {

        String content = paramContent.trim().replace(System.lineSeparator(), ""); //$NON-NLS-1$
        String[] attributes = content.replace(" ", "").toLowerCase().split("\\,"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (attributes.length == 0)
            return Collections.emptyList();

        Map<String, Pair<String, EObject>> names = new HashMap<>();
        for (int i = 0; i < attributes.length; i++)
        {
            String attribute = attributes[i];
            names.put(attribute, Tuples.create(attribute, null));
        }

        return createCustomTypeByPropertyNames(refTypes, names, inv);
    }

    protected List<TypeItem> computeTypes(Invocation inv, List<TypeItem> refTypes,
        Map<String, Pair<String, EObject>> paramContent)
    {
        return createCustomTypeByPropertyNames(refTypes, paramContent, inv);
    }

    protected List<TypeItem> createCustomTypeByPropertyNames(List<TypeItem> refTypes,
        Map<String, Pair<String, EObject>> names, EObject context)
    {

        // key = first segmet, Triple-first = second and other segment, second = orignal property key
        // third = source object
        Map<String, List<Triple<List<String>, String, EObject>>> firstSegments =
            new TreeMap<>((o1, o2) -> o1.compareToIgnoreCase(o2));

        for (Entry<String, Pair<String, EObject>> entry : names.entrySet())
        {
            String[] segments = entry.getValue().getFirst().split("\\."); //$NON-NLS-1$
            if (segments.length == 0)
            {
                List<Triple<List<String>, String, EObject>> list =
                    firstSegments.computeIfAbsent(entry.getValue().getFirst(), k -> new ArrayList<>());
                list.add(Tuples.create(Arrays.asList(), entry.getKey(), entry.getValue().getSecond()));
            }
            else
            {
                String first = segments[0];
                List<String> segmentList = new ArrayList<>(Arrays.asList(segments));
                segmentList.remove(0);
                List<Triple<List<String>, String, EObject>> list =
                    firstSegments.computeIfAbsent(first, k -> new ArrayList<>());
                list.add(Tuples.create(segmentList, entry.getKey(), entry.getValue().getSecond()));
            }
        }

        Collection<Pair<Collection<Property>, TypeItem>> all =
            getDynamicFeatureAccessComputer().getAllProperties(refTypes, context.eResource());

        IEObjectProvider provider =
            Registry.INSTANCE.get(TYPE_ITEM, this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));
        EObject proxyType = provider.getProxy(IEObjectTypeNames.STRUCTURE);
        Type structureType = (Type)EcoreUtil2.cloneWithProxies((TypeItem)EcoreUtil.resolve(proxyType, context));

        Map<String, Pair<Property, EObject>> newProperties = new HashMap<>();

        for (Pair<Collection<Property>, TypeItem> pair : all)
        {
            if (firstSegments.isEmpty())
                break;

            for (Property entry : pair.getFirst())
            {
                if (firstSegments.isEmpty())
                    break;
                List<Triple<List<String>, String, EObject>> value = firstSegments.get(entry.getName());
                if (value == null)
                    value = firstSegments.get(entry.getNameRu());

                if (value != null && !value.isEmpty())
                {
                    for (Iterator<Triple<List<String>, String, EObject>> iterator =
                        value.iterator(); iterator.hasNext();)
                    {
                        Triple<List<String>, String, EObject> item = iterator.next();

                        if (item.getFirst().isEmpty())
                        {
                            newProperties.put(item.getSecond(), Tuples.create(entry, item.getThird()));
                            iterator.remove();
                        }
                        else
                        {
                            Property subProperty = getSubProperty(entry, item.getFirst(), context);
                            if (subProperty != null)
                            {
                                newProperties.put(item.getSecond(), Tuples.create(subProperty, item.getThird()));
                                iterator.remove();
                            }
                        }
                    }

                    if (value.isEmpty())
                    {
                        firstSegments.remove(entry.getName());
                        firstSegments.remove(entry.getNameRu());
                    }
                }
            }
        }

        // Create type properties
        ContextDef contextDef = structureType.getContextDef();
        EList<Property> properties = contextDef.getProperties();
        for (Entry<String, Pair<Property, EObject>> entry : newProperties.entrySet())
        {
            properties.add(createPropertyFromSource(entry.getKey(), entry.getValue()));
        }
        return Lists.newArrayList(structureType);
    }

    private Property getSubProperty(Property property, final List<String> properties, EObject context)
    {
        if (properties == null || properties.isEmpty())
            return null;

        String propName = properties.get(0);
        if (propName == null || propName.trim().length() == 0)
            return null;

        Collection<Pair<Collection<Property>, TypeItem>> all =
            getDynamicFeatureAccessComputer().getAllProperties(property.getTypes(), context.eResource());

        for (Pair<Collection<Property>, TypeItem> pair : all)
        {
            for (Property entry : pair.getFirst())
            {
                if (entry.getNameRu().equalsIgnoreCase(propName) || entry.getName().equalsIgnoreCase(propName))
                {
                    if (properties.size() > 1)
                    {
                        List<String> next = new ArrayList<>(properties);
                        next.remove(0);
                        return getSubProperty(entry, next, context);
                    }
                    else
                    {
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    private DerivedProperty createPropertyFromSource(String name, Pair<Property, EObject> source)
    {
        DerivedProperty property = McoreFactory.eINSTANCE.createDerivedProperty();
        if (source.getFirst() instanceof DerivedProperty)
            property.setSource(((DerivedProperty)source.getFirst()).getSource());

        if (name.equalsIgnoreCase(source.getFirst().getName()) || name.equalsIgnoreCase(source.getFirst().getNameRu()))
        {
            property.setName(source.getFirst().getName());
            property.setNameRu(source.getFirst().getNameRu());
        }
        else
        {
            property.setName(name);
            property.setNameRu(name);
            property.setSource(source.getSecond());
        }
        property.setReadable(true);
        property.setWritable(true);

        property.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());
        ((TypeContainerRef)property.getTypeContainer()).getTypes().addAll(source.getFirst().getTypes());
        return property;
    }
}
