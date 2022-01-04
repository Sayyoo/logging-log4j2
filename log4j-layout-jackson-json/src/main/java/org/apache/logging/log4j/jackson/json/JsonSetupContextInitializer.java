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
package org.apache.logging.log4j.jackson.json;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.jackson.ExtendedStackTraceElementMixIn;
import org.apache.logging.log4j.jackson.InstantMixIn;
import org.apache.logging.log4j.jackson.LevelMixIn;
import org.apache.logging.log4j.jackson.LogEventJsonMixIn;
import org.apache.logging.log4j.jackson.MarkerMixIn;
import org.apache.logging.log4j.jackson.StackTraceElementMixIn;
import org.apache.logging.log4j.jackson.ThrowableProxyMixIn;
import org.apache.logging.log4j.jackson.ThrowableProxyWithStacktraceAsStringMixIn;
import org.apache.logging.log4j.jackson.ThrowableProxyWithoutStacktraceMixIn;

import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Used to set up {@link SetupContext} from different {@link SimpleModule}s.
 * <p>
 * Differs from SetupContextInitializer by installing {@code LogEventJsonMixIn} for LogEvents, not
 * {@code LogEventMixIn}, so it handles {@link ThreadContext} serialization differently.
 * </p>
 * <p>
 * <em>Consider this class private.</em>
 * </p>
 */
public class JsonSetupContextInitializer {

    public void setupModule(final SetupContext context, final boolean includeStacktrace,
            final boolean stacktraceAsString) {
        // JRE classes: we cannot edit those with Jackson annotations
        context.setMixInAnnotations(StackTraceElement.class, StackTraceElementMixIn.class);
        // Log4j API classes: we do not want to edit those with Jackson annotations because the API module should not
        // depend on Jackson.
        context.setMixInAnnotations(Marker.class, MarkerMixIn.class);
        context.setMixInAnnotations(Level.class, LevelMixIn.class);
        context.setMixInAnnotations(Instant.class, InstantMixIn.class);
        context.setMixInAnnotations(LogEvent.class, LogEventJsonMixIn.class); // different ThreadContext handling
        // Log4j Core classes: we do not want to bring in Jackson at runtime if we do not have to.
        context.setMixInAnnotations(ExtendedStackTraceElement.class, ExtendedStackTraceElementMixIn.class);
        context.setMixInAnnotations(ThrowableProxy.class, includeStacktrace
                ? (stacktraceAsString ? ThrowableProxyWithStacktraceAsStringMixIn.class : ThrowableProxyMixIn.class)
                : ThrowableProxyWithoutStacktraceMixIn.class);
    }
}
