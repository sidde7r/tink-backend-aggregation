package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class BankiaLoanFetcherTest {

    public static final String LOAN_IDENTIFIER = "loan_identifier";
    public static final String PRODUCT_CODE = "product_code";
    @Mock private BankiaApiClient bankiaApiClientTest;

    @InjectMocks private BankiaLoanFetcher bankiaLoanFetcher;

    @Test
    public void shouldReturnCollectionWhenWeFetchTheOneLoanWithDetails() {
        // given
        LoanAccountEntity accountEntity = mock(LoanAccountEntity.class);
        when(accountEntity.getLoanIdentifier()).thenReturn(LOAN_IDENTIFIER);
        when(accountEntity.getProductCode()).thenReturn(PRODUCT_CODE);
        when(bankiaApiClientTest.getLoans()).thenReturn(Collections.singletonList(accountEntity));

        LoanDetailsResponse loanDetailsResponse = mock(LoanDetailsResponse.class);
        Either<LoanDetailsErrorCode, LoanDetailsResponse> right = Either.right(loanDetailsResponse);
        when(bankiaApiClientTest.getLoanDetails(Mockito.any())).thenReturn(right);

        // when
        Collection<LoanAccount> accounts = bankiaLoanFetcher.fetchAccounts();

        // then
        assertThat(accounts.isEmpty()).isFalse();
        assertThat(accounts.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnEmptyListWhenWeFetchTheOneLoanButWithoutDetails() {
        // given
        LoanAccountEntity accountEntity = mock(LoanAccountEntity.class);
        when(accountEntity.getLoanIdentifier()).thenReturn(LOAN_IDENTIFIER);
        when(accountEntity.getProductCode()).thenReturn(PRODUCT_CODE);
        when(bankiaApiClientTest.getLoans()).thenReturn(Collections.singletonList(accountEntity));

        HttpResponse response = mock(HttpResponse.class);

        when(bankiaApiClientTest.getLoanDetails(Mockito.any()))
                .thenThrow(new HttpResponseException(null, response));

        // when
        Collection<LoanAccount> accounts = bankiaLoanFetcher.fetchAccounts();

        // then
        assertThat(accounts.isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnEmptyListWhenWeAreNotAbleToFetchLoans() {
        // given
        HttpResponse response = mock(HttpResponse.class);

        when(bankiaApiClientTest.getLoans()).thenThrow(new HttpResponseException(null, response));

        // when
        Collection<LoanAccount> accounts = bankiaLoanFetcher.fetchAccounts();

        // then
        assertThat(accounts.isEmpty()).isTrue();
    }
}
