package se.tink.backend.aggregation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.aggregation.constants.Capability;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Implements {
    Capability[] value();
}
