package se.tink.backend.aggregation.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.aggregation.annotations.serializers.DoubleSerializer;

/**
 * Annotation used for serializing double values in a more controlled
 * way (e.g. by limiting number of decimals, trailing zeros, or by
 * converting to String and setting grouping separator, prefix, etc),
 * by attaching to "getter" methods, or fields.
 *
 * When annotating value classes, configuration is used for instances
 * of the value class but can be overridden by more specific annotations
 * (ones that attach to methods or fields).
 *
 *<p>
 * An example annotation would be:
 *<pre>
 *  &#64;JsonDouble(outputType=JsonType.NUMERIC,
 *    decimals=2,
 *    decimalSeparator=',',
 *    prefix="€"
 *  )
 *</pre>
 *
 * (which would be redundant, since outputType and decimals are set like
 * this by default, and decimalSeparator and prefix are disregarded when
 * outputType is JsonType.NUMERIC (since this could result in invalid JSON).
 */
@JacksonAnnotationsInside
@JsonSerialize(using = DoubleSerializer.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface JsonDouble {
    
    JsonType outputType() default JsonType.NUMERIC;

    // Always applied
    int decimals() default 2;
    boolean trailingZeros() default true;

    // Only applied when outputType is set to STRING
    char decimalSeparator() default '.';
    char groupingSeparator() default Character.MIN_VALUE; // no grouping applied when '\0'
    String prefix() default "";
    String suffix() default "";

    enum JsonType {
        STRING,
        NUMERIC;
    }
}
