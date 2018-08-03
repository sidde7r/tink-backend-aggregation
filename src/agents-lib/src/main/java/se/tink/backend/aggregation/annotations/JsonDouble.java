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
 * <p>
 * Example of default annotation behaviour:
 *<pre>
 *     &#64;JsonDouble
 *     double amount = 89.9899999999999;
 *</pre>
 * which will be serialized as
 * <pre>
 *     "amount": 89.99
 * </pre>
 *
 * Example when setting decimals and trailingZeros:
 *<pre>
 *     &#64;JsonDouble(decimals=0, trailingZeros=false)
 *     double amount = 89.9899999999999;
 *</pre>
 * which will be serialized as
 * <pre>
 *     "amount": 90
 * </pre>
 *
 * Example annotation when serializing to a string:
 * <pre>
 *     &#64;JsonDouble(
 *          outputType=JsonType.STRING
 *          decimalSeparator=',',
 *          suffix=" SEK"
 *     )
 *     double amount = 89.9899999999999;
 * </pre>
 * which will be serialized as
 * <pre>
 *     "amount": "89,99 SEK"
 * </pre>
 *
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
