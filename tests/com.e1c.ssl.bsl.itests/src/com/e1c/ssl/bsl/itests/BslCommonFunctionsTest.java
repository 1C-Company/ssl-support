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

import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.KEY_AND_VALUE;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.MAP;
import static com._1c.g5.v8.dt.platform.IEObjectTypeNames.STRUCTURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.Before;
import org.junit.Test;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Method;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.model.SimpleStatement;
import com._1c.g5.v8.dt.bsl.model.Statement;
import com._1c.g5.v8.dt.bsl.resource.TypesComputer;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.Property;
import com._1c.g5.v8.dt.mcore.Type;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.mcore.util.McoreUtil;
import com._1c.g5.v8.dt.platform.IEObjectTypeNames;
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

    @Override
    protected void initilizeSpecialServicesByRSP(IResourceServiceProvider rsp)
    {
        typesComputer = rsp.get(TypesComputer.class);
        assertNotNull(typesComputer);
    }

    @Test
    public void testFunctionCommonModule() throws Exception
    {

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/common-module.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-ref.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-ref-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-full-name.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/manager-by-full-name-ref.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/common-module-client.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/common-module-object-manager.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attribute-value.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attribute-value-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/objects-attribute-value.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());

        Expression right = getRightExpr(method.getStatements().get(0));
        Environmental envs = EcoreUtil2.getContainerOfType(right, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(right, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/objects-attribute-value-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());

        Expression right = getRightExpr(method.getStatements().get(1));
        Environmental envs = EcoreUtil2.getContainerOfType(right, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(right, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(0));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array-ctor-subproperty.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array-ctor.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-array.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(6, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(5));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-compute-strings.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(5, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(4));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-string-var.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure-ctor.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(3, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(2));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure-subproperty.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(7, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(6));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/object-attributes-values-structure.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(6, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(5));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(1, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(0));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        Optional<Property> found = collectionType.getContextDef().getProperties().stream().filter(
            p -> "Value".equals(p.getName())).findFirst(); //$NON-NLS-1$
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
    public void testFunctionObjectsAttributesValuesDynamic() throws Exception
    {

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values-dynamic.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(2, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        Optional<Property> found = collectionType.getContextDef().getProperties().stream().filter(
            p -> "Value".equals(p.getName())).findFirst(); //$NON-NLS-1$
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

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/objects-attributes-values-string-var.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(2, method.getStatements().size());
        Expression expr = getRightExpr(method.getStatements().get(1));

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(expr, envs.environments());
        assertEquals(1, types.size());
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

        Optional<Property> found = collectionType.getContextDef().getProperties().stream().filter(
            p -> "Value".equals(p.getName())).findFirst(); //$NON-NLS-1$
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
    public void testFunctionTableToArray() throws Exception
    {

        IFile oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        String oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/common-module-table-row-to-structure.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);
        testingWorkspace.buildWorkspace();

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        assertEquals(1, module.allMethods().size());
        Method method = module.allMethods().get(0);
        assertEquals(5, method.getStatements().size());
        checkExpr(getRightExpr(method.getStatements().get(4)), Lists.newArrayList(IEObjectTypeNames.STRUCTURE));

        Expression structure = getRightExpr(method.getStatements().get(4));
        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
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
        InputStream stream = new FileInputStream(newFile);
        updateFileContent(oldFile, stream);
    }

    private void restoreState(String oldFileContent, IFile oldFile) throws Exception
    {
        InputStream stream = new StringInputStream(oldFileContent);
        updateFileContent(oldFile, stream);
    }

    private void updateFileContent(IFile file, InputStream stream) throws Exception
    {
        boolean[] wasChanged = new boolean[1];
        project.getWorkspace().addResourceChangeListener(new IResourceChangeListener()
        {
            @Override
            public void resourceChanged(IResourceChangeEvent event)
            {
                if (event.getResource() == file)
                {
                    wasChanged[0] = true;
                    project.getWorkspace().removeResourceChangeListener(this);
                }
            }
        }, IResourceChangeEvent.PRE_REFRESH);
        file.setContents(stream, true, false, null);
        int i = 0;
        while (!wasChanged[0] && i < 4)
        {
            ++i;
            Thread.sleep(500);
        }
        testingWorkspace.buildWorkspace();
    }

}
