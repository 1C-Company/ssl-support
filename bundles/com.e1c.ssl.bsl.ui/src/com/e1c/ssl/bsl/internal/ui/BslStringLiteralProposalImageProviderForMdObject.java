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
package com.e1c.ssl.bsl.internal.ui;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.swt.graphics.Image;

import com._1c.g5.v8.dt.bsl.ui.contentassist.stringliteral.IStringLiteralProposalProvider.IBslStringLiteralProposalImageProvider;
import com._1c.g5.v8.dt.md.ui.shared.MdUiSharedImages;

/**
 * Image provider for bsl string literal proposal based {@link EClass} of the MD object
 *
 * @author Dmitriy Marmyshev
 *
 */
public class BslStringLiteralProposalImageProviderForMdObject
    implements IBslStringLiteralProposalImageProvider
{
    private final EClass eClass;

    public BslStringLiteralProposalImageProviderForMdObject(EClass eClass)
    {
        this.eClass = eClass;
    }

    @Override
    public Image getImage()
    {
        return MdUiSharedImages.getMdClassImage(this.eClass);
    }
}
