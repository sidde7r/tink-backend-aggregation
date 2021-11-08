package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.HandelsbankenConstants.ExceptionMessages;
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
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc2/transactions\"}},\"accountId\":\"acc2\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"bban\":\"84223103\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB97HAND40516284223103\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT_WITHOUT_NAME =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc2/transactions\"}},\"accountId\":\"acc2\",\"accountType\":\"CLIENT ACCOUNT\",\"bban\":\"84223103\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB97HAND40516284223103\",\"ownerName\":\"FLUFFY\"}";
    private static final String CHECKING_ACCOUNT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc1/transactions\"}},\"accountId\":\"acc1\",\"accountType\":\"CURRENT ACCOUNT\",\"balances\":[{\"amount\":{\"content\":33713.47,\"currency\":\"GBP\"},\"balanceType\":\"AVAILABLE_AMOUNT\"},{\"amount\":{\"content\":33670.35,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":33670.35,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}],\"bban\":\"49747837\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB06HAND40516249747837\",\"ownerName\":\"FLUFFY\"}";
    private static final String SAVINGS_ACCOUNT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc2/transactions\"}},\"accountId\":\"acc2\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"balances\":[{\"amount\":{\"content\":13264.5,\"currency\":\"GBP\"},\"balanceType\":\"CURRENT\"},{\"amount\":{\"content\":13264.5,\"currency\":\"GBP\"},\"balanceType\":\"CLEARED\"}],\"bban\":\"84223103\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB97HAND40516284223103\",\"ownerName\":\"FLUFFY\"}";
    private static final String ACCOUNT_WITHOUT_BALANCES =
            "{\"_links\":{\"transactions\":{\"href\":\"https://api.handelsbanken.com/openbanking/psd2/v2/accounts/acc2/transactions\"}},\"accountId\":\"acc2\",\"accountType\":\"CLIENT ACCOUNT DEPOSITS\",\"balances\":[{\"amount\":{\"content\":13264.5,\"currency\":\"GBP\"},\"balanceType\":\"OPENING_CLEARED\"}],\"bban\":\"84223103\",\"bic\":\"HANDGB22\",\"clearingNumber\":\"405162\",\"currency\":\"GBP\",\"iban\":\"GB97HAND40516284223103\",\"ownerName\":\"FLUFFY\"}";

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
                .isEqualTo(createExpectedCheckingAccount(null));
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
                        createExpectedCheckingAccount(
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
        assertThat(result).usingRecursiveComparison().isEqualTo(createExpectedSavingsAccount(null));
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
                        createExpectedSavingsAccount(
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
                .hasMessage(ExceptionMessages.AVAILABLE_BALANCE_NOT_FOUND);
    }

    private <T> T objectFromString(String json, Class<T> tClass) throws Exception {
        return new ObjectMapper().readValue(json, tClass);
    }

    private Optional<TransactionalAccount> createExpectedCheckingAccount(
            AccountIdentifier accountIdentifier) {
        if (accountIdentifier == null) {
            accountIdentifier =
                    AccountIdentifier.create(
                            AccountIdentifierType.IBAN,
                            "GB06HAND40516249747837",
                            "CURRENT ACCOUNT");
        }

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(33713.47, "GBP")))
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

    private Optional<TransactionalAccount> createExpectedSavingsAccount(
            AccountIdentifier accountIdentifier) {
        if (accountIdentifier == null) {
            accountIdentifier =
                    AccountIdentifier.create(
                            AccountIdentifierType.IBAN,
                            "GB97HAND40516284223103",
                            "CLIENT ACCOUNT DEPOSITS");
        }
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(13264.5, "GBP")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("84223103")
                                .withAccountNumber("405162-84223103")
                                .withAccountName(accountIdentifier.getName().get())
                                .addIdentifier(accountIdentifier)
                                .build())
                .addHolderName("FLUFFY")
                .setApiIdentifier("acc2")
                .build();
    }
}
