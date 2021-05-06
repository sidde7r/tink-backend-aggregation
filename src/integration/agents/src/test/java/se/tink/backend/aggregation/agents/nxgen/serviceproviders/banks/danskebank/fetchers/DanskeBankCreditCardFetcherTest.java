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
    private static final String ACCOUNT_NO_EXT = "70626761838";
    private static final String ACCOUNT_NO_INT = "1234512345";
    private static final String ACCOUNT_NAME = "MC Corporate Card";

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
        assertThat(result.get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        new ExpectedCreditCardAccount.Builder().build().account);
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
                            new IbanIdentifier("NO9570626761838"),
                            new MaskedPanIdentifier(MASKED_CARD_NUMBER));
            private List<Party> parties =
                    Collections.singletonList(new Party("NAME LASTNAME", Party.Role.HOLDER));

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

            private CreditCardAccount getExpectedCreditCardAccounts(
                    String cardNumber,
                    String cardAlias,
                    List<AccountIdentifier> identifiers,
                    List<Party> parties) {
                return CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardNumber)
                                        .withBalance(ExactCurrencyAmount.of(-10, CURRENCY))
                                        .withAvailableCredit(ExactCurrencyAmount.of(5000, CURRENCY))
                                        .withCardAlias(cardAlias)
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(ACCOUNT_NO_INT)
                                        .withAccountNumber(ACCOUNT_NO_EXT)
                                        .withAccountName(ACCOUNT_NAME)
                                        .addIdentifiers(identifiers)
                                        .build())
                        .setBankIdentifier(ACCOUNT_NO_INT)
                        .setApiIdentifier(ACCOUNT_NO_INT)
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
                        getExpectedCreditCardAccounts(cardNumber, cardAlias, identifiers, parties);
                return expectedCreditCardAccount;
            }
        }
    }
}
