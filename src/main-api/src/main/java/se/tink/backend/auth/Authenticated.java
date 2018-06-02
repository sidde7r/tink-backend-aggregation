package se.tink.backend.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.core.enums.FeatureFlags;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
public @interface Authenticated {
    boolean required() default true;

    boolean requireAuthorizedDevice() default true;

    FeatureFlags.FeatureFlagGroup[] requireFeatureGroup() default {};

    String[] scopes() default {};

}
