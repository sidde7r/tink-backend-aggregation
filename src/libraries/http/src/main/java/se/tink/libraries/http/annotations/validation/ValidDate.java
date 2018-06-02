package se.tink.libraries.http.annotations.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDate.Validator.class)
public @interface ValidDate {

    String message() default "is too far back in time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidDate, Date> {

        @Override
        public void initialize(ValidDate validDate) {}

        @Override
        public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
            return date != null && !date.before(new Date(0));
        }
    }
}
