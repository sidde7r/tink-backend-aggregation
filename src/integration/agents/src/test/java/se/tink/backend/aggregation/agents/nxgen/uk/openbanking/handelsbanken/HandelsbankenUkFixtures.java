package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;

public class HandelsbankenUkFixtures {

    private static final String ALL_TYPE_OF_BALANCES =
            "{\"balances\":[{\"amount\":{\"content\":90.09,\"currency\":\"GBP\"},\"balanceType\":\"AVAILABLE_AMOUNT\"},{\"amount\":{\"content\":90.09,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":90.09,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}]}";
    private static final String CURRENT_BALANCE =
            "{\"balances\":[{\"amount\":{\"content\":90.09,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":90.09,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}]}";
    private static final String NO_BALANCES = "{\"balances\":[]}";

    private static final String CHECKING_ACCOUNT =
            "{\"accountId\":\"acc1\",\"accountType\":\"CURRENT ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String CHECKING_ACCOUNT_WITHOUT_NAME =
            "{\"accountId\":\"acc1\",\"accountType\":\"ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT =
            "{\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT_WITHOUT_NAME =
            "{\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";

    public static AccountDetailsResponse noBalances() throws Exception {
        return objectFromString(NO_BALANCES, AccountDetailsResponse.class);
    }

    public static AccountsItemEntity checkingAccount() throws Exception {
        return objectFromString(CHECKING_ACCOUNT, AccountsItemEntity.class);
    }

    public static AccountDetailsResponse currentBalance() throws Exception {
        return objectFromString(CURRENT_BALANCE, AccountDetailsResponse.class);
    }

    public static AccountDetailsResponse allBalances() throws Exception {
        return objectFromString(ALL_TYPE_OF_BALANCES, AccountDetailsResponse.class);
    }

    public static AccountsItemEntity checkingAccountWithoutProperAccountName() throws Exception {
        return objectFromString(CHECKING_ACCOUNT_WITHOUT_NAME, AccountsItemEntity.class);
    }

    public static AccountsItemEntity savingsAccount() throws Exception {
        return objectFromString(SAVINGS_ACCOUNT, AccountsItemEntity.class);
    }

    public static AccountsItemEntity savingsAccountWithoutProperAccountName() throws Exception {
        return objectFromString(SAVINGS_ACCOUNT_WITHOUT_NAME, AccountsItemEntity.class);
    }

    private static <T> T objectFromString(String json, Class<T> tClass) throws Exception {
        return new ObjectMapper().readValue(json, tClass);
    }
}
