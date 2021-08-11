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

import java.util.Collections;
import java.util.List;

import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com.google.inject.Inject;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectsAttributeValue()} that
 * returns actual types for the ref type of link and attribute names.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectsAttributeValueTypesComputer
    extends CommonFunctionObjectAttributeValueTypesComputer
{

    private final TypesComputerHelper typesComputerHelper;

    @Inject
    public CommonFunctionObjectsAttributeValueTypesComputer(TypesComputerHelper typesComputerHelper)
    {
        this.typesComputerHelper = typesComputerHelper;
    }

    @Override
    protected List<TypeItem> computeTypes(Invocation inv, String paramContent)
    {

        List<TypeItem> refTypes = getReturnArrayRefTypes(inv.getParams().get(0));

        if (refTypes.isEmpty())
            return Collections.emptyList();

        String content = paramContent.trim().replace(System.lineSeparator(), ""); //$NON-NLS-1$
        if (content.isEmpty())
            return Collections.emptyList();

        String[] properties = content.split("\\.", -1); //$NON-NLS-1$

        List<TypeItem> types = getTypeByPropertyName(refTypes, properties, inv);
        if (!types.isEmpty())
        {
            return typesComputerHelper.createCustomMapType(refTypes, types, inv);

        }
        return Collections.emptyList();
    }
}
