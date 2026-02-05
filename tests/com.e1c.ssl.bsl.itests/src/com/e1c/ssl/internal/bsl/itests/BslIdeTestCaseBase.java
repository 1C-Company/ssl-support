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
package com.e1c.ssl.internal.bsl.itests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.junit.Rule;

import com._1c.g5.v8.dt.bsl.model.BslPackage;
import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.resource.BslResolveCrossReferencesJob;
import com._1c.g5.v8.dt.bsl.resource.BslResource;
import com._1c.g5.v8.dt.core.handle.IV8File;
import com._1c.g5.v8.dt.core.handle.impl.V8ModelManager;
import com._1c.g5.v8.dt.core.handle.impl.V8XtextFile;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.testing.TestingWorkspace;
import com._1c.g5.wiring.ServiceAccess;

/**
 * Base class for Bsl tests
 *
 * @author Dzyuba_M
 */
abstract public class BslIdeTestCaseBase
{

    @Rule
    public TestingWorkspace testingWorkspace;

    protected DerivedStateAwareResource res;

    protected BslIdeTestCaseBase()
    {
        super();
        this.testingWorkspace = new TestingWorkspace();
    }

    protected BslIdeTestCaseBase(boolean autoBuild, boolean cleanUp)
    {
        super();
        this.testingWorkspace = new TestingWorkspace(autoBuild, cleanUp);
    }

    /**
     * Method calls {@link org.eclipse.xtext.EcoreUtil2#resolveLazyCrossReferences(Resource, CancelIndicator)}
     * and wait while {@link BslResolveCrossReferencesJob} finish
     * @param resource {@link LazyLinkingResource} for resolve cross references, cannot be <code>null</code>
     * @throws InterruptedException if {@link BslResolveCrossReferencesJob} was interrupted
     */
    public static void customResolveLazyCrossReferences(LazyLinkingResource resource) throws InterruptedException
    {
        EcoreUtil2.resolveLazyCrossReferences(resource, null);
        Collection<BslResolveCrossReferencesJob> jobs = BslResolveCrossReferencesJob.findJobsByResource(resource);
        for (BslResolveCrossReferencesJob job : jobs)
        {
            job.join();
        }
    }

    /**
     * Initializes inner attributes defined in RuntimeModule in test class
     * @param rsp actual {@link IResourceServiceProvider} by project, can't be <code>null</code>
     */
    abstract protected void initilizeSpecialServicesByRSP(IResourceServiceProvider rsp);

    /**
     * Gets actual {@link DerivedStateAwareResource} by {@link V8XtextFile}
     * @param xtextFile actual {@link V8XtextFile}, can't be <code> null</code>
     * @return  actual {@link DerivedStateAwareResource} by {@link V8XtextFile}, never <code>null</code>
     */
    protected DerivedStateAwareResource getResourceFromFile(V8XtextFile xtextFile)
    {
        XtextResource res = xtextFile.readOnly(new IUnitOfWork<XtextResource, XtextResource>()
        {
            @Override
            public XtextResource exec(XtextResource state) throws Exception
            {
                return state;
            }
        });
        assertTrue(res instanceof DerivedStateAwareResource);
        return (DerivedStateAwareResource)res;
    }

    /**
     * Gets actual {@link DerivedStateAwareResource} by file with name <code>fileName</code> from project with name </code>projectNamem</code>
     * @param projectName actual project name, can't be <code> null</code>
     * @param fileName actual file name, can't be <code> null</code>
     * @return  actual {@link DerivedStateAwareResource} by file with name <code>fileName</code> from project with name </code>projectNamem</code>
     */
    protected DerivedStateAwareResource getResourceFromProject(String projectName, String fileName) throws Exception
    {
        testingWorkspace.setUpProject(projectName, getClass());
        testingWorkspace.waitForBuildCompletion();
        BslPackage.eINSTANCE.eClass(); //force BSL environment to load

        IV8File file = V8ModelManager.INSTANCE.getV8Model().getV8Project(projectName).getV8File(fileName);
        assertTrue(file instanceof V8XtextFile);
        V8XtextFile xtextFile = (V8XtextFile)file;
        return getResourceFromFile(xtextFile);
    }

    /**
     * Build project with name <code>projectName</code>
     * @param projectName actual name of the project, can't be <code>null</code>
     * @throws Exception
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    protected void initProject(String projectName) throws Exception, CoreException, IOException, InterruptedException
    {
        testingWorkspace.cleanUpWorkspace();
        IProject project = testingWorkspace.setUpProject(projectName, getClass());
        ServiceAccess.get(IBmModelManager.class).waitModelSynchronization(project);
        BslPackage.eINSTANCE.eClass(); //force BSL environment to load
        testingWorkspace.waitForBuildCompletion();
    }

    /**
     * For initialization test with actual Bsl module
     * @param projectName projectName actual project name, can't be <code> null</code>
     * @param fileName actual file name, can't be <code> null</code>
     * @return actual Bsl module for initialization, never <code>null</code>
     * @throws Exception
     */
    protected Module init(String projectName, String fileName) throws Exception
    {
        res = getResourceFromProject(projectName, fileName);
        assertTrue(res instanceof BslResource);
        ((BslResource)res).setDeepAnalysis(true);

        IResourceServiceProvider provider = res.getResourceServiceProvider();
        initilizeSpecialServicesByRSP(provider);

        EObject eObject = res.getContents().get(0);
        assertTrue(eObject instanceof Module);
        customResolveLazyCrossReferences(res);
        return (Module)eObject;
    }

    /**
     * For initialization test with actual Bsl module with name "CommonModule.CommonModule.Module.bsl"
     * @param projectName projectName actual project name, can't be <code> null</code>
     * @return actual Bsl module for initialization, never <code>null</code>
     * @throws Exception
     */
    protected Module init(String projectName) throws Exception
    {
        return init(projectName, "/src/CommonModules/CommonModule/Module.bsl"); //$NON-NLS-1$
    }

    /**
     * Gets Bsl module for file withName <code>fileName</code> in initialized project with name <code>projectName</code>.
     *
     * @param projectName project name, can't be <code>null</code>
     * @param fileName file name of bsl module, can't be <code>null</code>
     * @return Bsl module, never <code>null<code>
     * @throws Exception the exception
     */
    protected Module getBslModule(String projectName, String fileName) throws Exception
    {
        IV8File file = V8ModelManager.INSTANCE.getV8Model().getV8Project(projectName).getV8File(fileName);
        assertTrue(file instanceof V8XtextFile);
        V8XtextFile xtextFile = (V8XtextFile)file;
        DerivedStateAwareResource resource = getResourceFromFile(xtextFile);
        assertTrue(resource instanceof BslResource);

        ((BslResource)resource).setDeepAnalysis(true);

        IResourceServiceProvider provider = resource.getResourceServiceProvider();
        initilizeSpecialServicesByRSP(provider);

        EObject eObject = resource.getContents().get(0);
        assertTrue(eObject instanceof Module);
        customResolveLazyCrossReferences(resource);
        return (Module)eObject;
    }

}
