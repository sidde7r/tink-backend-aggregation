package se.tink.backend.aggregation.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Prioritizes the annotated class over others implementing the PrioritizedClientConfiguration
// interface in
// case there are more than one found for a specific provider.
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface PrioritizedClientConfiguration {}
