package com.event.tracing.core;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Aspect
@Component
public class TracingAspect {

	private final Tracer tracer;

	public TracingAspect(Tracer tracer) {
		this.tracer = tracer;
	}

	@Around("@annotation(traceable)")
	public Object traceMethod(ProceedingJoinPoint joinPoint, Traceable traceable) throws Throwable {
		String spanName = traceable.value().isEmpty() ? joinPoint.getSignature().toShortString() : traceable.value();
		System.out.println("spanName --> "+ joinPoint.getSignature().getName());
		Span span = tracer.spanBuilder(spanName).setSpanKind(traceable.spanKind()).startSpan();
		System.out.println(String.format("Enter :: %s.%s - [%s, %s]", joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(), span.getSpanContext().getSpanId(),
				span.getSpanContext().getTraceId()));
		try (Scope scope = span.makeCurrent()) {

			// Add some attributes
			span.setAttribute("method", joinPoint.getSignature().getName());
			span.setAttribute("class", joinPoint.getSignature().getDeclaringTypeName());
			System.out.println(String.format("Exit :: %s.%s - [%s, %s]",
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
					span.getSpanContext().getSpanId(), span.getSpanContext().getTraceId()));
			return joinPoint.proceed();
		} catch (Throwable ex) {
			span.recordException(ex);
			System.out.println(String.format("Error At :: %s.%s - [%s, %s] - %s",
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
					span.getSpanContext().getSpanId(), span.getSpanContext().getTraceId(), ex.getStackTrace()));
			throw ex;
		} finally {
			span.end();
		}
	}
}
