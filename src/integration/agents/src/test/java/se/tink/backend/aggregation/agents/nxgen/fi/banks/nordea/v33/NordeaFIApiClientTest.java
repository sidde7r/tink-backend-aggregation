package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class NordeaFIApiClientTest {
    @Mock TinkHttpClient httpClient;
    @Mock SessionStorage sessionStorage;
    @Mock RequestBuilder request;

    NordeaFIApiClient apiClient;

    @Before
    public void init() {
        apiClient = new NordeaFIApiClient(httpClient, sessionStorage);
    }

    @Test
    public void shouldFetchLoanDetails() {
        when(httpClient.request(Mockito.any(URL.class))).thenReturn(request);
        when(request.header(any(), any())).thenReturn(request);
        when(request.get(FetchLoanDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                getLoanDetails(), FetchLoanDetailsResponse.class));

        FetchLoanDetailsResponse fetchLoanDetailsResponse = apiClient.fetchLoanDetails("dummyId");

        Assert.assertNotNull(fetchLoanDetailsResponse);
        LoanAccount loanAccount = fetchLoanDetailsResponse.toTinkLoanAccount();
        ExactCurrencyAmount initialBalance = loanAccount.getDetails().getInitialBalance();
        Assert.assertEquals(BigDecimal.valueOf(4000.0), initialBalance.getExactValue());
    }

    private String getLoanDetails() {
        return "{\n"
                + "  \"loan_id\": \"1111111111111\",\n"
                + "  \"loan_formatted_id\": \"111111-1111111\",\n"
                + "  \"product_code\": \"FI60002\",\n"
                + "  \"currency\": \"EUR\",\n"
                + "  \"group\": \"cash_loan\",\n"
                + "  \"repayment_status\": \"in_progress\",\n"
                + "  \"interest\": {\n"
                + "    \"rate\": 1.1000,\n"
                + "    \"reference_rate_type\": \"euribor_3\",\n"
                + "    \"interest_change_dates_history\": []\n"
                + "  },\n"
                + "  \"amount\": {\n"
                + "    \"drawn\": 4000.00,\n"
                + "    \"undrawn\": 0.00,\n"
                + "    \"paid\": 38.22,\n"
                + "    \"balance\": -3961.78\n"
                + "  },\n"
                + "  \"following_payment\": {\n"
                + "    \"instalment\": 0.00,\n"
                + "    \"interest\": 29.33,\n"
                + "    \"fees\": 0.00,\n"
                + "    \"account_management_fee\": 12.40,\n"
                + "    \"total\": 41.73,\n"
                + "    \"date\": \"2020-12-07\"\n"
                + "  },\n"
                + "  \"repayment_schedule\": {\n"
                + "    \"instalment_free_months\": [\n"
                + "      6,\n"
                + "      12\n"
                + "    ],\n"
                + "    \"period_between_instalments\": 1,\n"
                + "    \"period_between_interest_payments\": 1,\n"
                + "    \"method_of_payment\": \"equal_instalments\",\n"
                + "    \"following_instalment\": \"2020-12-07\",\n"
                + "    \"following_interest_payment\": \"2020-12-07\",\n"
                + "    \"repayment_amount\": 89.00\n"
                + "  },\n"
                + "  \"owners\": [\n"
                + "    {\n"
                + "      \"name\": \"NAME\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }
}
