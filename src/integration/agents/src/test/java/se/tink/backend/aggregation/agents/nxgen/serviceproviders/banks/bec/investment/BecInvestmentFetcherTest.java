package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.DepositDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecInvestmentFetcherTest {

    private BecApiClient apiClient;
    private BecInvestmentFetcher investmentFetcher;

    @Before
    public void setup() {
        apiClient = mock(BecApiClient.class);
        investmentFetcher = new BecInvestmentFetcher(apiClient);
    }

    @Test
    public void shouldSkipFillingInstrumentDetailsWhenBankFailsToReturnThem() {
        when(apiClient.fetchInvestment()).thenReturn(getFetchInvestmentResponse());
        when(apiClient.fetchDepositDetail(anyString())).thenReturn(getDepositDetailsResponse(1, 1));
        when(apiClient.fetchInstrumentDetails(anyString(), anyString()))
                .thenThrow(new HttpResponseException("", null, null));

        Collection<InvestmentAccount> investmentAccounts = investmentFetcher.fetchAccounts();

        int EXPECTED_NUM_OF_ACCOUNTS = 1;
        int EXPECTED_NUM_OF_PORTFOLIOS = 1;
        int EXPECTED_NUM_OF_INSTRUMENTS = 1;

        assertEquals(EXPECTED_NUM_OF_ACCOUNTS, investmentAccounts.size());
        InvestmentAccount investmentAccount = investmentAccounts.iterator().next();

        assertEquals(EXPECTED_NUM_OF_PORTFOLIOS, investmentAccount.getSystemPortfolios().size());
        Portfolio portfolio = investmentAccount.getSystemPortfolios().get(0);
        assertEquals(EXPECTED_NUM_OF_INSTRUMENTS, portfolio.getInstruments().size());
        Instrument instrument = portfolio.getInstruments().get(0);
        assertNull(instrument.getIsin());
        assertNull(instrument.getMarketPlace());
    }

    @Test
    public void shouldReturnOneInvestmentAccountWith15Instruments() {
        when(apiClient.fetchInvestment()).thenReturn(getFetchInvestmentResponse());
        when(apiClient.fetchDepositDetail(anyString())).thenReturn(getDepositDetailsResponse(3, 5));
        when(apiClient.fetchInstrumentDetails(anyString(), anyString()))
                .thenReturn(getInstrumentDetailsEntity());

        Collection<InvestmentAccount> investmentAccounts = investmentFetcher.fetchAccounts();

        int EXPECTED_NUM_OF_ACCOUNTS = 1;
        int EXPECTED_NUM_OF_PORTFOLIOS = 1;
        int EXPECTED_NUM_OF_INSTRUMENTS = 15;

        assertEquals(EXPECTED_NUM_OF_ACCOUNTS, investmentAccounts.size());
        InvestmentAccount investmentAccount = investmentAccounts.iterator().next();
        assertEquals(EXPECTED_NUM_OF_PORTFOLIOS, investmentAccount.getSystemPortfolios().size());
        Portfolio portfolio = investmentAccount.getSystemPortfolios().get(0);
        assertEquals(EXPECTED_NUM_OF_INSTRUMENTS, portfolio.getInstruments().size());
    }

    private FetchInvestmentResponse getFetchInvestmentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"depositAccounts\": [\n"
                        + "        {\n"
                        + "            \"accountNo\": \"123456\",\n"
                        + "            \"amount\": 1234.0,\n"
                        + "            \"amountTxt\": \"1234,00\",\n"
                        + "            \"depositAccount\": \"12340998\",\n"
                        + "            \"depositName\": \"First Deposit\",\n"
                        + "            \"id\": \"123852147\",\n"
                        + "            \"marketValue\": 1234.0,\n"
                        + "            \"marketValueTxt\": \"1234,00\",\n"
                        + "            \"name\": \"First Deposit\",\n"
                        + "            \"urlDetail\": \"/mobilbank/depot/dispositionsoversigt?d=zxcv&v=asdf\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"stockorders\": []\n"
                        + "}",
                FetchInvestmentResponse.class);
    }

    private DepositDetailsResponse getDepositDetailsResponse(
            int numberOfPortfolios, int numberOfInstruments) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "{\"depotid\": \"2001-02-03-12.11.22.123456\", \"maxRefreshTime\": 900, \"papers\": [");
        String portfolioString = generatePortfolioString(numberOfInstruments);
        for (int i = 0; i < numberOfPortfolios; i++) {
            sb.append(portfolioString);
            if (i + 1 != numberOfPortfolios) {
                sb.append(",");
            }
        }
        sb.append("], \"refreshRate\": 10, \"warningRate\": 20 }");
        return SerializationUtils.deserializeFromString(
                sb.toString(), DepositDetailsResponse.class);
    }

    private String generatePortfolioString(int numberOfInstruments) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"dataType\": \"1\", \"paperType\": \"Securities\", \"papers\": [");

        for (int i = 0; i < numberOfInstruments; i++) {
            sb.append(
                    "{\n"
                            + "    \"amount\": 100000.0,\n"
                            + "    \"amountTxt\": \"100.000,00\",\n"
                            + "    \"currency\": \"DKK\",\n"
                            + "    \"dataType\": \"1\",\n"
                            + "    \"id\": \"ASD    123456724  1234567890999999N\",\n"
                            + "    \"noOfPapers\": 100000.0,\n"
                            + "    \"noOfPapersTxt\": \"100.000,00\",\n"
                            + "    \"paperHash\": 1234567890987654321,\n"
                            + "    \"paperName\": \"Super paper\",\n"
                            + "    \"rate\": 1.0,\n"
                            + "    \"rateTxt\": \"1,0000\",\n"
                            + "    \"urlDetail\": \"/mobilbank/depot/papirinfo?paperId=ASD++++1234567245++1234567890999999N&getGraphData=true&getBoersAbb=true\"\n"
                            + "}");
            if (i + 1 != numberOfInstruments) {
                sb.append(",");
            }
        }
        sb.append("], \"typeAmount\": 1234.56, \"typeAmountTxt\": \"1.234,560\"}");

        return sb.toString();
    }

    private InstrumentDetailsEntity getInstrumentDetailsEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"isinCode\": \"DK12356\",\n"
                        + "    \"market\": \"asdzxc A/S\"\n"
                        + "}",
                InstrumentDetailsEntity.class);
    }
}
