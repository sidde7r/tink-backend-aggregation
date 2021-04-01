package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.SAVINGS;

import java.nio.file.Paths;
import java.util.Collection;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class Xs2aDevelopersTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/resources/";
    private static final GetBalanceResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "balance_response.json").toFile(),
                    GetBalanceResponse.class);
    private Xs2aDevelopersApiClient apiClient = mock(Xs2aDevelopersApiClient.class);
    private Xs2aDevelopersAuthenticatorHelper oauth2Authenticator =
            Mockito.mock(Xs2aDevelopersAuthenticatorHelper.class);
    private Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
            new Xs2aDevelopersTransactionalAccountFetcher(apiClient, oauth2Authenticator);

    @Test
    @Parameters(method = "checkingAccountKeysParameters")
    public void should_fetch_and_map_checking_transactional_account(String accountType) {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = getAccountsResponse.getAccounts().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, CHECKING))
                .isTrue();
        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo("PL666");
        assertThat(account.getName()).isEqualTo("NAME");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
    }

    @Test
    @Parameters(method = "savingAccountKeysParameters")
    public void should_fetch_and_map_saving_transactional_account(String accountType) {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse(accountType);
        AccountEntity accountEntity = getAccountsResponse.getAccounts().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, SAVINGS)).isTrue();

        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo("PL666");
        assertThat(account.getName()).isEqualTo("NAME");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
    }

    @Test
    public void should_not_map_unknown_type_transactional_account() {
        // given
        GetAccountsResponse getAccountsResponse = getAccountsResponse("UNKNOWN_TYPE");
        AccountEntity accountEntity = getAccountsResponse.getAccounts().get(0);
        given(apiClient.getAccounts()).willReturn(getAccountsResponse);
        given(apiClient.getBalance(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @SuppressWarnings("unused")
    private static Object checkingAccountKeysParameters() {
        return Xs2aDevelopersConstants.CHECKING_ACCOUNT_KEYS;
    }

    @SuppressWarnings("unused")
    private static Object savingAccountKeysParameters() {
        return Xs2aDevelopersConstants.SAVING_ACCOUNT_KEYS;
    }

    private static GetAccountsResponse getAccountsResponse(String accountType) {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\" : [{\"iban\" : \"PL666\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"currency\" : \"EUR\", \"product\" : \""
                        + accountType
                        + "\"}]}",
                GetAccountsResponse.class);
    }
}
