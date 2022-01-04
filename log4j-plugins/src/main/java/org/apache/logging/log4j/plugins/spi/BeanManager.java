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

package org.apache.logging.log4j.plugins.spi;

import org.apache.logging.log4j.plugins.di.Inject;
import org.apache.logging.log4j.plugins.di.Producer;
import org.apache.logging.log4j.plugins.di.Qualifier;
import org.apache.logging.log4j.plugins.name.AnnotatedElementNameProvider;
import org.apache.logging.log4j.plugins.util.AnnotationUtil;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Central SPI for injecting and managing beans and their instances.
 */
// TODO: move this whole API to log4j-plugins
public interface BeanManager extends AutoCloseable {

    /**
     * Loads beans from the given classes. This looks for injectable classes and producers in the provided classes,
     * loads them into this manager, and returns the loaded beans.
     *
     * @param beanClasses classes to load beans from
     * @return beans loaded from the given classes
     */
    Collection<Bean<?>> loadBeans(final Collection<Class<?>> beanClasses);

    /**
     * Creates a bean for an injectable bean class.
     */
    <T> Bean<T> createBean(final Class<T> beanClass);

    /**
     * Creates a bean for a producer field.
     *
     * @param producerField field to obtain bean instance from
     * @param owner         owning bean for instance field or null for a static field
     * @return newly created bean
     */
    Bean<?> createBean(final Field producerField, final Bean<?> owner);

    /**
     * Creates a bean for a producer method.
     *
     * @param producerMethod method to obtain bean instance from
     * @param owner          owning bean for instance method or null for a static method
     * @return newly created bean
     */
    Bean<?> createBean(final Method producerMethod, final Bean<?> owner);

    /**
     * Loads beans from the given classes. This looks for injectable classes and producers, registers them in this
     * manager, validates them, then returns the validated beans.
     *
     * @param beanClasses classes to load beans from
     * @throws ValidationException if any beans have validation errors
     */
    default void loadAndValidateBeans(final Class<?>... beanClasses) {
        final Collection<Bean<?>> beans = loadBeans(List.of(beanClasses));
        validateBeans(beans);
    }

    /**
     * Validates beans and throws a {@link ValidationException} if there are any errors.
     *
     * @param beans beans to check for validation errors
     * @throws ValidationException if any beans have validation errors
     */
    void validateBeans(final Iterable<Bean<?>> beans);

    /**
     * Validates the given injection point.
     *
     * @param point injection point to validate
     * @throws DefinitionException      if the injection point is improperly defined
     * @throws UnsatisfiedBeanException if no beans can satisfy the injection point
     */
    void validateInjectionPoint(InjectionPoint point);

