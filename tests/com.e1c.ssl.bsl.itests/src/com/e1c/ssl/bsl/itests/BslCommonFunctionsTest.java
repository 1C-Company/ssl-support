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
package com.e1c.ssl.bsl.itests;

import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.FIXED_ARRAY;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.FIXED_MAP;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.FIXED_STRUCTURE;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.KEY_AND_VALUE;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.MAP;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.STRUCTURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.Statement;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.core.operations.ProjectPipelineJob;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.core.platform.IDtProjectManager;
import com._1c.g5.v8.dt.core.platform.IWorkspaceOrchestrator;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
import com._1c.g5.wiring.ServiceAccess;
import com.e1c.ssl.internal.bsl.itests.BslIdeTestCaseBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Tests for API functions of 1C:SSL API modules "Common" or "CommonClient".
 *
 * @author Dmitriy Marmyshev
 *
 */
public class BslCommonFunctionsTest
    extends BslIdeTestCaseBase
{

    private static final String PATH_COMMON_MODULE_TEST = "/src/CommonModules/ТестовыйМодуль/Module.bsl"; //$NON-NLS-1$

    private static final String PROJECT_NAME = "CommonFunctions"; //$NON-NLS-1$

    private static final String FOLDER_NAME = "./resources/"; //$NON-NLS-1$

    private IProject project;

    private TypesComputer typesComputer;

    private String oldFileContent;

    private IFile oldFile;

    public BslCommonFunctionsTest()
    {
        super(false, false);
    }

    /**
     * Builds project for test
     * @throws Exception
     */
    @Before
    public void initTestProjects() throws Exception
    {
        project = testingWorkspace.getWorkspaceRoot().getProject(PROJECT_NAME);
        if (!project.isAccessible())
        {
            initProject(PROJECT_NAME);
        }
    }

    @After
    public void afterTest() throws Exception
    {
        restoreState(oldFileContent, oldFile);
    }

    @Override
    protected void initilizeSpecialServicesByRSP(IResourceServiceProvider rsp)
    {
        typesComputer = rsp.get(TypesComputer.class);
        assertNotNull(typesComputer);
    }

    @Test
    public void testFunctionCommonModule() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)), Lists.newArrayList("CommonModule.УсловныйМодуль")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionManagerByRef() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-ref.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)), Lists.newArrayList("CatalogManager.Товары")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionManagerByRefDynamic() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-ref-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(1)), Lists.newArrayList("CatalogManager.Товары")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionManagerByFullName() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-full-name.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)),
            Lists.newArrayList("InformationRegisterManager.ШтрихКоды")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionManagerByFullNameRef() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-full-name-ref.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)), Lists.newArrayList("CatalogManager.Товары")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionClientCommonModule() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-client.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)),
            Lists.newArrayList("CommonModule.ОбщегоНазначенияКлиент")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionCatalogMangerModule() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-object-manager.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)), Lists.newArrayList("CatalogManager.Товары")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributeValue() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attribute-value.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(0)), Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributeValueDynamic() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attribute-value-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(1)), Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectsAttributeValue() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/objects-attribute-value.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());

        Expression right = getRightExpr(method.getStatements().get(0));
        Environmental envs = EcoreUtil2.getContainerOfType(right, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(right, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(MAP, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Key", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Value", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectsAttributeValueDynamic() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/objects-attribute-value-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());

        Expression right = getRightExpr(method.getStatements().get(1));
        Environmental envs = EcoreUtil2.getContainerOfType(right, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(right, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(MAP, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Key", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Value", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValues() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(0));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesDynamic() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$ //$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesArrayCtorSubProperty() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array-ctor-subproperty.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("ПоставщикКод", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("РодительСсылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesArrayCtor() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array-ctor.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesArray() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(6, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(5));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesComputeString() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-compute-strings.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(5, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(4));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Код", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Наименование", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesStringVar() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-string-var.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesStructureCtor() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure-ctor.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(3, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(2));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("ОсновнойПоставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesStructureSubProperty() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure-subproperty.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(7, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(6));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("ОсновнойПоставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Код", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка2", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectAttributesValuesStructure() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(6, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(5));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectsAttributesValues() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(0));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(MAP, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Key", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Value", Lists.newArrayList(STRUCTURE)); //$NON-NLS-1$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        Optional<Property> found = collectionType.getContextDef()
            .getProperties()
            .stream()
            .filter(p -> "Value".equals(p.getName())) //$NON-NLS-1$
            .findFirst();
        assertTrue(found.isPresent());
        Property property = found.get();
        Type valuePropertyType = (Type)property.getTypes().get(0);

        Optional<com._1c.g5.v8.dt.mcore.Method> methodFound =
            type.getContextDef().getMethods().stream().filter(m -> "Get".equals(m.getName())).findFirst(); //$NON-NLS-1$
        assertTrue(methodFound.isPresent());
        com._1c.g5.v8.dt.mcore.Method methodGet = methodFound.get();
        Type methodGetType = (Type)methodGet.getRetValType().get(0);
        assertEquals(valuePropertyType, methodGetType);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(valuePropertyType));
        assertNotNull(valuePropertyType.getContextDef());

        Map<String, Collection<String>> expected2 = Maps.newHashMap();
        expected2.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(valuePropertyType.getContextDef().getProperties(), expected2, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectsAttributesValuesDynamic() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(MAP, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Key", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Value", Lists.newArrayList(STRUCTURE)); //$NON-NLS-1$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        Optional<Property> found = collectionType.getContextDef()
            .getProperties()
            .stream()
            .filter(p -> "Value".equals(p.getName())) //$NON-NLS-1$
            .findFirst();
        assertTrue(found.isPresent());
        Property property = found.get();
        type = (Type)property.getTypes().get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected2 = Maps.newHashMap();
        expected2.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected2, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectsAttributesValuesStringVar() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values-string-var.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(MAP, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Key", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Value", Lists.newArrayList(STRUCTURE)); //$NON-NLS-1$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        Optional<Property> found = collectionType.getContextDef()
            .getProperties()
            .stream()
            .filter(p -> "Value".equals(p.getName())) //$NON-NLS-1$
            .findFirst();
        assertTrue(found.isPresent());
        Property property = found.get();
        type = (Type)property.getTypes().get(0);

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected2 = Maps.newHashMap();
        expected2.put("Поставщик", Lists.newArrayList("CatalogRef.Поставщики")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Родитель", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$
        expected2.put("Ссылка", Lists.newArrayList("CatalogRef.Товары")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected2, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionTableRowToStructure() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-table-row-to-structure.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(5, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(4)), Lists.newArrayList(IEObjectTypeNames.STRUCTURE));

        Expression structure = getRightExpr(method.getStatements().get(4));
        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("НомерШага", Lists.newArrayList("Number")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Страница", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionTableToArray() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-table-to-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(4, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(3)), Lists.newArrayList(IEObjectTypeNames.ARRAY));

        Expression array = getRightExpr(method.getStatements().get(3));
        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.ARRAY, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("НомерШага", Lists.newArrayList("Number")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Страница", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(collectionType.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()),
            expected, true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionUnloadColumn() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-unload-column.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(4, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(3)), Lists.newArrayList(IEObjectTypeNames.ARRAY));

        Expression array = getRightExpr(method.getStatements().get(3));
        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.ARRAY, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(IEObjectTypeNames.STRING, McoreUtil.getTypeName(collectionType));

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionObjectPropertiesDetails() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/object-property-details.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(3, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(2)), Lists.newArrayList(IEObjectTypeNames.VALUE_TABLE));

        Expression table = getRightExpr(method.getStatements().get(2));
        Environmental envs = EcoreUtil2.getContainerOfType(table, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(table, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.VALUE_TABLE, McoreUtil.getTypeName(type));
        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(IEObjectTypeNames.VALUE_TABLE_ROW, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Имя", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("Синоним", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$
        expected.put("МногострочныйРежим", Lists.newArrayList("Boolean")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(collectionType.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()),
            expected, true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionFixedData() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-fixed-data.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());

        Expression data = getRightExpr(method.getStatements().get(0));
        Environmental envs = EcoreUtil2.getContainerOfType(data, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(data, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(FIXED_STRUCTURE, McoreUtil.getTypeName(type));

        Map<String, Collection<String>> expected = Map.of("Ключ1", List.of("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionFixedArray() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-fixed-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(3, method.getStatements().size());

        Expression data = getRightExpr(method.getStatements().get(1));
        Environmental envs = EcoreUtil2.getContainerOfType(data, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(data, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(FIXED_ARRAY, McoreUtil.getTypeName(type));

        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(collectionType));

        Map<String, Collection<String>> expected =
            Map.of("Наименование", List.of("String"), "ОбновлениеДоступно", List.of("Boolean")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        checkProperties(collectionType.getContextDef().getProperties().stream().collect(Collectors.toList()), expected,
            true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionFixedMap() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/common-module-fixed-map.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(5, method.getStatements().size());

        Expression data = getRightExpr(method.getStatements().get(3));
        Environmental envs = EcoreUtil2.getContainerOfType(data, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(data, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(FIXED_MAP, McoreUtil.getTypeName(type));

        assertEquals(1, type.getCollectionElementTypes().allTypes().size());
        assertTrue(type.getCollectionElementTypes().allTypes().get(0) instanceof Type);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        assertNotNull(collectionType.getContextDef());

        Map<String, Collection<String>> expected =
            Map.of("Key", List.of("String", IEObjectTypeNames.ARBITRARY), "Value", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                List.of(STRUCTURE, "String", IEObjectTypeNames.ARBITRARY)); //$NON-NLS-1$

        checkProperties(collectionType.getContextDef().getProperties(), expected, false, false);

        Optional<Property> found = collectionType.getContextDef()
            .getProperties()
            .stream()
            .filter(p -> "Value".equals(p.getName())) //$NON-NLS-1$
            .findFirst();
        assertTrue(found.isPresent());

        for (TypeItem typeStructure : found.get().getTypes())
            if (typeStructure instanceof Type && STRUCTURE.equals(McoreUtil.getTypeName(typeStructure)))
                type = (Type)typeStructure;

        assertEquals(STRUCTURE, McoreUtil.getTypeName(type));
        assertNotNull(type.getContextDef());

        Map<String, Collection<String>> expected2 = Map.of("Ключ3", List.of("Number")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties(), expected2, true, true);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionCollapseArray() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/collapse-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(3, method.getStatements().size());
        Expression array = getRightExpr(method.getStatements().get(2));
        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);

        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments())
            .stream()
            .filter(t -> !IEObjectTypeNames.ARBITRARY.equalsIgnoreCase(McoreUtil.getTypeName(t)))
            .collect(Collectors.toList());

        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());

        TypeItem type = types.get(0);

        assertTrue(type instanceof Type);

        assertEquals("CatalogRef.Товары", McoreUtil.getTypeName(type)); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);

    }

    @Test
    public void testFunctionArraysDifference() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/arrays-difference.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(4, method.getStatements().size());
        Expression array = getRightExpr(method.getStatements().get(3));
        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments())
            .stream()
            .filter(t -> !IEObjectTypeNames.ARBITRARY.equalsIgnoreCase(McoreUtil.getTypeName(t)))
            .collect(Collectors.toList());

        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$//$NON-NLS-2$
            1, types.size());

        TypeItem type = types.get(0);

        assertTrue(type instanceof Type);

        assertEquals("CatalogRef.Товары", McoreUtil.getTypeName(type)); //$NON-NLS-1$
        restoreState(oldFileContent, oldFile);

    }

    @Test
    public void testFunctionValueInArray() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/value-in-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        Expression structure = getRightExpr(method.getStatements().get(0));
        checkExpr(structure, Lists.newArrayList(IEObjectTypeNames.STRUCTURE));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals("Current types: " + types.stream().map(McoreUtil::getTypeName).collect(Collectors.joining(", ")), //$NON-NLS-1$ //$NON-NLS-2$
            1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Ключ1", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

        restoreState(oldFileContent, oldFile);
    }

    @Test
    public void testFunctionCheckDocumentsPosting() throws Exception
    {
        readOldContents();

        File newFile = new File(FOLDER_NAME + "common-functions/check-documents-posting.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression array = getRightExpr(method.getStatements().get(1));
        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments());
        assertEquals(2, types.size());
        assertTrue(types.get(0) instanceof Type);
        assertTrue(types.get(1) instanceof Type);

        Type type = (Type)types.get(0);
        if (type.eContainer() == null)
            type = (Type)types.get(1);

        assertEquals("DocumentRef.Документ", McoreUtil.getTypeName(type)); //$NON-NLS-1$

        restoreState(oldFileContent, oldFile);

    }

    protected void readOldContents() throws CoreException, IOException
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8)))
        {
            this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private Expression getRightExpr(Statement statement)
    {
        assertTrue(statement instanceof SimpleStatement);
        return ((SimpleStatement)statement).getRight();
    }

    private void checkExpr(EObject expr, List<String> expectedType)
    {
        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        Set<String> setTypes = new HashSet<>();
        for (TypeItem type : types)
        {
            setTypes.add(McoreUtil.getTypeName(type));
        }
        assertEquals(Sets.newHashSet(expectedType), setTypes);
    }

    private void checkProperties(List<Property> properties, Map<String, Collection<String>> expected,
        boolean isPropertyRu, boolean allowOtherProp)
    {
        assertNotNull(properties);
        assertEquals(expected.isEmpty(), properties.isEmpty());

        Map<String, Collection<String>> toCheck = Maps.newHashMap(expected);

        for (Property property : properties)
        {
            String keyName = isPropertyRu ? property.getNameRu() : property.getName();
            assertNotNull(keyName);
            Collection<String> types = toCheck.remove(keyName);
            assertTrue(MessageFormat.format("Found not expected property {0}", keyName), //$NON-NLS-1$
                types != null || allowOtherProp);
            if (types == null)
                continue;
            Set<String> actualTypes =
                Sets.newHashSet(property.getTypes().stream().map(McoreUtil::getTypeName).collect(Collectors.toList()));
            assertEquals(Sets.newHashSet(types), actualTypes);
        }
        assertEquals("Not all checking properties found", Maps.newHashMap(), toCheck); //$NON-NLS-1$
    }

    private void replaceFileContent(IFile oldFile, File newFile) throws Exception
    {
        try (InputStream stream = new FileInputStream(newFile))
        {
            updateFileContent(oldFile, stream);
        }
    }

    private void restoreState(String oldFileContent, IFile oldFile) throws Exception
    {
        try (InputStream stream = new StringInputStream(oldFileContent, StandardCharsets.UTF_8.name()))
        {
            updateFileContent(oldFile, stream);
        }
    }

    private void updateFileContent(IFile file, InputStream stream) throws Exception
    {
        // Protection from some building inconsistency that occurs if we are placing the change between the build
        // and started process of full BSL module check. In this case the builder cannot compute module dependencies
        // properly for small and fast test configurations
        IWorkspaceOrchestrator orchestrator = ServiceAccess.get(IWorkspaceOrchestrator.class);
        IDtProject dtProject = ServiceAccess.get(IDtProjectManager.class).getDtProject(project);

        Object handle = orchestrator.beginExclusiveOperation("Wait until full check", //$NON-NLS-1$
            Collections.singleton(dtProject), ProjectPipelineJob.AFTER_BUILD_DD);
        orchestrator.endOperation(handle);

        file.setContents(stream, true, false, null);
        Thread.sleep(1500);

        testingWorkspace.waitForBuildCompletion();
    }
}
