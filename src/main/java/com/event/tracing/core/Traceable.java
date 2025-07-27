package com.event.tracing.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.opentelemetry.api.trace.SpanKind;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traceable {
	String value() default "";

	SpanKind spanKind() default SpanKind.INTERNAL;
}