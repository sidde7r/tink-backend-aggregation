package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts.HandelsbankenUkAccountConverter;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenAccountConverterTest {

    private static final String CHECKING_ACCOUNT =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CURRENT ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String CHECKING_ACCOUNT_WITHOUT_NAME =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT_WITHOUT_NAME =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT\",\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB97HAND40516284223103\",\"ownerName\":\"FLUFFY\"}";
    private static final String CHECKING_ACCOUNT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CURRENT ACCOUNT\",\"balances\":[{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"AVAILABLE_AMOUNT\"},{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}],\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"balances\":[{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}],\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String ACCOUNT_WITHOUT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"balances\":[{\"amount\":{\"content\":1.01,\"currency\":\"GBP\"},\"balanceType\":\"OPENING_CLEARED\"}],\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";

    private HandelsbankenUkAccountConverter accountConverter;

    @Before
    public void setup() {
        accountConverter = new HandelsbankenUkAccountConverter();
    }

    @Test
    public void shouldParseCheckingAccount() throws Exception {
        // given
        AccountsItemEntity account = objectFromString(CHECKING_ACCOUNT, AccountsItemEntity.class);
        AccountDetailsResponse detailsResponse =
                objectFromString(CHECKING_ACCOUNT_BALANCES, AccountDetailsResponse.class);

        // when
        Optional<TransactionalAccount> result =
                accountConverter.toTinkAccount(account, detailsResponse);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(createExpectedAccount(TransactionalAccountType.CHECKING));
    }

    @Test
    public void shouldParseCheckingAccountWithoutName() throws Exception {
        // given
        AccountsItemEntity account =
                objectFromString(CHECKING_ACCOUNT_WITHOUT_NAME, AccountsItemEntity.class);
        AccountDetailsResponse detailsResponse =
                objectFromString(CHECKING_ACCOUNT_BALANCES, AccountDetailsResponse.class);

        // when
        Optional<TransactionalAccount> result =
                accountConverter.toTinkAccount(account, detailsResponse);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        createExpectedAccount(
                                TransactionalAccountType.CHECKING,
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "GB06HAND40516249747837",
                                        "ACCOUNT")));
    }

    @Test
    public void shouldParseSavingsAccount() throws Exception {
        // given
        AccountsItemEntity account = objectFromString(SAVINGS_ACCOUNT, AccountsItemEntity.class);
        AccountDetailsResponse detailsResponse =
                objectFromString(SAVINGS_ACCOUNT_BALANCES, AccountDetailsResponse.class);

        // when
        Optional<TransactionalAccount> result =
                accountConverter.toTinkAccount(account, detailsResponse);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(createExpectedAccount(TransactionalAccountType.SAVINGS));
    }

    @Test
    public void shouldParseSavingsAccountWithoutName() throws Exception {
        // given
        AccountsItemEntity account =
                objectFromString(SAVINGS_ACCOUNT_WITHOUT_NAME, AccountsItemEntity.class);
        AccountDetailsResponse detailsResponse =
                objectFromString(SAVINGS_ACCOUNT_BALANCES, AccountDetailsResponse.class);

        // when
        Optional<TransactionalAccount> result =
                accountConverter.toTinkAccount(account, detailsResponse);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        createExpectedAccount(
                                TransactionalAccountType.SAVINGS,
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN,
                                        "GB97HAND40516284223103",
                                        "CLIENT ACCOUNT")));
    }

    @Test
    public void shouldThrowBalanceException() throws Exception {
        // given
        AccountsItemEntity account = objectFromString(SAVINGS_ACCOUNT, AccountsItemEntity.class);
        AccountDetailsResponse detailsResponse =
                objectFromString(ACCOUNT_WITHOUT_BALANCES, AccountDetailsResponse.class);

        // when
        Throwable throwable =
                catchThrowable(() -> accountConverter.toTinkAccount(account, detailsResponse));

        // then
        assertThat(throwable)
                .isInstanceOf(AccountRefreshException.class)
                .hasMessage(ExceptionMessages.BALANCE_NOT_FOUND);
    }

    private <T> T objectFromString(String json, Class<T> tClass) throws Exception {
        return new ObjectMapper().readValue(json, tClass);
    }

    private Optional<TransactionalAccount> createExpectedAccount(TransactionalAccountType type) {
        return createExpectedAccount(type, null);
    }

    private Optional<TransactionalAccount> createExpectedAccount(
            TransactionalAccountType type, AccountIdentifier accountIdentifier) {
        if (accountIdentifier == null) {
            if (type == TransactionalAccountType.CHECKING) {
                accountIdentifier =
                        AccountIdentifier.create(
                                AccountIdentifierType.IBAN,
                                "GB06HAND40516249747837",
                                "CURRENT ACCOUNT");
            } else {
                accountIdentifier =
                        AccountIdentifier.create(
                                AccountIdentifierType.IBAN,
                                "GB06HAND40516249747837",
                                "CLIENT ACCOUNT DEPOSITS");
            }
        }

        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(1.01, "GBP")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("49747837")
                                .withAccountNumber("405162-49747837")
                                .withAccountName(accountIdentifier.getName().get())
                                .addIdentifier(accountIdentifier)
                                .build())
                .addHolderName("FLUFFY")
                .setApiIdentifier("acc1")
                .build();
    }
}
