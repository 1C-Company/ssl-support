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

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.mcore.TypeItem;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code Common.ObjectsAttributeValue()} that
 * returns actual types for the ref type of link and attribute names.
 *
 * @author Dmitriy Marmyshev
 *
 */
public class CommonFunctionObjectsAttributesValuesTypesComputer
    extends CommonFunctionObjectAttributesValuesTypesComputer
{

    @Override
    protected List<TypeItem> computeTypes(Invocation inv, List<TypeItem> refTypes, String paramContent)
    {
        List<TypeItem> types = super.computeTypes(inv, refTypes, paramContent);
        if (!types.isEmpty())
        {
            return createCustomMapType(refTypes, types, inv);

        }
        return Collections.emptyList();
    }

    @Override
    protected List<TypeItem> getReturnRefTypes(Expression expr)
    {
        return getReturnArrayRefTypes(expr);
    }
}
