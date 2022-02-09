package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DefaultEmbeddedFieldBuilderTest {

    private static final Function<String, Optional<String>> TEST_STARTCODE_EXTRACTOR =
            input -> {
                if (input == null) {
                    return Optional.empty();
                }
                Matcher matcher = Pattern.compile("Startcode\\s(\\d+)").matcher(input);
                return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
            };

    private DefaultEmbeddedFieldBuilder defaultEmbeddedFieldBuilder;

    @Before
    public void initSetup() {
        Catalog catalog = Catalog.getCatalog("EN");
        this.defaultEmbeddedFieldBuilder =
                new DefaultEmbeddedFieldBuilder(
                        catalog, ScaMethodEntity::getName, TEST_STARTCODE_EXTRACTOR);
    }

    @Test
    public void getOtpFieldsReturnAdditionalFieldForStartCodeIfChipTan() throws LoginException {
        // given
        ScaMethodEntity scaMethod =
                SerializationUtils.deserializeFromString(
                        "{\"authenticationMethodId\": \"MANUAL\", \"authenticationType\": \"CHIP_OTP\", \"authenticationVersion\": \"HHD1.3.2\", \"name\": \"chipTAN manuelle Eingabe | Kartennummer: ******1234\"}",
                        ScaMethodEntity.class);
        ChallengeDataEntity challengeData =
                SerializationUtils.deserializeFromString(
                        "{\"additionalInformation\": \"Sie stimmen einem Zugriff auf Ihre Kontodaten zu. Ist der Auftrag korrekt, gehen Sie wie folgt vor: Stecken Sie Ihre Karte in den TAN-Generator und drücken Sie die Taste \\\"TAN\\\". Geben Sie den Startcode 123488559 ein und drücken Sie die Taste \\\"OK\\\".\", \"otpFormat\": \"integer\", \"otpMaxLength\": 6 }",
                        ChallengeDataEntity.class);

        // when
        List<Field> fields = defaultEmbeddedFieldBuilder.getOtpFields(scaMethod, challengeData);

        // then
        assertThat(fields).hasSize(2);
        assertThat(fields.get(0).getDescription()).isEqualTo("Startcode");
        assertThat(fields.get(0).getValue()).isEqualTo("123488559");
        assertThat(fields.get(1).getDescription()).isEqualTo("TAN");
        assertThat(fields.get(1).getHelpText())
                .contains("chipTAN manuelle Eingabe | Kartennummer: ******1234");
    }

    @Test
    public void getOtpFieldsReturnsJustOneFieldIfNotChipTan() throws LoginException {
        // given
        ScaMethodEntity scaMethod =
                SerializationUtils.deserializeFromString(
                        "{\"authenticationMethodId\": \"nummer2\", \"authenticationType\": \"PUSH_OTP\", \"authenticationVersion\": \"\", \"name\": \"pushTAN | nummer2\"}",
                        ScaMethodEntity.class);

        // when
        List<Field> fields = defaultEmbeddedFieldBuilder.getOtpFields(scaMethod, null);

        // then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getDescription()).isEqualTo("TAN");
        assertThat(fields.get(0).getHelpText()).contains("pushTAN | nummer2");
    }

    @Test
    public void getOtpFieldShouldThrowExceptionIfNotFoundStartCode() {
        // given
        ScaMethodEntity scaMethod =
                SerializationUtils.deserializeFromString(
                        "{\"authenticationMethodId\": \"MANUAL\", \"authenticationType\": \"CHIP_OTP\", \"authenticationVersion\": \"HHD1.3.2\", \"name\": \"chipTAN manuelle Eingabe | Kartennummer: ******1234\"}",
                        ScaMethodEntity.class);
        ChallengeDataEntity challengeData =
                SerializationUtils.deserializeFromString(
                        "{\"additionalInformation\": \"Sie stimmen einem Zugriff auf Ihre Kontodaten zu. Ist der Auftrag korrekt, gehen Sie wie folgt vor: Stecken Sie Ihre Karte in den TAN-Generator und drücken Sie die Taste \\\"TAN\\\". Geben Sie den NO STARTCODE ein und drücken Sie die Taste \\\"OK\\\".\", \"otpFormat\": \"integer\", \"otpMaxLength\": 6 }",
                        ChallengeDataEntity.class);
        // when
        Throwable exception =
                catchThrowable(
                        () -> defaultEmbeddedFieldBuilder.getOtpFields(scaMethod, challengeData));

        // then
        assertThat(exception)
                .isInstanceOf(LoginException.class)
                .hasMessage("Startcode for Chip tan not found");
    }

    @Test
    public void startcodeExtractorBuildOnPatternShouldReturnEmptyOptionalWhenProvidedWithNull() {
        // given
        Function<String, Optional<String>> startcodeExtractor =
                DefaultEmbeddedFieldBuilder.buildStartcodeExtractor(
                        Pattern.compile("Startcode\\s(\\d+)"));

        // when
        Optional<String> extractedStartcode = startcodeExtractor.apply(null);

        // then
        assertThat(extractedStartcode).isEmpty();
    }

    @Test
    public void startcodeExtractorBuildOnPatternShouldReturnProperlyExtractedStartcode() {
        // given
        Function<String, Optional<String>> startcodeExtractor =
                DefaultEmbeddedFieldBuilder.buildStartcodeExtractor(
                        Pattern.compile("Startcode\\s(\\d+)"));

        // when
        Optional<String> extractedStartcode =
                startcodeExtractor.apply(
                        "asdf asdf StartCODE 1234 Startcode 54321 Startcode 12345");

        // then
        assertThat(extractedStartcode).isPresent();
        assertThat(extractedStartcode).hasValue("54321");
    }
}
