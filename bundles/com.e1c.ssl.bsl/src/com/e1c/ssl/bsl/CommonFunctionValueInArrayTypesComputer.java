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
 *******************************************************************************/
package com.e1c.ssl.bsl;

import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.EcoreUtil2;

import com._1c.g5.v8.dt.bsl.model.Expression;
import com._1c.g5.v8.dt.bsl.model.ExtendedCollectionType;
import com._1c.g5.v8.dt.bsl.model.Invocation;
import com._1c.g5.v8.dt.bsl.typesystem.util.TypeSystemUtil;
import com._1c.g5.v8.dt.mcore.Environmental;
import com._1c.g5.v8.dt.mcore.McorePackage;
import com._1c.g5.v8.dt.mcore.TypeItem;
import com._1c.g5.v8.dt.platform.IEObjectProvider;
import com._1c.g5.v8.dt.platform.version.Version;
import com.google.common.collect.Lists;

/**
 * Extension computer of invocation types of 1C:SSL API module function {@code CommonClientServer.ValueInArray()} that
 * returns the typed array.
 *
 * @author Artem Iliukhin
 *
 */
public class CommonFunctionValueInArrayTypesComputer
    extends AbstractCommonModuleObjectAttributeValueTypesComputer
{

    @Override
    public List<TypeItem> getTypes(Invocation inv)
    {
        if (inv.getParams().size() != 1)
            return Collections.emptyList();

        if (!isValidClientServerModuleNameInvocation(inv))
            return Collections.emptyList();

        Expression expr = inv.getParams().get(0);
        if (expr == null)
            return Collections.emptyList();

        Environmental envs = EcoreUtil2.getContainerOfType(expr, Environmental.class);

        List<TypeItem> type = this.getTypesComputer().computeTypes(expr, envs.environments());

        List<TypeItem> collectionTypes = Lists.newArrayList();
        collectionTypes.add(type.get(0));

        IEObjectProvider provider = IEObjectProvider.Registry.INSTANCE.get(McorePackage.Literals.TYPE_ITEM,
            versionSupport.getRuntimeVersionOrDefault(inv, Version.LATEST));

        ExtendedCollectionType extendedType = TypeSystemUtil.createExtendedArrayType(collectionTypes, provider, inv);

        return Lists.newArrayList(extendedType);

    }

}
