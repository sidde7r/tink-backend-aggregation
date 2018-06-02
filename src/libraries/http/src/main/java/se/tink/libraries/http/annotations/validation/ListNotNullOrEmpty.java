package se.tink.libraries.http.annotations.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ListNotNullOrEmpty.Validator.class)
public @interface ListNotNullOrEmpty {

    String message() default "may not be null or empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ListNotNullOrEmpty, List> {

        @Override
        public void initialize(ListNotNullOrEmpty listNotNullOrEmpty) {}

        @Override
        public boolean isValid(List list, ConstraintValidatorContext constraintValidatorContext) {
            return list != null && !list.isEmpty();
        }
    }
}
