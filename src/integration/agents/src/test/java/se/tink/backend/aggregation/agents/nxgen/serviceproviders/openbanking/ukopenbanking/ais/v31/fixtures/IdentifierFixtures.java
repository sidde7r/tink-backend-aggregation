package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IdentifierFixtures {

    private static final String PAN_IDENTIFIER =
            "{ \"Identification\": \"************5004\", \"Name\": \"Mr Myszo-Jelen\", \"SchemeName\": \"UK.OBIE.PAN\" }";
    private static final String SORT_CODE_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"}";
    private static final String IBAN_IDENTIFIER =
            "{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}";
    private static final String CURRENCY_ACCOUNT_IDENTIFIER =
            "{\"SchemeName\": \"UK.NWB.CurrencyAccount\",\"Identification\": \"111/11/11111111\",\"SecondaryIdentification\": \"111111111\",\"Name\": \"Kapitan Bomba\"}";

    public static AccountIdentifierEntity panIdentifier() {
        return SerializationUtils.deserializeFromString(
                PAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity ibanIdentifier() {
        return SerializationUtils.deserializeFromString(
                IBAN_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity sortCodeIdentifier() {
        return SerializationUtils.deserializeFromString(
                SORT_CODE_IDENTIFIER, AccountIdentifierEntity.class);
    }

    public static AccountIdentifierEntity currencyAccountIdentifier() {
        return SerializationUtils.deserializeFromString(
                CURRENCY_ACCOUNT_IDENTIFIER, AccountIdentifierEntity.class);
    }
}
