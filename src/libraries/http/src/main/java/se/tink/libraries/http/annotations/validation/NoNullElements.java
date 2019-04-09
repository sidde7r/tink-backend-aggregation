package se.tink.libraries.http.annotations.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoNullElements.Validator.class)
public @interface NoNullElements {

    String message() default "may not contain null elements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<NoNullElements, List> {
        @Override
        public void initialize(final NoNullElements hasId) {}

        @Override
        public boolean isValid(
                List objects, ConstraintValidatorContext constraintValidatorContext) {
            return objects == null || !objects.contains(null);
        }
    }
}
