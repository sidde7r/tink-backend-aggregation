package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankCreditCardFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources/";

    private static final String CURRENCY = "NOK";
    private static final String MASKED_CARD_NUMBER = "123456XXXXXX1234";
    private static final String MASKED_CARD_NUMBER_2 = "111111XXXXXX4321";
    private static final String MASKED_CARD_NUMBER_3 = "102938XXXXXX8888";
    private static final String ACCOUNT_NO_EXT = "70626761838";
    private static final String ACCOUNT_NO_EXT_2 = "96112899153";
    private static final String ACCOUNT_NO_EXT_3 = "56473829101";
    private static final String ACCOUNT_NO_INT = "1234512345";
    private static final String ACCOUNT_NO_INT_2 = "0978563412";
    private static final String ACCOUNT_NO_INT_3 = "1928374650";
    private static final String ACCOUNT_NAME = "MC Corporate Card";
    private static final String ACCOUNT_NAME_2 = "MC Test Card";
    private static final String ACCOUNT_NAME_3 = "MC Another Card";
    private static final String CARD_ALIAS_2 = "Mastercard Test Card";
    private static final String CARD_ALIAS_3 = "Mastercard Another Card";
    private static final String IBAN = "NO9570626761838";

    private DanskeBankCreditCardFetcher fetcher;
    private DanskeBankApiClient apiClient;
    private ListAccountsResponse listAccountsResponse;

    @Before
    public void setup() {
        apiClient = mock(DanskeBankApiClient.class);
        DanskeBankConfiguration configuration = mock(DanskeBankConfiguration.class);
        DanskeBankAccountDetailsFetcher accountDetailsFetcher =
                new DanskeBankAccountDetailsFetcher(apiClient);
        fetcher =
                new DanskeBankCreditCardFetcher(
                        apiClient,
                        configuration,
                        new AccountEntityMapper("NO"),
                        accountDetailsFetcher);

        when(configuration.getLanguageCode()).thenReturn("ZZ");
        when(configuration.getMarketCode()).thenReturn("no");
        when(configuration.canExecuteExternalTransfer(any()))
                .thenReturn(AccountCapabilities.Answer.UNINITIALIZED);
        when(configuration.canReceiveExternalTransfer(any()))
                .thenReturn(AccountCapabilities.Answer.UNINITIALIZED);
        when(configuration.canPlaceFunds(any()))
                .thenReturn(AccountCapabilities.Answer.UNINITIALIZED);
        when(configuration.canWithdrawCash(any()))
                .thenReturn(AccountCapabilities.Answer.UNINITIALIZED);

        listAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accountEntities.json").toFile(),
                        ListAccountsResponse.class);
    }

    @Test
    public void shouldFetchCreditCardAccounts() {
        // given
        when(apiClient.listAccounts(any(ListAccountsRequest.class)))
                .thenReturn(listAccountsResponse);

        when(apiClient.fetchAccountDetails(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accountDetails.json").toFile(),
                                AccountDetailsResponse.class));

        when(apiClient.listCards(any(CardsListRequest.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "cardListResponse.json").toFile(),
                                CardsListResponse.class));

        // when
        List<CreditCardAccount> result = (List<CreditCardAccount>) fetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder().build().account);
        assertThat(result.get(1))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder()
                                .cardNumber(MASKED_CARD_NUMBER_2)
                                .cardAlias(CARD_ALIAS_2)
                                .identifiers(
                                        Arrays.asList(
                                                new NorwegianIdentifier(ACCOUNT_NO_EXT_2),
                                                new IbanIdentifier(IBAN),
                                                new MaskedPanIdentifier(MASKED_CARD_NUMBER_2)))
                                .balance(-20.01)
                                .availableCredit(10000)
                                .accountName(ACCOUNT_NAME_2)
                                .accountNoInt(ACCOUNT_NO_INT_2)
                                .accountNoExt(ACCOUNT_NO_EXT_2)
                                .build()
                                .account);
        assertThat(result.get(2))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder()
                                .cardNumber(MASKED_CARD_NUMBER_3)
                                .cardAlias(CARD_ALIAS_3)
                                .identifiers(
                                        Arrays.asList(
                                                new NorwegianIdentifier(ACCOUNT_NO_EXT_3),
                                                new IbanIdentifier(IBAN),
                                                new MaskedPanIdentifier(MASKED_CARD_NUMBER_3)))
                                .balance(-20.11)
                                .availableCredit(1000)
                                .accountName(ACCOUNT_NAME_3)
                                .accountNoInt(ACCOUNT_NO_INT_3)
                                .accountNoExt(ACCOUNT_NO_EXT_3)
                                .build()
                                .account);
    }

    @Test
    public void shouldFetchCreditCardAccountsWhenDetailsOrCardsAreEmpty() {
        // given
        when(apiClient.listAccounts(any(ListAccountsRequest.class)))
                .thenReturn(listAccountsResponse);

        when(apiClient.fetchAccountDetails(any())).thenReturn(new AccountDetailsResponse());

        when(apiClient.listCards(any(CardsListRequest.class))).thenReturn(new CardsListResponse());

        // when
        List<CreditCardAccount> result = (List<CreditCardAccount>) fetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder()
                                .cardNumber(ACCOUNT_NO_EXT)
                                .cardAlias(ACCOUNT_NAME)
                                .identifiers(
                                        Collections.singletonList(
                                                new NorwegianIdentifier(ACCOUNT_NO_EXT)))
                                .parties(Collections.emptyList())
                                .build()
                                .account);
        assertThat(result.get(1))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder()
                                .cardNumber(ACCOUNT_NO_EXT_2)
                                .cardAlias(ACCOUNT_NAME_2)
                                .identifiers(
                                        Collections.singletonList(
                                                new NorwegianIdentifier(ACCOUNT_NO_EXT_2)))
                                .parties(Collections.emptyList())
                                .balance(-20.01)
                                .availableCredit(10000)
                                .accountName(ACCOUNT_NAME_2)
                                .accountNoInt(ACCOUNT_NO_INT_2)
                                .accountNoExt(ACCOUNT_NO_EXT_2)
                                .build()
                                .account);
        assertThat(result.get(2))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder()
                                .cardNumber(ACCOUNT_NO_EXT_3)
                                .cardAlias(ACCOUNT_NAME_3)
                                .identifiers(
                                        Collections.singletonList(
                                                new NorwegianIdentifier(ACCOUNT_NO_EXT_3)))
                                .parties(Collections.emptyList())
                                .balance(-20.11)
                                .availableCredit(1000)
                                .accountName(ACCOUNT_NAME_3)
                                .accountNoInt(ACCOUNT_NO_INT_3)
                                .accountNoExt(ACCOUNT_NO_EXT_3)
                                .build()
                                .account);
    }

    private static class ExpectedCreditCardAccount {
        private CreditCardAccount account;

        private ExpectedCreditCardAccount() {}

        private static class Builder {
            private String cardNumber = MASKED_CARD_NUMBER;
            private String cardAlias = "Mastercard Corporate Gold";
            private List<AccountIdentifier> identifiers =
                    Arrays.asList(
                            new NorwegianIdentifier(ACCOUNT_NO_EXT),
                            new IbanIdentifier(IBAN),
                            new MaskedPanIdentifier(MASKED_CARD_NUMBER));
            private List<Party> parties =
                    Collections.singletonList(new Party("NAME LASTNAME", Party.Role.HOLDER));
            private double balance = -10;
            private double availableCredit = 5000;
            private String accountName = ACCOUNT_NAME;
            private String accountNoInt = ACCOUNT_NO_INT;
            private String accountNoExt = ACCOUNT_NO_EXT;

            private ExpectedCreditCardAccount.Builder cardNumber(String cardNumber) {
                this.cardNumber = cardNumber;
                return this;
            }

            private ExpectedCreditCardAccount.Builder cardAlias(String cardAlias) {
                this.cardAlias = cardAlias;
                return this;
            }

            private ExpectedCreditCardAccount.Builder identifiers(
                    List<AccountIdentifier> identifiers) {
                this.identifiers = identifiers;
                return this;
            }

            private ExpectedCreditCardAccount.Builder parties(List<Party> parties) {
                this.parties = parties;
                return this;
            }

            private ExpectedCreditCardAccount.Builder balance(double balance) {
                this.balance = balance;
                return this;
            }

            private ExpectedCreditCardAccount.Builder availableCredit(double availableCredit) {
                this.availableCredit = availableCredit;
                return this;
            }

            private ExpectedCreditCardAccount.Builder accountName(String accountName) {
                this.accountName = accountName;
                return this;
            }

            private ExpectedCreditCardAccount.Builder accountNoInt(String accountNoInt) {
                this.accountNoInt = accountNoInt;
                return this;
            }

            private ExpectedCreditCardAccount.Builder accountNoExt(String accountNoExt) {
                this.accountNoExt = accountNoExt;
                return this;
            }

            private CreditCardAccount getExpectedCreditCardAccounts(
                    String cardNumber,
                    String cardAlias,
                    List<AccountIdentifier> identifiers,
                    List<Party> parties,
                    double balance,
                    double availableCredit,
                    String accountName,
                    String accountNoInt,
                    String acccuntNoExt) {
                return CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardNumber)
                                        .withBalance(ExactCurrencyAmount.of(balance, CURRENCY))
                                        .withAvailableCredit(
                                                ExactCurrencyAmount.of(availableCredit, CURRENCY))
                                        .withCardAlias(cardAlias)
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNoInt)
                                        .withAccountNumber(acccuntNoExt)
                                        .withAccountName(accountName)
                                        .addIdentifiers(identifiers)
                                        .build())
                        .setBankIdentifier(accountNoInt)
                        .setApiIdentifier(accountNoInt)
                        .canExecuteExternalTransfer(AccountCapabilities.Answer.UNINITIALIZED)
                        .canReceiveExternalTransfer(AccountCapabilities.Answer.UNINITIALIZED)
                        .canPlaceFunds(AccountCapabilities.Answer.UNINITIALIZED)
                        .canWithdrawCash(AccountCapabilities.Answer.UNINITIALIZED)
                        .sourceInfo(
                                AccountSourceInfo.builder()
                                        .bankProductCode("14B")
                                        .bankAccountType("")
                                        .build())
                        .addParties(parties)
                        .build();
            }

            private ExpectedCreditCardAccount build() {
                ExpectedCreditCardAccount expectedCreditCardAccount =
                        new ExpectedCreditCardAccount();
                expectedCreditCardAccount.account =
                        getExpectedCreditCardAccounts(
                                cardNumber,
                                cardAlias,
                                identifiers,
                                parties,
                                balance,
                                availableCredit,
                                accountName,
                                accountNoInt,
                                accountNoExt);
                return expectedCreditCardAccount;
            }
        }
    }
}
