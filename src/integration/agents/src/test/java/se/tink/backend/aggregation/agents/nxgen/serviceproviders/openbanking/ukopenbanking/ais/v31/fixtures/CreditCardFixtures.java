package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFixtures {

    private static final String CREDIT_CARD_ACCOUNT =
            "{\"Account\":[{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-IBAN\",\"SchemeName\":\"UK.OBIE.IBAN\"},{\"Identification\":\"************1234\",\"Name\":\"MR MYSZO-JELEN\",\"SchemeName\":\"UK.OBIE.PAN\"}],\"AccountId\":\"10000000000000691111\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CreditCard\"}";

    public static AccountEntity creditCardAccount() {
        return SerializationUtils.deserializeFromString(CREDIT_CARD_ACCOUNT, AccountEntity.class);
    }
}