    /**
     * Checks if a class has exactly one injectable constructor. A constructor is <i>injectable</i> if:
     * <ol>
     *     <li>it is annotated with {@link Inject}; or</li>
     *     <li>it has as least one parameter annotated with a {@linkplain Qualifier qualifier annotation}; or</li>
     *     <li>it is the lone no-arg constructor.</li>
     * </ol>
     *
     * @param type class to find an injectable constructor in
     * @return true if the class has exactly one injectable constructor or false otherwise
     */
    default boolean isInjectable(final Class<?> type) {
        boolean result = false;
        int injectConstructors = 0;
        final Constructor<?>[] constructors = type.getDeclaredConstructors();
        for (final Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                injectConstructors++;
            }
        }
        if (injectConstructors <= 1) {
            if (injectConstructors == 1) {
                result = true;
            } else {
                int implicitConstructors = 0;
                for (final Constructor<?> constructor : constructors) {
                    for (final Parameter parameter : constructor.getParameters()) {
                        if (AnnotatedElementNameProvider.hasName(parameter)) {
                            implicitConstructors++;
                            break;
                        }
                    }
                }
                if (implicitConstructors <= 1) {
                    if (implicitConstructors == 1) {
                        result = true;
                    } else {
                        try {
                            type.getDeclaredConstructor();
                            result = true;
                        } catch (final NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Checks if an element is injectable. An element is <i>injectable</i> if:
     * <ol>
     *     <li>it is annotated with {@link Inject}; or</li>
     *     <li>it is annotated with a {@linkplain Qualifier qualifier annotation}
     *     and is not annotated with a {@link Producer} annotation.</li>
     * </ol>
     *
     * @param element field, method, or parameter to check
     * @return true if the element is injectable or false otherwise
     */
    default boolean isInjectable(final AnnotatedElement element) {
        if (element.isAnnotationPresent(Inject.class)) {
            return true;
        }
        if (AnnotationUtil.isMetaAnnotationPresent(element, Producer.class)) {
            return false;
        }
        return AnnotatedElementNameProvider.hasName(element);
    }

    default <T> Optional<Bean<T>> getDefaultBean(final Class<T> beanType) {
        return getNamedBean(beanType, Strings.EMPTY);
    }

    default <T> Optional<Bean<T>> getNamedBean(final Class<T> beanType, final String name) {
        return getBean(beanType, name, List.of());
    }

    <T> Optional<Bean<T>> getBean(Type type, final String name, Collection<String> aliases);

    /**
     * Creates an injection point for a field with an optional owning bean.
     *
     * @param field field where injection will take place
     * @param owner bean where field is located or null for static fields
     * @return an injection point describing the field
     */
    InjectionPoint createFieldInjectionPoint(final Field field, final Bean<?> owner);

    /**
     * Creates an injection point for a method or constructor parameter with an optional owning bean.
     *
     * @param executable method or constructor where injection will take place
     * @param parameter  which parameter of that executable to create a point at
     * @param owner      bean where executable is located or null for static methods
     * @return an injection point describing the parameter
     */
    InjectionPoint createParameterInjectionPoint(final Executable executable, final Parameter parameter, final Bean<?> owner);

    /**
     * Creates a collection of injection points for all the parameters of a method or constructor with an optional
     * owning bean.
     *
     * @param executable method or constructor where injection will take place
     * @param owner      bean where executable is located or null for static methods
     * @return collection of injection points describing the executable parameters
     */
    default Collection<InjectionPoint> createExecutableInjectionPoints(final Executable executable, final Bean<?> owner) {
        final Parameter[] parameters = executable.getParameters();
        final Collection<InjectionPoint> points = new ArrayList<>(parameters.length);
        for (final Parameter parameter : parameters) {
            points.add(createParameterInjectionPoint(executable, parameter, owner));
        }
        return points;
    }

    /**
     * Creates an InitializationContext for a given Bean instance for use in dependency injection SPIs.
     *
     * @param bean bean to create an initialization context for (may be null to bootstrap a dependency graph)
     * @param <T>  type of object created by bean
     * @return new InitializationContext for the given Bean
     */
    <T> InitializationContext<T> createInitializationContext(final Bean<T> bean);

    /**
     * Gets or creates the value for a given bean inside a given InitializationContext.
     *
     * @param bean          bean to get or create value for
     * @param parentContext which context this bean is being used in
     * @param <T>           type of value
     * @return value of the bean in the given context
     */
    <T> T getValue(final Bean<T> bean, final InitializationContext<?> parentContext);

    /**
     * Gets the value to use for injecting into a given InjectionPoint in a given InitializationContext.
     *
     * @param point         location where injectable value would be injected
     * @param parentContext which context this value is being injected under
     * @param <T>           type of injectable value
     * @return value to inject if defined or empty otherwise
     */
    <T> Optional<T> getInjectableValue(final InjectionPoint point, final InitializationContext<?> parentContext);

    /**
     * Destroys all the beans managed by this instance.
     */
    @Override
    void close();

    // TODO: integrate with constraint validators
    // TODO: integrate with TypeConverters
    // TODO: need some sort of default value strategy to bridge over @PluginAttribute and optional injected values
    // TODO: add support for injecting collections and arrays
    // TODO: begin integrating with singleton beans in log4j-core
    // TODO: LoggerContext scope (sort of like a singleton/application scope with each LoggerContext)
    // TODO: configuration scope? should be similar to LoggerContext scope but can be restarted/reconfigured at runtime
    // TODO: update annotation processor to output bean descriptors for lazy loading (attempt to provide partial type closures?)
}
