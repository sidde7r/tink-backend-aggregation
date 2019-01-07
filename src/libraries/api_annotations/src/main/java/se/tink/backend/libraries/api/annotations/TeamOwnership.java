package se.tink.backend.libraries.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.libraries.api.annotations.Team;

/**
 * Defines which team is responsible to make sure an API endpoint is functioning correctly. Used on exported API metric
 * labels.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TeamOwnership {
    Team value();
}
