package se.tink.libraries.repository.guice.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Centralized {}
