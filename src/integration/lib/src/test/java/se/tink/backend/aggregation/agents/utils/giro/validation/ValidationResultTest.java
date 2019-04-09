package se.tink.backend.aggregation.agents.utils.giro.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ValidationResultTest {
    @Test
    public void notSetValuesAreAbsent() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();

        assertThat(validationResult.getAllowedType()).isNull();
        assertThat(validationResult.getValidOcr().isPresent()).isFalse();
        assertThat(validationResult.getValidMessage().isPresent()).isFalse();
    }

    @Test
    public void messageIsNotValidWhenMessageIsAbsent() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();
        validationResult.setAllowedType(GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
        validationResult.setValidOcr("OCR");

        assertThat(validationResult.getValidMessage().isPresent()).isFalse();
    }

    @Test
    public void ocrIsNotValidWhenOcrIsAbsent() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();
        validationResult.setAllowedType(GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
        validationResult.setValidMessage("MESSAGE");

        assertThat(validationResult.getValidOcr().isPresent()).isFalse();
    }

    @Test
    public void messageIsValidWhenMessageIsSet() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();
        validationResult.setAllowedType(GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
        validationResult.setValidMessage("MESSAGE");

        assertThat(validationResult.getValidMessage().orElse(null)).isEqualTo("MESSAGE");
    }

    @Test
    public void ocrIsValidWhenOcrIsSet() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();
        validationResult.setAllowedType(GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
        validationResult.setValidOcr("OCR");

        assertThat(validationResult.getValidOcr().orElse(null)).isEqualTo("OCR");
    }

    @Test
    public void potentiallyIntendedOcrIsSetRegardlessOfTypeOrMessage() {
        GiroMessageValidator.ValidationResult validationResult =
                new GiroMessageValidator.ValidationResult();
        validationResult.setNumericPotentiallyIntentedOcr(true);

        assertThat(validationResult.isNumericPotentiallyIntentedOcr()).isTrue();
    }
}
