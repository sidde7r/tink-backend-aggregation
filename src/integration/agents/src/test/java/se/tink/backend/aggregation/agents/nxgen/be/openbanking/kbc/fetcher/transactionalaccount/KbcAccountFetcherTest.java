package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class KbcAccountFetcherTest {

    private static final String FAKE_BELGIUM_IBAN = "BE13456225778439";

    private BerlinGroupApiClient apiClient;
    private BerlinGroupAccountFetcher fetcher;

    @Before
    public void init() {
        apiClient = mock(KbcApiClient.class);
        fetcher = new BerlinGroupAccountFetcher(apiClient);
    }

    @Test
    @Parameters(method = "checkingAccountKeysParameters")
    public void shouldFetchAndMapCheckingTransactionalAccount(String accountType) {
        // given
        BerlinGroupAccountResponse berlinGroupAccountResponse = getAccountsResponse(accountType);
        given(apiClient.fetchAccounts()).willReturn(berlinGroupAccountResponse);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(
                        KbcConstants.ACCOUNT_TYPE_MAPPER.isOf(
                                accountType, TransactionalAccountType.CHECKING))
                .isTrue();
        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo(FAKE_BELGIUM_IBAN);
        assertThat(account.getName()).isEqualTo(accountType);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
    }

    @Test
    @Parameters(method = "savingAccountKeysParameters")
    public void shouldFetchAndMapSavingTransactionalAccount(String accountType) {
        // given
        BerlinGroupAccountResponse berlinGroupAccountResponse = getAccountsResponse(accountType);
        given(apiClient.fetchAccounts()).willReturn(berlinGroupAccountResponse);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(
                        KbcConstants.ACCOUNT_TYPE_MAPPER.isOf(
                                accountType, TransactionalAccountType.SAVINGS))
                .isTrue();
        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo(FAKE_BELGIUM_IBAN);
        assertThat(account.getName()).isEqualTo(accountType);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new SepaEurIdentifier(FAKE_BELGIUM_IBAN),
                        new IbanIdentifier(FAKE_BELGIUM_IBAN));
    }

    @SuppressWarnings("unused")
    private static Object checkingAccountKeysParameters() {
        return KbcConstants.CHECKING_ACCOUNT_KEYS;
    }

    @SuppressWarnings("unused")
    private static Object savingAccountKeysParameters() {
        return KbcConstants.SAVING_ACCOUNT_KEYS;
    }

    private static AccountResponse getAccountsResponse(String accountType) {
        return SerializationUtils.deserializeFromString(
                "{\"accounts\" : [{\"iban\" : \""
                        + FAKE_BELGIUM_IBAN
                        + "\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"currency\" : \"EUR\", \"product\" : \""
                        + accountType
                        + "\", \"balances\" : [ { \"balanceType\" : \"closingBooked\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }}, { \"balanceType\" : \"expected\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }} ]  }]}",
                AccountResponse.class);
    }
}
