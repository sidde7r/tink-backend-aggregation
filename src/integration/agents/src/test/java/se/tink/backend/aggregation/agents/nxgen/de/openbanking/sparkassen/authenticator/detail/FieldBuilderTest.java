package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.libraries.i18n.Catalog;

public class FieldBuilderTest {
    private FieldBuilder fieldBuilder;

    @Before
    public void initSetup() {
        Catalog catalog = Catalog.getCatalog("EN");
        this.fieldBuilder = new FieldBuilder(catalog);
    }

    @Test
    public void getOtpFieldShouldGetDescriptionWithStartCodeIfChipTan() throws LoginException {
        // given
        String otpType = "CHIP_OTP";
        int otpValueLength = 6;
        String additionalInformation =
                " Geben Sie den Startcode 80080053 ein und drücken Sie die Taste.";

        // when
        Field field = fieldBuilder.getOtpField(otpType, otpValueLength, additionalInformation);

        // then
        assertThat(field.getDescription()).contains("80080053");
    }

    @Test
    public void getOtpFieldShouldThrowExceptionIfNotFoundStartCode() {
        // given
        String otpType = "CHIP_OTP";
        int otpValueLength = 6;
        String additionalInformation = "Geben Sie den Startcode ein und drücken Sie die Taste.";

        // when
        Throwable exception =
                catchThrowable(
                        () ->
                                fieldBuilder.getOtpField(
                                        otpType, otpValueLength, additionalInformation));

        // then
        assertThat(exception)
                .isInstanceOf(LoginException.class)
                .hasMessage("Startcode fo Chip tan not found");
    }
}
