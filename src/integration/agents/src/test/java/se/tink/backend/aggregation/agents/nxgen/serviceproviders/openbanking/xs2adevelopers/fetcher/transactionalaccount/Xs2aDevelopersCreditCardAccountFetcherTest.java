package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Xs2aDevelopersCreditCardAccountFetcherTest {

    private static final GetAccountsResponse SINGLE_CREDIT_CARD_ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\" : [{\"resourceId\" : \"1234567890123456789\", \"name\" : \"Visa-Karte (Prepaid-Kreditkarte)\", \"bic\" : \"COBADEHD044\", \"currency\" : \"EUR\", \"ownerName\" : \"John Doe\", \"maskedPan\" : \"123456XXXXXX7890\", \"_links\" : {\"account\" : {\"href\" : \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789\"}, \"balances\" : {\"href\" : \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/balances\"}, \"transactions\" : {\"href\" : \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/transactions\"} }, \"product\" : \"Prepaid-Kreditkarte\"}]}",
                    GetAccountsResponse.class);

    private static final GetAccountsResponse TWO_CREDIT_CARD_ACCOUNT_AND_FEW_OTHERS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\": [{\"_links\": {\"account\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789\"}, \"balances\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/balances\"}, \"transactions\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/transactions\"} }, \"bic\": \"COBADEHD044\", \"currency\": \"EUR\", \"maskedPan\": \"123456XXXXXX7890\", \"name\": \"Visa-Karte (Prepaid-Kreditkarte)\", \"ownerName\": \"John Doe\", \"product\": \"Prepaid-Kreditkarte\", \"resourceId\": \"1234567890123456789\"}, {\"_links\": {\"account\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/345603e5ytkhjsdfkjgkjerg\"}, \"balances\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/345603e5ytkhjsdfkjgkjerg/balances\"}, \"transactions\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/345603e5ytkhjsdfkjgkjerg/transactions\"} }, \"bic\": \"COBADEHD044\", \"cashAccountType\": \"SVGS\", \"currency\": \"EUR\", \"iban\": \"DE235235235235\", \"name\": \"Tagesgeld PLUS-Konto\", \"ownerName\": \"John Doe\", \"product\": \"Tagesgeldkonto\", \"resourceId\": \"345603e5ytkhjsdfkjgkjerg\"}, {\"_links\": {\"account\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/ygiusydfiuby7xcyvb78yxcv87b\"}, \"balances\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/ygiusydfiuby7xcyvb78yxcv87b/balances\"}, \"transactions\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/ygiusydfiuby7xcyvb78yxcv87b/transactions\"} }, \"bic\": \"COBADEHD044\", \"cashAccountType\": \"CACC\", \"currency\": \"EUR\", \"iban\": \"DEsdfgbxcvbcvbxcvb\", \"name\": \"Girokonto\", \"ownerName\": \"John Doe\", \"product\": \"Girokonto\", \"resourceId\": \"ygiusydfiuby7xcyvb78yxcv87b\"}, {\"_links\": {\"account\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789\"}, \"balances\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/balances\"}, \"transactions\": {\"href\": \"https://xs2a-api.comdirect.de/berlingroup/v1/accounts/1234567890123456789/transactions\"} }, \"bic\": \"COBADEHD044\", \"currency\": \"EUR\", \"maskedPan\": \"654321XXXXXX7890\", \"name\": \"Visa-Karte (Prepaid-Kreditkarte)\", \"ownerName\": \"John Doe\", \"product\": \"Prepaid-Kreditkarte\", \"resourceId\": \"1234567890123456789\"} ] }",
                    GetAccountsResponse.class);

    private static final GetBalanceResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"account\" : {\"maskedPan\" : \"123456XXXXXX7890\", \"currency\" : \"EUR\"}, \"balances\" : [ {\"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 1234.12 }, \"referenceDate\" : \"2020-04-20\", \"balanceType\" : \"authorised\"}, {\"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 1234.12 }, \"referenceDate\" : \"2020-04-20\", \"balanceType\" : \"closingBooked\"} ] }",
                    GetBalanceResponse.class);

    private Xs2aDevelopersApiClient apiClient = mock(Xs2aDevelopersApiClient.class);
    private Xs2aDevelopersCreditCardAccountFetcher creditCardFetcher =
            new Xs2aDevelopersCreditCardAccountFetcher(apiClient);

    @Test
    public void should_return_properly_mapped_account() {
        // given
        AccountEntity accountEntity = SINGLE_CREDIT_CARD_ACCOUNT_RESPONSE.getAccounts().get(0);
        given(apiClient.getAccounts()).willReturn(SINGLE_CREDIT_CARD_ACCOUNT_RESPONSE);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(1);
        assertCreditCardAccountIsProperlyMapped(
                creditCardAccounts.iterator().next(),
                "123456XXXXXX7890",
                "Visa-Karte (Prepaid-Kreditkarte)",
                ExactCurrencyAmount.of("1234.12", "EUR"));
        verify(apiClient).getAccounts();
        verify(apiClient).getBalance(accountEntity);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void should_only_map_credit_card_accounts() {
        // given
        AccountEntity accountEntityOne =
                TWO_CREDIT_CARD_ACCOUNT_AND_FEW_OTHERS_RESPONSE.getAccounts().get(0);
        AccountEntity accountEntityTwo =
                TWO_CREDIT_CARD_ACCOUNT_AND_FEW_OTHERS_RESPONSE.getAccounts().get(3);
        given(apiClient.getAccounts()).willReturn(TWO_CREDIT_CARD_ACCOUNT_AND_FEW_OTHERS_RESPONSE);
        given(apiClient.getBalance(accountEntityOne)).willReturn(BALANCE_RESPONSE);
        given(apiClient.getBalance(accountEntityTwo)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(2);
        Iterator<CreditCardAccount> iterator = creditCardAccounts.iterator();
        assertCreditCardAccountIsProperlyMapped(
                iterator.next(),
                "123456XXXXXX7890",
                "Visa-Karte (Prepaid-Kreditkarte)",
                ExactCurrencyAmount.of("1234.12", "EUR"));
        assertCreditCardAccountIsProperlyMapped(
                iterator.next(),
                "654321XXXXXX7890",
                "Visa-Karte (Prepaid-Kreditkarte)",
                ExactCurrencyAmount.of("1234.12", "EUR"));
        verify(apiClient).getAccounts();
        verify(apiClient).getBalance(accountEntityOne);
        verify(apiClient).getBalance(accountEntityTwo);
        verifyNoMoreInteractions(apiClient);
    }

    private void assertCreditCardAccountIsProperlyMapped(
            CreditCardAccount account,
            String accountNumber,
            String name,
            ExactCurrencyAmount balance) {
        assertThat(account.getCardModule().getCardNumber()).isEqualTo(accountNumber);
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getName()).isEqualTo(name);
        assertThat(account.getExactBalance()).isEqualTo(balance);
    }
}
