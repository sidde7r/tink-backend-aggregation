package se.tink.backend.aggregation.injectableproviders;

import com.sun.jersey.api.core.HttpContext;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
public @interface ClusterContext {
}
