/**
 *
 */
package com.e1c.ssl.bsl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.Pair;

import com._1c.g5.v8.dt.bsl.model.BslFactory;
import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.ExtendedCollectionType;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.resource.DynamicFeatureAccessComputer;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.ContextDefWithRefItem;
import com._1c.g5.v8.dt.mcore.DerivedProperty;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McoreFactory;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.Method;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeContainerDef;
import com._1c.g5.v8.dt.mcore.TypeContainerRef;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.Environments;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.v8.dt.platform.version.IRuntimeVersionSupport;
import com._1c.g5.v8.dt.platform.version.Version;

/**
 * @author Popov Vitalii
 *
 */
class TypesComputerUtils
{

    private final IRuntimeVersionSupport versionSupport;
    private IEObjectProvider provider;
    private final DynamicFeatureAccessComputer dynamicFeatureAccessComputer;
    private TypesComputer typesComputer;

    public TypesComputerUtils(IRuntimeVersionSupport versionSupport)
    {
        this.versionSupport = versionSupport;
        IResourceServiceProvider rsp =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createURI("*.bsl")); //$NON-NLS-1$
        this.dynamicFeatureAccessComputer = rsp.get(DynamicFeatureAccessComputer.class);
        this.typesComputer = rsp.get(TypesComputer.class);
    }

    protected List<TypeItem> tranformToStructureType(String dstType, Expression srcExpression,
        Invocation context)
    {
        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        TypeItem collectionType = (TypeItem)provider.getProxy(dstType);
        Type type = EcoreUtil2.cloneWithProxies((Type)EcoreUtil.resolve(collectionType, context));

        Environmental envs = EcoreUtil2.getContainerOfType(srcExpression, Environmental.class);
        List<TypeItem> types = this.typesComputer.computeTypes(srcExpression, envs.environments());
        Collection<Pair<Collection<Property>, TypeItem>> collection =
            dynamicFeatureAccessComputer.getAllProperties(types, envs.eResource());

        if (collection.isEmpty())
            return Collections.emptyList();

        Iterator<Pair<Collection<Property>, TypeItem>> iterator = collection.iterator();
        Pair<Collection<Property>, TypeItem> all = iterator.next();

        if (all == null)
            return Collections.emptyList();

        for (Property prop : all.getFirst())
        {
            if (prop instanceof DerivedProperty)
            {
                DerivedProperty newProp = cloneProperty(prop);
                type.getContextDef().getProperties().add(newProp);
            }
        }

        return Collections.singletonList(type);
    }

    /**
     * @param expr
     * @param context
     * @return the list of types
     */
    protected List<TypeItem> transformToFixArray(Expression expr, Invocation context)
    {
        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        if (types.isEmpty())
            return Collections.emptyList();

        Type type = (Type)types.get(0);
        ExtendedCollectionType extendedFixArrayType =
            createExtendedFixArrayType(type.getCollectionElementTypes().allTypes(), provider, context);
        return Collections.singletonList(extendedFixArrayType);
    }

    /**
     * Creates the custom MAP type where kay and value has specific types.
     *
     * @param mapType FIX_MAP or MAP
     * @param expr source type
     * @param context the context
     */
    protected List<TypeItem> createCustomMapWithType(String mapType, Expression expr, Invocation context)
    {
        provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            this.versionSupport.getRuntimeVersionOrDefault(context, Version.LATEST));

        TypeItem dstType = (TypeItem)provider.getProxy(mapType);

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        Type type = (Type)types.get(0);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        List<TypeItem> keysTypes = getTypeFromPropertyCollection(collectionType, "Key"); //$NON-NLS-1$
        List<TypeItem> valuesTypes = getTypeFromPropertyCollection(collectionType, "Value"); //$NON-NLS-1$
        return Collections.singletonList(createCustomMapWithType(dstType, keysTypes, valuesTypes, context));
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

    private Type createCustomMapWithType(TypeItem type, List<TypeItem> keyTypes, List<TypeItem> valueTypes,
        Invocation context)
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

    private DerivedProperty cloneProperty(Property prop)
    {
        DerivedProperty property = McoreFactory.eINSTANCE.createDerivedProperty();
        property.setName(prop.getName());
        property.setNameRu(prop.getNameRu());
        property.setReadable(true);
        property.setWritable(true);
        property.setTypeContainer(McoreFactory.eINSTANCE.createTypeContainerRef());

        ((TypeContainerRef)property.getTypeContainer()).getTypes().addAll(prop.getTypes());

        property.setSource(((DerivedProperty)prop).getSource());
        return property;
    }
}
