package se.tink.backend.aggregation.agents.utils.supplementalfields.de;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

public class SdkTemplatesEmbeddedFieldBuilderTest {

    private static final Function<String, Optional<String>> TEST_STARTCODE_EXTRACTOR =
            input -> {
                if (input == null) {
                    return Optional.empty();
                }
                Matcher matcher = Pattern.compile("Startcode\\s(\\d+)").matcher(input);
                return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
            };
    private static final Function<String, List<String>> TEST_INSTRUCTIONS_EXTRACTOR =
            input -> {
                if (input == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(Pattern.compile(", ").split(input));
            };

    private SdkTemplatesEmbeddedFieldBuilder sdkTemplatesEmbeddedFieldBuilder;

    @Before
    public void init() {
        sdkTemplatesEmbeddedFieldBuilder =
                new SdkTemplatesEmbeddedFieldBuilder(
                        Catalog.getCatalog(Locale.ENGLISH),
                        ScaMethodEntity::getName,
                        TEST_STARTCODE_EXTRACTOR,
                        TEST_INSTRUCTIONS_EXTRACTOR);
    }

    @Test
    public void getOtpFields_should_return_fields_with_CardReaderTemplate_if_CHIP_OTP_selected() {
        // given
        ScaMethodEntity scaMethod =
                SerializationUtils.deserializeFromString(
                        "{\"authenticationMethodId\": \"MANUAL\", \"authenticationType\": \"CHIP_OTP\", \"authenticationVersion\": \"HHD1.3.2\", \"name\": \"chipTAN manuelle Eingabe | Kartennummer: ******1234\"}",
                        ScaMethodEntity.class);
        ChallengeDataEntity challengeData =
                SerializationUtils.deserializeFromString(
                        "{\"otpMaxLength\":6,\"otpFormat\":\"integer\",\"additionalInformation\":\"Instruction 001, Startcode 88176841, Instruction 003\"}",
                        ChallengeDataEntity.class);
        // when
        List<Field> fields =
                sdkTemplatesEmbeddedFieldBuilder.getOtpFields(scaMethod, challengeData);
        // then
        assertThat(fields).hasSize(4);
        assertField(fields.get(0), "TEMPLATE", "TEMPLATE", "CARD_READER", "TEMPLATE");
        assertField(fields.get(1), "Startcode", "instruction", "88176841", "TEXT");
        assertField(fields.get(2), "TAN", "chipTan", null, "INPUT");
        assertField(
                fields.get(3),
                "Instructions",
                "instructionList",
                "[\"Instruction 001\",\"Startcode 88176841\",\"Instruction 003\"]",
                "TEXT");
    }

    private void assertField(
            Field field, String description, String name, String value, String type) {
        assertThat(field.getDescription()).isEqualTo(description);
        assertThat(field.getName()).isEqualTo(name);
        assertThat(field.getValue()).isEqualTo(value);
        assertThat(field.getType()).isEqualTo(type);
    }

    @Test
    public void getOtpFields_returns_just_one_field_if_not_chipTan() throws LoginException {
        // given
        ScaMethodEntity scaMethod =
                SerializationUtils.deserializeFromString(
                        "{\"authenticationMethodId\": \"nummer2\", \"authenticationType\": \"PUSH_OTP\", \"authenticationVersion\": \"\", \"name\": \"pushTAN | nummer2\"}",
                        ScaMethodEntity.class);

        // when
        List<Field> fields = sdkTemplatesEmbeddedFieldBuilder.getOtpFields(scaMethod, null);

        // then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getDescription()).isEqualTo("TAN");
        assertThat(fields.get(0).getHelpText()).contains("pushTAN | nummer2");
    }
}
