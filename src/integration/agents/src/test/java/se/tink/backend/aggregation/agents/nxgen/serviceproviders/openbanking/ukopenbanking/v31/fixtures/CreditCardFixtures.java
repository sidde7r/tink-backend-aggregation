package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFixtures {

    private static final String CREDIT_CARD_ACCOUNT =
            "{\"Account\":[{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-IBAN\",\"SchemeName\":\"UK.OBIE.IBAN\"},{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-JELEN\",\"SchemeName\":\"UK.OBIE.PAN\"}],\"AccountId\":\"10000000000000691111\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CreditCard\"}";

    private static final String PAN_IDENTIFIER =
            "{ \"Identification\": \"************5004\", \"Name\": \"MR MYSZO-JELEN\", \"SchemeName\": \"UK.OBIE.PAN\" }";

    private static final String TEMPORARY_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"4087.64\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Temporary\"}";
    private static final String AVAILABLE_CREDIT_LINE =
            "{\"Amount\":{\"Amount\":\"4087.64\",\"Currency\":\"GBP\"},\"Included\":false,\"Type\":\"Available\"}";

    public static CreditLineEntity temporaryCreditLine() {
        return SerializationUtils.deserializeFromString(
                TEMPORARY_CREDIT_LINE, CreditLineEntity.class);
    }

    public static CreditLineEntity availableCreditLine() {
        return SerializationUtils.deserializeFromString(
                AVAILABLE_CREDIT_LINE, CreditLineEntity.class);
    }

    public static AccountEntity creditCardAccount() {
        return SerializationUtils.deserializeFromString(CREDIT_CARD_ACCOUNT, AccountEntity.class);
    }
}
