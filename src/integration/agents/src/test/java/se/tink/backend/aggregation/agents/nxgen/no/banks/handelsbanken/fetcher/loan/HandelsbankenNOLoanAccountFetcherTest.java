package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.PaymentDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc.LoanFetchingResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenNOLoanAccountFetcherTest {

    private static final BigDecimal BALANCE = BigDecimal.valueOf(1.00);
    private static final double INTEREST_RATE = 2.02;
    private static final BigDecimal INSTALMENT = BigDecimal.valueOf(11.11);
    private static final long INITIAL_BALANCE = 4234234;
    private static final String ID = "8923454567";
    private static final String ACCOUNT_NUMBER = "8923454567";
    private static final String ACCOUNT_DESCRIPTION = "Lån annuitet";
    private static final String REPAYMENT_PLAN = "repayment_plan";
    private static final String TYPE = "loan";
    private static final String CAPABILITIES = "capabilities";
    private static final String DETAILS = "details";
    private static final String ID_MODULE = "idModule";
    private static final String NOK = "NOK";
    private static final String REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH =
            "loanmod/accounts/enc!!1wgjSvWweNFwenfuw@#$eerfnVPuSYsOx_LW-WLn7rfergergt33Pe9_v0LJEWNewfnuwehiS/repayment_plan";

    private HandelsbankenNOApiClient handelsbankenNOApiClient;
    private HandelsbankenNOLoanAccountFetcher handelsbankenNOLoanAccountFetcher;
    private ObjectMapper objectMapper;
    private List<LoanAccount> expected;
    private List<LoanAccount> result;

    @Before
    public void setup() {
        handelsbankenNOApiClient = Mockito.mock(HandelsbankenNOApiClient.class);
        handelsbankenNOLoanAccountFetcher =
                new HandelsbankenNOLoanAccountFetcher(handelsbankenNOApiClient);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldFetchLoanAccounts() throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(getDetailsEntity()))));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, INSTALMENT, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenRepaymentPlanPathIsMissing() {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(new LinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(getDetailsEntity()))));
        expected = Collections.singletonList(getLoanAccount(0, BigDecimal.ZERO, 0));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenLinksAreNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoans()).thenReturn(getLoanFetchingResponse(null));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(getDetailsEntity()))));
        expected = Collections.singletonList(getLoanAccount(0, BigDecimal.ZERO, 0));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsListInLoanDetailsResponseIsNull()
            throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(null));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, BigDecimal.ZERO, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsListInLoanDetailsResponseIsEmpty()
            throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(Collections.emptyList()));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, BigDecimal.ZERO, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsInLoanDetailsResponseIsNull()
            throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(Collections.singletonList(null)));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, BigDecimal.ZERO, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenDetailsEntityInPaymentDetailsIsNull()
            throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(getPaymentDetailEntity(null))));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, BigDecimal.ZERO, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    @Test
    public void shouldFetchLoanAccountsWhenInstalmentIsNull() throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(getLoanFetchingResponse(getLinks(getLinkEntity())));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(new DetailsEntity()))));
        expected =
                Collections.singletonList(
                        getLoanAccount(INTEREST_RATE, BigDecimal.ZERO, INITIAL_BALANCE));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0))
                .isEqualToIgnoringGivenFields(result.get(0), DETAILS, ID_MODULE, CAPABILITIES);
        assertThat(result.get(0).getDetails())
                .isEqualToComparingFieldByField(expected.get(0).getDetails());
        assertThat(result.get(0).getIdModule())
                .isEqualToComparingFieldByField(expected.get(0).getIdModule());
    }

    private LoanAccount getLoanAccount(
            double interestRate, BigDecimal instalment, long initialBalance) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.DERIVE_FROM_NAME)
                                .withBalance(ExactCurrencyAmount.of(BALANCE, NOK))
                                .withInterestRate(interestRate)
                                .setMonthlyAmortization(ExactCurrencyAmount.of(instalment, NOK))
                                .setInitialBalance(ExactCurrencyAmount.inNOK(initialBalance))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ID)
                                .withAccountNumber(ACCOUNT_NUMBER)
                                .withAccountName(ACCOUNT_DESCRIPTION)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.NO, ID))
                                .build())
                .build();
    }

    private LoanFetchingResponse getLoanFetchingResponse(Map<String, LinkEntity> links) {
        LoanFetchingResponse response = new LoanFetchingResponse();
        response.setLoanAccountEntities(Collections.singletonList(getLoanAccountEntity(links)));
        return response;
    }

    private LoanAccountEntity getLoanAccountEntity(Map<String, LinkEntity> links) {
        LoanAccountEntity loanAccountEntity = new LoanAccountEntity();
        loanAccountEntity.setId(ID);
        loanAccountEntity.setAccountDescription(ACCOUNT_DESCRIPTION);
        loanAccountEntity.setAccountNumber(ACCOUNT_NUMBER);
        loanAccountEntity.setBalance(BALANCE);
        loanAccountEntity.setType(TYPE);
        loanAccountEntity.setLinks(links);
        return loanAccountEntity;
    }

    private Map<String, LinkEntity> getLinks(LinkEntity linkEntity) {
        return new HashMap<>(Maps.newHashMap(REPAYMENT_PLAN, linkEntity));
    }

    private LinkEntity getLinkEntity() throws IOException {
        return objectMapper.readValue(
                "{\n"
                        + "     \"href\": \"loanmod/accounts/enc!!1wgjSvWweNFwenfuw@#$eerfnVPuSYsOx_LW-WLn7rfergergt33Pe9_v0LJEWNewfnuwehiS/repayment_plan\",\n"
                        + "     \"verbs\": [\n"
                        + "         \"GET\"\n"
                        + "      ]\n"
                        + " }",
                LinkEntity.class);
    }

    private LoanDetailsResponse getLoanDetailsResponse(List<PaymentDetailEntity> paymentDetails) {
        LoanDetailsResponse detailsResponse = new LoanDetailsResponse();
        detailsResponse.setId(ID);
        detailsResponse.setOriginalLoanAmount(
                HandelsbankenNOLoanAccountFetcherTest.INITIAL_BALANCE);
        detailsResponse.setNominalInterestRate(HandelsbankenNOLoanAccountFetcherTest.INTEREST_RATE);
        detailsResponse.setPaymentDetail(paymentDetails);
        return detailsResponse;
    }

    private DetailsEntity getDetailsEntity() {
        DetailsEntity detailsEntity = new DetailsEntity();
        detailsEntity.setInstalment(HandelsbankenNOLoanAccountFetcherTest.INSTALMENT);
        return detailsEntity;
    }

    private PaymentDetailEntity getPaymentDetailEntity(DetailsEntity detailsEntity) {
        PaymentDetailEntity paymentDetailEntity = new PaymentDetailEntity();
        paymentDetailEntity.setDetails(Arrays.asList(detailsEntity, detailsEntity));
        return paymentDetailEntity;
    }
}
