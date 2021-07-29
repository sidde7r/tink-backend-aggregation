package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FieldBuilderPaymentsTest {
    private FieldBuilderPayments fieldBuilderPayments;

    @Before
    public void init() {
        fieldBuilderPayments =
                new FieldBuilderPayments(
                        Catalog.getCatalog(Locale.ENGLISH), new SparkassenIconUrlMapper());
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
                        "{\"otpMaxLength\":6,\"otpFormat\":\"integer\",\"additionalInformation\":\"Sie haben eine \\\"Terminüberweisung\\\" an die Empfänger-IBAN *** bei dem Institut \\\"SPARDA-BANK NUERNBERG\\\" in Höhe von 1,00 EUR erfasst. Bitte überprüfen Sie die Richtigkeit der Daten. Ist der Auftrag korrekt, gehen Sie wie folgt vor: Stecken Sie Ihre Karte in den TAN-Generator und drücken Sie die Taste \\\"TAN\\\". Geben Sie den \\\"Startcode 88176841\\\" ein und drücken Sie die Taste \\\"OK\\\". Geben Sie die \\\"letzten 10 Ziffern der IBAN des Empfängers\\\" ein und drücken Sie die Taste \\\"OK\\\". Geben Sie den \\\"Betrag\\\" (Euro und Cent durch Komma getrennt) ein und drücken Sie die Taste \\\"OK\\\".\"}",
                        ChallengeDataEntity.class);
        // when
        List<Field> fields = fieldBuilderPayments.getOtpFields(scaMethod, challengeData);
        // then
        assertThat(fields).hasSize(4);
        assertField(fields.get(0), "TEMPLATE", "TEMPLATE", "CARD_READER", "TEMPLATE");
        assertField(fields.get(1), "Startcode", "instruction", "88176841", "TEXT");
        assertField(fields.get(2), "TAN", "chipTan", null, "INPUT");
        assertField(
                fields.get(3),
                "Instructions",
                "instructionList",
                "[\"Stecken Sie Ihre Karte in den TAN-Generator und drücken Sie die Taste \\\"TAN\\\"\",\"Geben Sie den \\\"Startcode 88176841\\\" ein und drücken Sie die Taste \\\"OK\\\"\",\"Geben Sie die \\\"letzten 10 Ziffern der IBAN des Empfängers\\\" ein und drücken Sie die Taste \\\"OK\\\"\",\"Geben Sie den \\\"Betrag\\\" (Euro und Cent durch Komma getrennt) ein und drücken Sie die Taste \\\"OK\\\".\"]",
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
        List<Field> fields = fieldBuilderPayments.getOtpFields(scaMethod, null);

        // then
        assertThat(fields).hasSize(1);
        assertThat(fields.get(0).getDescription()).isEqualTo("TAN");
        assertThat(fields.get(0).getHelpText()).contains("pushTAN | nummer2");
    }
}
