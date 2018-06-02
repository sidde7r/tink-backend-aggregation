package se.tink.libraries.http.annotations.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container Jersey resource that require client token authentication to be accessed.
 */
@Inherited
@Target({
        ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowClient {
    String value();
}
