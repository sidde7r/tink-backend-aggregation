package se.tink.backend.aggregation.nxgen.http.filter.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FilterOrder {

    FilterPhases category() default FilterPhases.CUSTOM;

    int order() default 100;
}
