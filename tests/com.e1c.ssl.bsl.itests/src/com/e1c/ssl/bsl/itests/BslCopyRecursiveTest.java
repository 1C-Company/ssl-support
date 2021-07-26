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
package com.e1c.ssl.bsl.itests;

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
import org.junit.After;
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
 * Tests for functions CopyRecursive of 1C:SSL API modules "Common".
 *
 * @author Popov Vitalii
 *
 */
public class BslCopyRecursiveTest
    extends BslIdeTestCaseBase
{

    private static final String PATH_COMMON_MODULE_TEST = "/src/CommonModules/ТестовыйМодуль/Module.bsl"; //$NON-NLS-1$

    private static final String PROJECT_NAME = "CommonFunctions"; //$NON-NLS-1$

    private static final String FOLDER_NAME = "./resources/"; //$NON-NLS-1$

    private IProject project;

    private TypesComputer typesComputer;

    private String oldFileContent;

    private IFile oldFile;

    public BslCopyRecursiveTest()
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

    /**
     * Check that {@code Common.CopyRecursive(Value)} return correct type.
     */
    @Test
    public void testFunctionCopyRecursiveWithDefaultParam() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(0);
        Expression structure = getRightExpr(method.getStatements().get(1));
        checkExpr(structure, Lists.newArrayList(IEObjectTypeNames.STRUCTURE));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Ключ1", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

    }

    /**
     * Check that {@code Common.CopyRecursive(Value, False)} return correct type.
     */
    @Test
    public void testFunctionCopyRecursiveWithDeafuleValueParam() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(1);
        Expression structure = getRightExpr(method.getStatements().get(1));
        checkExpr(structure, Lists.newArrayList(IEObjectTypeNames.STRUCTURE));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.STRUCTURE, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Ключ1", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

        restoreState(oldFileContent, oldFile);
    }

    /**
     * Check that {@code Common.CopyRecursive(Value, true)} return correct type for Structure.
     */
    @Test
    public void testFunctionCopyRecursiveReturnFixStructure() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(2);
        Expression structure = getRightExpr(method.getStatements().get(1));
        checkExpr(structure, Lists.newArrayList(IEObjectTypeNames.FIXED_STRUCTURE));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.FIXED_STRUCTURE, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Ключ1", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

    }

    /**
     * Check that {@code Common.CopyRecursive(Value, true)} return correct type for Map.
     */
    @Test
    public void testFunctionCopyRecursiveReturnFixMap() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(3);
        Expression map = getRightExpr(method.getStatements().get(2));
        checkExpr(map, Lists.newArrayList(IEObjectTypeNames.FIXED_MAP));

        Environmental envs = EcoreUtil2.getContainerOfType(map, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(map, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);

        Type type = (Type)types.get(0);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);

        assertEquals(IEObjectTypeNames.KEY_AND_VALUE, McoreUtil.getTypeName(collectionType));

        Map<String, Collection<String>> expected = Maps.newHashMap();
        // Arbitary added from source collection
        expected.put("Ключ", Lists.newArrayList(IEObjectTypeNames.NUMBER, IEObjectTypeNames.ARBITRARY)); //$NON-NLS-1$
        expected.put("Значение", Lists.newArrayList(IEObjectTypeNames.STRUCTURE, IEObjectTypeNames.ARBITRARY)); //$NON-NLS-1$

        checkProperties(collectionType.getContextDef().getProperties(), expected,
            true, false);

    }

    /**
     * Check that {@code Common.CopyRecursive(Value, true)} return correct type for Array.
     */
    @Test
    public void testFunctionCopyRecursiveReturnFixArray() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(4);
        Expression array = getRightExpr(method.getStatements().get(1));
        checkExpr(array, Lists.newArrayList(IEObjectTypeNames.FIXED_ARRAY));

        Environmental envs = EcoreUtil2.getContainerOfType(array, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(array, envs.environments());
        assertEquals(2, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);
        Type collectionType = (Type)type.getCollectionElementTypes().allTypes().get(0);
        assertEquals(IEObjectTypeNames.STRUCTURE,
            McoreUtil.getTypeName(collectionType));
    }

    /**
     * Check that {@code Common.CopyRecursive(Value, true)} return correct type for ValueList.
     */
    @Test
    public void testFunctionCopyRecursiveReturnValueList() throws Exception
    {
        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(5);
        Expression structure = getRightExpr(method.getStatements().get(2));
        checkExpr(structure, Lists.newArrayList(IEObjectTypeNames.VALUE_LIST));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(IEObjectTypeNames.VALUE_LIST, McoreUtil.getTypeName(type));

    }

    /**
     * Check that {@code Common.CopyRecursive(Value, false)} return correct type for Fixed Structure.
     */
    @Test
    public void testFunctionCopyRecursiveReturnStructure() throws Exception
    {
        String resultType = IEObjectTypeNames.STRUCTURE;

        this.oldFile = project.getFile(Path.fromPortableString(PATH_COMMON_MODULE_TEST));
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(oldFile.getContents(true), StandardCharsets.UTF_8));
        this.oldFileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        File newFile = new File(FOLDER_NAME + "common-functions/copy-recursive.bsl"); //$NON-NLS-1$
        replaceFileContent(oldFile, newFile);

        Module module = getBslModule(PROJECT_NAME, PATH_COMMON_MODULE_TEST);
        Method method = module.allMethods().get(6);
        Expression structure = getRightExpr(method.getStatements().get(1));

        checkExpr(structure, Lists.newArrayList(resultType));

        Environmental envs = EcoreUtil2.getContainerOfType(structure, Environmental.class);
        List<TypeItem> types = typesComputer.computeTypes(structure, envs.environments());
        assertEquals(1, types.size());
        assertTrue(types.get(0) instanceof Type);
        Type type = (Type)types.get(0);

        assertEquals(resultType, McoreUtil.getTypeName(type));
        Map<String, Collection<String>> expected = Maps.newHashMap();
        expected.put("Ключ1", Lists.newArrayList("String")); //$NON-NLS-1$ //$NON-NLS-2$

        checkProperties(type.getContextDef().getProperties().stream().skip(1).collect(Collectors.toList()), expected,
            true, false);

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
