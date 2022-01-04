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

import org.apache.logging.log4j.plugins.name.NameProvider;
import org.apache.logging.log4j.plugins.name.PluginNameProvider;
import org.apache.logging.log4j.util.Strings;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that identifies a Class as a Plugin.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@NameProvider(PluginNameProvider.class)
public @interface Plugin {

    /**
     * Value of the elementType when none is specified.
     */
    String EMPTY = Strings.EMPTY;

    /**
     * Name of the plugin. Note that this name is case-insensitive.
     * @return the name of the plugin.
     */
    String name();

    /**
     * Category to place the plugin under. Category names are case-sensitive.
     * @return the category
     */
    String category();

    /**
     * Name of the corresponding category of elements this plugin belongs under. For example, {@code appender} would
     * indicate an Appender plugin which would be in the
     * {@code <Appenders/>} element of a Configuration.
     * @return the element's type.
     */
    String elementType() default EMPTY;

    /**
     * Indicates if the plugin class implements a useful {@link Object#toString()} method for use in log messages.
     * @return true if the object should print nicely.
     */
    boolean printObject() default false;

    /**
     * Indicates if construction and injection of child configuration nodes should be deferred until first use.
     * @return true if child elements should defer instantiation until they are accessed.
     */
    boolean deferChildren() default false;
}
