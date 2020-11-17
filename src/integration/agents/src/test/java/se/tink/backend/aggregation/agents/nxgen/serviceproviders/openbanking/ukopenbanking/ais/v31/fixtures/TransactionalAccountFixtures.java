package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionalAccountFixtures {

    private static final String CURRENT_ACCOUNT_BUSINESS =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Business\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
    private static final String CURRENT_ACCOUNT =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"CurrentAccount\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
    private static final String SAVINGS_ACCOUNT =
            "{\"AccountId\":\"xxxiddddxxxx\",\"Currency\":\"GBP\",\"AccountType\":\"Personal\",\"AccountSubType\":\"Savings\",\"Nickname\":\"someNickname\",\"Account\":[{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"07111111111111\",\"Name\":\"Myszon Jelen\"},{\"SchemeName\":\"UK.OBIE.IBAN\",\"Identification\":\"1234 NAIA 5678 1234 55 1111\",\"Name\":\"Myszo Jelen\"}]}";
    private static final String SAVINGS_ACCOUNT_WITHOUT_ID =
            "{\"Account\": [],\"AccountId\": \"8ffd285c-2cf8-4637-b8b9-a20ebff12c04\",\"AccountSubType\": \"Savings\",\"AccountType\":\"Personal\",\"Currency\": \"EUR\",\"Nickname\": \"Sommar2000\"}";

    public static AccountEntity currentAccount() {
        return SerializationUtils.deserializeFromString(CURRENT_ACCOUNT, AccountEntity.class);
    }

    public static AccountEntity currentAccountBusiness() {
        return SerializationUtils.deserializeFromString(
                CURRENT_ACCOUNT_BUSINESS, AccountEntity.class);
    }

    public static AccountEntity savingsAccount() {
        return SerializationUtils.deserializeFromString(SAVINGS_ACCOUNT, AccountEntity.class);
    }

    public static AccountEntity savingsAccountWithoutId() {
        return SerializationUtils.deserializeFromString(
                SAVINGS_ACCOUNT_WITHOUT_ID, AccountEntity.class);
    }
}
