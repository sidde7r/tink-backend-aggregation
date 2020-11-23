package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.fixtures;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class DanskeIdentifierFixtures {

    private static final String IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"DK1145005678123455\"}";
    private static final String SHORT_IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"DK11452356781234\"}";
    private static final String DANSKE_IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"DK9830003161123456\"}";
    private static final String SHORT_DANSKE_IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"DK98300031611234\"}";
    private static final String DANSKE_BBAN_IDENTIFIER =
            "{\"SchemeName\":\"DK.DanskeBank.AccountNumber\",\"Identification\":\"30983161123456\"}";
    private static final String SHORT_DANSKE_BBAN_IDENTIFIER =
            "{\"SchemeName\":\"DK.DanskeBank.AccountNumber\",\"Identification\":\"3098316112\"}";
    private static final String PAN_IDENTIFIER =
            "{ \"Identification\": \"1212345678905004\", \"Name\": \"MR MYSZO-JELEN\", \"SchemeName\": \"UK.OBIE.PAN\" }";
    private static final String SHORT_PAN_IDENTIFIER =
            "{ \"Identification\": \"905004\", \"Name\": \"MR MYSZO-JELEN\", \"SchemeName\": \"UK.OBIE.PAN\" }";

    public static AccountIdentifierEntity bbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                DANSKE_BBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity shortBbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                SHORT_DANSKE_BBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity ibanIdentifier() {
        return SerializationUtils.deserializeFromString(
                IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity shortIbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                SHORT_IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity danskeIbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                DANSKE_IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity shortDanskeIbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                SHORT_DANSKE_IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity panIdentifier() {
        return SerializationUtils.deserializeFromString(
                PAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity shortPanIdentifier() {
        return SerializationUtils.deserializeFromString(
                SHORT_PAN_IDENTIFIER, AccountIdentifierEntity.class);
    }
}
