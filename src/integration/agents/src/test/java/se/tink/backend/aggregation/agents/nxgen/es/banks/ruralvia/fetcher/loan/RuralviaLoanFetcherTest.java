package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public class RuralviaLoanFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";
    private static String globalPositionHtml;
    private static String loansSelectAccountForDetails;
    private static String loanDetailedConditions;
    private static String loanSelectAmortizationDetails;
    private static String loanAmortizationTable;
    private RuralviaApiClient apiClient;
    private RuralviaLoanFetcher loanFetcher;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        globalPositionHtml =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));
        loansSelectAccountForDetails =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "loansSelectAccountForDetails.html")));
        loanDetailedConditions =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "loanDetailedConditions.html")));
        loanSelectAmortizationDetails =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "loanSelectAmortizationDetails.html")));
        loanAmortizationTable =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "loanAmortizationTable.html")));
    }

    @Before
    public void setUp() throws Exception {
        apiClient = mock(RuralviaApiClient.class);
        loanFetcher = new RuralviaLoanFetcher(apiClient);
        when(apiClient.getGlobalPositionHtml()).thenReturn(globalPositionHtml);
        when(apiClient.navigateThroughLoan(any()))
                .thenReturn(
                        loansSelectAccountForDetails,
                        loanSelectAmortizationDetails,
                        loansSelectAccountForDetails,
                        loanSelectAmortizationDetails);
        when(apiClient.navigateToLoanDetails(any(), any())).thenReturn(loanDetailedConditions);
        when(apiClient.navigateToLoanAmortizationTableDetails(any(), any()))
                .thenReturn(loanAmortizationTable);
    }

    @Test
    public void fetchAccountsShouldFetchAndReturnTinkModel() {
        // given

        // when
        Collection<LoanAccount> loansFetched = loanFetcher.fetchAccounts();
        LoanAccount loan = loansFetched.stream().findFirst().get();

        // then
        Assert.assertEquals(2, loansFetched.size());
        Assert.assertEquals("00812371532699476527", loan.getAccountNumber());
        Assert.assertEquals(1, loan.getDetails().getApplicants().size());
        Assert.assertEquals((Double) 6.25, loan.getInterestRate());
        Assert.assertEquals("PTMO. CONSUMO PERSONAL", loan.getName());
        Assert.assertEquals("JOHN TINKER", loan.getDetails().getApplicants().get(0));
        Assert.assertEquals(Type.CREDIT, loan.getDetails().getType());
    }

    @Test
    public void fetchAccountsShouldReturnAVoidListWhenHasNoLoans() throws IOException {
        // given
        String globalPositionNoLoans =
                new String(
                        Files.readAllBytes(
                                Paths.get(
                                        TEST_DATA_PATH, "globalPositionWithNoCardsAndLoans.html")));
        when(apiClient.getGlobalPositionHtml()).thenReturn(globalPositionNoLoans);

        // when
        Collection<LoanAccount> loansFetched = loanFetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, loansFetched.size());
    }

    @Test
    public void fetchLoanAccountsShouldfetchWhenExistsLoans() {
        // given

        // when
        List<LoanEntity> loanEntitiesFetched = loanFetcher.fetchLoanAccounts();

        // then
        Assert.assertEquals(2, loanEntitiesFetched.size());
        Assert.assertEquals("00812371532699476527", loanEntitiesFetched.get(0).getAccountNumber());
        Assert.assertEquals("20388314881970780375", loanEntitiesFetched.get(1).getAccountNumber());
        Assert.assertEquals("332,45", loanEntitiesFetched.get(0).getMonthlyAmortization());
    }
}
