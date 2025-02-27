/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.plugins;

import org.apache.logging.log4j.plugins.di.Qualifier;
import org.apache.logging.log4j.plugins.name.AliasesProvider;
import org.apache.logging.log4j.plugins.name.PluginAliasesProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a list of aliases for an annotated plugin element. This is supported by plugin classes and other element
 * types supported by the annotations in this package.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@AliasesProvider(PluginAliasesProvider.class)
@Qualifier
public @interface PluginAliases {

    /**
     * Aliases the annotated element can also be referred to. These aliases are case-insensitive.
     * @return the aliases associated with the plugin.
     */
    String[] value();
}
