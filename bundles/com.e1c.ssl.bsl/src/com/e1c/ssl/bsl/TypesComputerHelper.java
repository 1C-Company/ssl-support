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
 *     Popov vitalii - task #52
 *******************************************************************************/
package com.e1c.ssl.bsl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.BslFactory;
import com._1c.g5.v8.dt.bsl.model.ExtendedCollectionType;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.typesystem.util.TypeSystemUtil;
import com._1c.g5.v8.dt.mcore.ContextDefWithRefItem;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Method;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainer;
import com._1c.g5.v8.dt.mcore.TypeContainerDef;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.Environments;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;

/**
 * @author Popov Vitalii
 *
 */
class TypesComputerHelper
{

    private final IRuntimeVersionSupport versionSupport;
    private IEObjectProvider provider;


    public TypesComputerHelper(IRuntimeVersionSupport versionSupport)
    {
        this.versionSupport = versionSupport;
    }

    /**
     * Transform types STRUCTURE to FIX_STRUCTURE or FIX_STRUCTURE to STRUCTURE.
     *
     * @param type - current type.
     * @param proprties - structure properties.
     * @param transformToFixType - transform direction.
     * @param context
     * @return type.
     */
    protected TypeItem tranformToStructureType(TypeItem type,
        Collection<Pair<Collection<Property>, TypeItem>> proprties, boolean transformToFixType,
        EObject context)
    {
        String dstType = transformToFixType ? IEObjectTypeNames.FIXED_STRUCTURE : IEObjectTypeNames.STRUCTURE;
        if (McoreUtil.getTypeName(type).equals(dstType))
        {
            return type;
        }

        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        TypeItem collectionType = (TypeItem)provider.getProxy(dstType);
        Type newType = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(collectionType, context));

        if (proprties.isEmpty())
            return type;

        Iterator<Pair<Collection<Property>, TypeItem>> iterator = proprties.iterator();
        Pair<Collection<Property>, TypeItem> all = iterator.next();

        if (all == null)
            return type;

        for (Property prop : all.getFirst())
        {
            if (prop instanceof DerivedProperty)
            {
                DerivedProperty newProp = cloneProperty((DerivedProperty)prop);
                newProp.setWritable(!transformToFixType);
                newType.getContextDef().getProperties().add(newProp);
            }
        }

