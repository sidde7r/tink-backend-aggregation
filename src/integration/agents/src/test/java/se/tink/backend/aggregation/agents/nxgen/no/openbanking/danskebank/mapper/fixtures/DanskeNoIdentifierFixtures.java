package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.mapper.fixtures;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class DanskeNoIdentifierFixtures {

    private static final String OTHER_IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"NO11ACA45005678\"}";
    private static final String DANSKE_NO_IBAN =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"NO1234567890123\"}";
    private static final String DANSKE_NO_BBAN =
            "{\"SchemeName\":\"DK.DanskeBank.AccountNumber\",\"Identification\":\"34567890123\"}";
    private static final String PAN_IDENTIFIER =
            "{ \"Identification\": \"1212345678905004\", \"Name\": \"MR MYSZO-JELEN\", \"SchemeName\": \"UK.OBIE.PAN\" }";

    public static AccountIdentifierEntity danskeNoBban() {
        return SerializationUtils.deserializeFromString(
                DANSKE_NO_BBAN, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity otherIbanIdentifier() {
        return SerializationUtils.deserializeFromString(
                OTHER_IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity danskeNoIban() {
        return SerializationUtils.deserializeFromString(
                DANSKE_NO_IBAN, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity panIdentifier() {
        return SerializationUtils.deserializeFromString(
                PAN_IDENTIFIER, AccountIdentifierEntity.class);
    }
}
