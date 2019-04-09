package se.tink.libraries.http.annotations.validation;

import com.google.common.base.Strings;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StringNotNullOrEmpty.Validator.class)
public @interface StringNotNullOrEmpty {

    String message() default "may not be null or empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<StringNotNullOrEmpty, String> {

        @Override
        public void initialize(StringNotNullOrEmpty stringNotNullOrEmpty) {}

        @Override
        public boolean isValid(
                String string, ConstraintValidatorContext constraintValidatorContext) {
            return !Strings.isNullOrEmpty(string);
        }
    }
}