        return newType;
    }

    /**
     * Transform types FIXIED_ARRAY to ARRAY or ARRAY to FIXED_ARRAY.
     *
     * @param expr
     * @param context
     * @return the list of types
     */
    protected TypeItem transformArray(TypeItem type, EObject context, boolean transformToFixCollection)
    {
        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        String typeName = McoreUtil.getTypeName(type);
        if (typeName.equals(IEObjectTypeNames.FIXED_ARRAY) && transformToFixCollection
            || typeName.equals(IEObjectTypeNames.ARRAY) && !transformToFixCollection)
        {
            return type;
        }

        if (!(type instanceof Type))
            return type;

        Type arrayType = (Type)type;

        ExtendedCollectionType extendedFixArrayType;
        if(transformToFixCollection)
        {
            extendedFixArrayType =
                createExtendedFixArrayType(arrayType.getCollectionElementTypes().allTypes(), provider, context);
        }
        else
        {
            extendedFixArrayType = TypeSystemUtil
                .createExtendedArrayType(arrayType.getCollectionElementTypes().allTypes(), provider, context);
        }

        return extendedFixArrayType;
    }

    /**
     * Transform types FIXED_MAP to MAP or MAP to FIXED_MAP.
     *
     * @param mapType FIX_MAP or MAP
     * @param expr source type
     * @param context the context
     */
    protected TypeItem transformMap(TypeItem type, EObject context, boolean transformToFixType)
    {
        String dstTypeName = transformToFixType ? IEObjectTypeNames.FIXED_MAP : IEObjectTypeNames.MAP;
        if (McoreUtil.getTypeName(type).equals(dstTypeName))
        {
            return type;
        }

        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        TypeItem dstType = (TypeItem)provider.getProxy(dstTypeName);

        if (!(type instanceof Type))
            return type;

        Type mapType = (Type)type;
        Type collectionType = (Type)mapType.getCollectionElementTypes().allTypes().get(0);
        List<TypeItem> keysTypes = getTypeFromPropertyCollection(collectionType, "Key"); //$NON-NLS-1$
        List<TypeItem> valuesTypes = getTypeFromPropertyCollection(collectionType, "Value"); //$NON-NLS-1$
        return createCustomMapWithType(dstType, keysTypes, valuesTypes, context);
    }

    /**
     * Creates the custom MAP type where kay and value has specific types.
     *
     * @param keyTypes the key types
     * @param valueTypes the value types
     * @param context the context
     * @return the list of types
     */
    protected List<TypeItem> createCustomMapType(List<TypeItem> keyTypes, List<TypeItem> valueTypes, Invocation context)
    {
        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        TypeItem type = (TypeItem)provider.getProxy(IEObjectTypeNames.MAP);
        Type mapType = createCustomMapWithType(type, keyTypes, valueTypes, context);
        return Collections.singletonList(mapType);
    }

    /**
     * @param collectionType
     * @param propertyName
     * @return item types
     */
    private List<TypeItem> getTypeFromPropertyCollection(Type collectionType, String propertyName)
    {
        return collectionType.getContextDef()
            .allProperties()
            .stream()
            .filter(property -> propertyName.equals(property.getName()))
            .flatMap(property -> property.getTypes().stream())
            .collect(Collectors.toList());
    }

    private Type createCustomMapWithType(TypeItem type, List<TypeItem> keyTypes, List<TypeItem> valueTypes,
        EObject context)
    {
        Type mapType = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(type, context));
        TypeContainerDef newTypeContainer = McoreFactory.eINSTANCE.createTypeContainerDef();
        mapType.setCollectionElementTypes(newTypeContainer);

        TypeItem keyValue = (TypeItem)provider.getProxy(IEObjectTypeNames.KEY_AND_VALUE);
        Type keyValueType = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(keyValue, context));
        if (keyValueType != null)
        {
            newTypeContainer.getTypes().add(keyValueType);
            Optional<Property> property = keyValueType.getContextDef()
                .getProperties()
                .stream()
                .filter(item -> "Key".equals(item.getName())) //$NON-NLS-1$
                .findFirst();
            if (property.isPresent())
            {
                Property columnProperty = property.get();
                TypeContainerRef newTypeContainerRef = McoreFactory.eINSTANCE.createTypeContainerRef();
                columnProperty.setTypeContainer(newTypeContainerRef);
                for (TypeItem keyType : keyTypes)
                {
                    columnProperty.getTypeContainer().allTypes().add(keyType);
                }
            }

            property = keyValueType.getContextDef()
                .getProperties()
                .stream()
                .filter(item -> "Value".equals(item.getName())) //$NON-NLS-1$
                .findFirst();
            if (property.isPresent())
            {
                Property columnProperty = property.get();
                TypeContainerRef newTypeContainerRef = McoreFactory.eINSTANCE.createTypeContainerRef();
                columnProperty.setTypeContainer(newTypeContainerRef);
                for (TypeItem valueType : valueTypes)
                {
                    columnProperty.getTypeContainer().allTypes().add(valueType);
                }
            }
        }

        return mapType;
    }

    /**
     * Copy from {@code TypeSystemUtils.createExtendedArrayType}
    */
    private ExtendedCollectionType createExtendedFixArrayType(List<TypeItem> collectionTypes, IEObjectProvider provider,
        EObject context)
    {
        TypeItem type = (TypeItem)provider.getProxy(IEObjectTypeNames.FIXED_ARRAY);
        Type arrayType = (Type)EcoreUtil.resolve(type, context);
        ExtendedCollectionType extendedType = BslFactory.eINSTANCE.createExtendedCollectionType();
        extendedType.setName(arrayType.getName());
        extendedType.setNameRu(arrayType.getNameRu());
        TypeContainerRef containerRef = McoreFactory.eINSTANCE.createTypeContainerRef();
        containerRef.getTypes().addAll(collectionTypes);
        extendedType.setCollectionElementTypes(containerRef);
        extendedType.setIterable(true);
        ContextDefWithRefItem contextDef = McoreFactory.eINSTANCE.createContextDefWithRefItem();
        contextDef.getRefMethods().addAll(arrayType.getContextDef().allMethods());
        contextDef.getRefProperties().addAll(arrayType.getContextDef().allProperties());
        extendedType.setContextDef(contextDef);
        for (Method method : arrayType.getContextDef().getMethods())
        {
            if (method.isRetVal() && "Get".equals(method.getName())) //$NON-NLS-1$
            {
                Method cloneMethod = (Method)EcoreUtil2.cloneWithProxies((EObject)method);
                cloneMethod.getRetValType().clear();
                cloneMethod.getRetValType().addAll(collectionTypes);
                contextDef.getMethods().add(cloneMethod);
                contextDef.getRefMethods().remove(method);
            }
        }
        extendedType.setCreatedByNewOperator(arrayType.isCreatedByNewOperator());
        extendedType.setEnvironments(new Environments(arrayType.getEnvironments().toArray()));
        extendedType.setExchangeWithServer(arrayType.isExchangeWithServer());
        extendedType.setIndexAccessible(arrayType.isIndexAccessible());
        extendedType.setParentType(arrayType.getParentType());
        extendedType.setSysEnum(arrayType.isSysEnum());
        return extendedType;
    }

    private DerivedProperty cloneProperty(DerivedProperty prop)
    {
        DerivedProperty property = McoreFactory.eINSTANCE.createDerivedProperty();
        property.setName(prop.getName());
        property.setNameRu(prop.getNameRu());
        property.setReadable(prop.isReadable());
        property.setWritable(prop.isWritable());
        property.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());

        TypeContainer typeContainer = property.getTypeContainer();
        if (typeContainer instanceof TypeContainerRef)
            ((TypeContainerRef)typeContainer).getTypes().addAll(prop.getTypes());

        property.setSource(prop.getSource());
        return property;
    }
}
