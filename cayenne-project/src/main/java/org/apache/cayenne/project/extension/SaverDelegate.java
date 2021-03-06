/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.project.extension;

import org.apache.cayenne.configuration.ConfigurationNodeVisitor;
import org.apache.cayenne.util.XMLEncoder;

/**
 * Delegate that handles saving XML of extension.
 * {@link BaseSaverDelegate} should be used as a base class for custom delegates.
 *
 * @since 4.1
 */
public interface SaverDelegate extends ConfigurationNodeVisitor<Void> {

    /**
     * @param encoder provided by caller
     */
    void setXMLEncoder(XMLEncoder encoder);

    /**
     * @param parentDelegate parent delegate, provided by caller
     */
    void setParentDelegate(SaverDelegate parentDelegate);

    SaverDelegate getParentDelegate();

}
