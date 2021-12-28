#!/bin/bash
#*******************************************************************************
# Copyright (C) 2020 1C-Soft LLC and others.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     1C-Soft LLC - initial API and implementation
#*******************************************************************************

current_version='0.5.0'
new_version='0.6.0'
next_version='0.7.0'

#sed "s/\<version\>0.1.0-SNAPSHOT\<\/version\>/\<version\>0.1.1-SNAPSHOT\<\/version\>/g" pom.xml
find . -name 'pom.xml' -exec sed -i '' "s/${current_version}-SNAPSHOT/${new_version}-SNAPSHOT/g" {} +
find . -name 'pom-rcptt.xml' -exec sed -i '' "s/\<version\>${current_version}-SNAPSHOT\<\/version\>/\<version\>${new_version}-SNAPSHOT\<\/version\>/g" {} +

#sed "s/Bundle-Version: 0.2.0.qualifier/Bundle-Version: 0.3.0.qualifier/g" ./com.e1c.ssl/META-INF/MANIFEST.MF
find . -name 'MANIFEST.MF' -exec sed -i '' "s/Bundle-Version: ${current_version}.qualifier/Bundle-Version: ${new_version}.qualifier/g" {} +

#sed "s/\(com\._1c\.ssl[\.a-z0-9].*;version=\"\)\([\.0-9]*\)\"/\10.3.0\"/g" ./com.e1c.ssl.bsl/META-INF/MANIFEST.MF
find . -name 'MANIFEST.MF' -exec sed -i '' "s/\(com\.e1c\.ssl[\.a-z0-9]*;version=\"\)\([\.0-9]*\)\"/\1${new_version}\"/g" {} +

#sed "s/\(com\._1c\.ssl[\.a-z.]*;version=\"\[\)\([\.,0-9]*)\"\)/\1zzzzz0.3.0zzzzz,yyyy0.4.0yyyy)\"/g" ./com.e1c.ssl.bsl/META-INF/MANIFEST.MF
find . -name 'MANIFEST.MF' -exec sed -i '' "s/\(com\.e1c\.ssl[\.a-z0-9]*;version=\"\[\)\([\.,0-9]*)\"\)/\1${new_version},${next_version})\"/g" {} +
find . -name 'MANIFEST.MF' -exec sed -i '' "s/\(com\.e1c\.ssl[\.a-z0-9]*;bundle-version=\"\[\)\([\.,0-9]*)\"\)/\1${new_version},${next_version})\"/g" {} +

#sed "s/0.2.0.qualifier/zzzz0.3.0.qualifierzzzz/g" ./repository/category.xml
find . -name 'category.xml' -exec sed -i '' "s/${current_version}.qualifier/${new_version}.qualifier/g" {} +

#sed "s/version=\"0.2.0.qualifier\"/version=\"0.3.0.qualifier\"/g" ./com.e1c.ssl.v8.dt.feature/feature.xml
find . -name 'feature.xml' -exec sed -i '' "s/version=\"${current_version}.qualifier\"/version=\"${new_version}.qualifier\"/g" {} +
