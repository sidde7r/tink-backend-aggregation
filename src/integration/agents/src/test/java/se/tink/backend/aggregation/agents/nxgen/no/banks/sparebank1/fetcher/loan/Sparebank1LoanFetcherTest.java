package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1LoanFetcherTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Test
    public void fetchLoansShouldReturnTinkLoansIfAvailable() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        Sparebank1ApiClient apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        Sparebank1LoanFetcher fetcher = new Sparebank1LoanFetcher(apiClient);

        when(requestBuilder.get(LoanListResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "loan_list_response.json").toFile(),
                                LoanListResponse.class));
        when(requestBuilder.get(LoanDetailsEntity.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "loan_details_response.json").toFile(),
                                LoanDetailsEntity.class));

        // when
        Collection<LoanAccount> loans = fetcher.fetchAccounts();

        // then
        assertThat(loans).isNotEmpty();
        LoanAccount loanAccount = loans.iterator().next();
        assertThat(loanAccount).isNotNull();
        assertThat(loanAccount.getDetails().getApplicants()).isNotEmpty();
        assertThat(loanAccount.getDetails().getApplicants().get(0))
                .isEqualTo("ownerName ownerLastName");
        assertThat(loanAccount.getDetails().getLoanNumber()).isEqualTo("12345678910");
        assertThat(loanAccount.getDetails().getInitialDate()).isEqualTo(new Date(1604401200000L));
        assertThat(loanAccount.getDetails().getNumMonthsBound()).isEqualTo(360);
        assertThat(loanAccount.getDetails().getInitialBalance())
                .isEqualTo(ExactCurrencyAmount.inNOK(-2250000.51));
        assertThat(loanAccount.getDetails().getExactAmortized())
                .isEqualTo(ExactCurrencyAmount.inNOK(-15316.0));
        assertThat(loanAccount.getDetails().getExactMonthlyAmortization())
                .isEqualTo(ExactCurrencyAmount.inNOK(-7602.51));
        assertThat(loanAccount.getInterestRate()).isEqualTo(0.0136);
        assertThat(loanAccount.getExactBalance()).isEqualTo(ExactCurrencyAmount.inNOK(-2234684.51));
        assertThat(loanAccount.getIdModule().getUniqueId()).isEqualTo("12345678910");
    }
}
