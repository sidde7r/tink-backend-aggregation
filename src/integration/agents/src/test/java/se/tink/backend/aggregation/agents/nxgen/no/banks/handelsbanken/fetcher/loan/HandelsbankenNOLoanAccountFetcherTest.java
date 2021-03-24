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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenNOLoanAccountFetcherTest {

    private static final BigDecimal BALANCE = BigDecimal.valueOf(1.00);
    private static final double INTEREST_RATE = 0.0202;
    private static final BigDecimal INSTALMENT = BigDecimal.valueOf(11.11);
    private static final long INITIAL_BALANCE = 4234234;
    private static final String ID = "8923454567";
    private static final String ACCOUNT_NUMBER = "8923454567";
    private static final String ACCOUNT_DESCRIPTION = "Renteståttelån serie";
    private static final String MORTGAGE_ACCOUNT_DESCRIPTION = "Bolig eiendomskreditt ann";
    private static final String MORTGAGE_ACCOUNT_DESCRIPTION2 = "Fast 7 år annu ek";
    private static final String MORTGAGE_ACCOUNT_DESCRIPTION3 = "Fast 5år annu ek";
    private static final String MORTGAGE_ACCOUNT_DESCRIPTION4 = "Lån annuitet";
    private static final String REPAYMENT_PLAN = "repayment_plan";
    private static final String TYPE = "loan";
    private static final String NOK = "NOK";
    private static final String REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH =
            "loanmod/accounts/enc!!1wgjSvWweNFwenfuw@#$eerfnVPuSYsOx_LW-WLn7rfergergt33Pe9_v0LJEWNewfnuwehiS/repayment_plan";

    private HandelsbankenNOApiClient handelsbankenNOApiClient;
    private HandelsbankenNOLoanAccountFetcher handelsbankenNOLoanAccountFetcher;
    private ObjectMapper objectMapper;
    private List<LoanAccount> expected;
    private List<LoanAccount> result;

    @Before
    public void setup() throws IOException {
        handelsbankenNOApiClient = Mockito.mock(HandelsbankenNOApiClient.class);
        handelsbankenNOLoanAccountFetcher =
                new HandelsbankenNOLoanAccountFetcher(handelsbankenNOApiClient);
        objectMapper = new ObjectMapper();

        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(
                        getLoanFetchingResponse(
                                Collections.singletonList(
                                        getLoanAccountEntity(
                                                ACCOUNT_DESCRIPTION, getLinks(getLinkEntity())))));
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(getDetailsEntity()))));
    }

    @Test
    public void shouldFetchLoanAccounts() {
        // Given
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchMortgages() throws IOException {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(
                        getLoanFetchingResponse(
                                Arrays.asList(
                                        getLoanAccountEntity(
                                                ACCOUNT_DESCRIPTION, getLinks(getLinkEntity())),
                                        getLoanAccountEntity(
                                                MORTGAGE_ACCOUNT_DESCRIPTION,
                                                getLinks(getLinkEntity())),
                                        getLoanAccountEntity(
                                                MORTGAGE_ACCOUNT_DESCRIPTION2,
                                                getLinks(getLinkEntity())),
                                        getLoanAccountEntity(
                                                MORTGAGE_ACCOUNT_DESCRIPTION3,
                                                getLinks(getLinkEntity())),
                                        getLoanAccountEntity(
                                                MORTGAGE_ACCOUNT_DESCRIPTION4,
                                                getLinks(getLinkEntity())))));

        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(getDetailsEntity()))));
        expected =
                Arrays.asList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION),
                        getLoanAccount(
                                LoanDetails.Type.MORTGAGE,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                MORTGAGE_ACCOUNT_DESCRIPTION),
                        getLoanAccount(
                                LoanDetails.Type.MORTGAGE,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                MORTGAGE_ACCOUNT_DESCRIPTION2),
                        getLoanAccount(
                                LoanDetails.Type.MORTGAGE,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                MORTGAGE_ACCOUNT_DESCRIPTION3),
                        getLoanAccount(
                                LoanDetails.Type.MORTGAGE,
                                INTEREST_RATE,
                                INSTALMENT,
                                INITIAL_BALANCE,
                                MORTGAGE_ACCOUNT_DESCRIPTION4));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(result.size()).isEqualTo(expected.size());
        assertThat(result).hasSameElementsAs(expected);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    @Test
    public void shouldFetchLoanAccountsWhenRepaymentPlanPathIsMissing() {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(
                        getLoanFetchingResponse(
                                Collections.singletonList(
                                        getLoanAccountEntity(
                                                ACCOUNT_DESCRIPTION, getLinks(new LinkEntity())))));

        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                0,
                                BigDecimal.ZERO,
                                0,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenLinksAreNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoans())
                .thenReturn(
                        getLoanFetchingResponse(
                                Collections.singletonList(
                                        getLoanAccountEntity(ACCOUNT_DESCRIPTION, null))));

        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                0,
                                BigDecimal.ZERO,
                                0,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsListInLoanDetailsResponseIsNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(null));
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                BigDecimal.ZERO,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsListInLoanDetailsResponseIsEmpty() {
        // Given
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(Collections.emptyList()));
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                BigDecimal.ZERO,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenPaymentDetailsInLoanDetailsResponseIsNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(getLoanDetailsResponse(Collections.singletonList(null)));
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                BigDecimal.ZERO,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenDetailsEntityInPaymentDetailsIsNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(getPaymentDetailEntity(null))));
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                BigDecimal.ZERO,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    @Test
    public void shouldFetchLoanAccountsWhenInstalmentIsNull() {
        // Given
        when(handelsbankenNOApiClient.fetchLoanDetails(REPAYMENT_PLAN_FOR_LOAN_ACCOUNT_PATH))
                .thenReturn(
                        getLoanDetailsResponse(
                                Collections.singletonList(
                                        getPaymentDetailEntity(new DetailsEntity()))));
        expected =
                Collections.singletonList(
                        getLoanAccount(
                                LoanDetails.Type.DERIVE_FROM_NAME,
                                INTEREST_RATE,
                                BigDecimal.ZERO,
                                INITIAL_BALANCE,
                                ACCOUNT_DESCRIPTION));

        // When
        result = (List<LoanAccount>) handelsbankenNOLoanAccountFetcher.fetchAccounts();

        // Then
        assertThat(expected.get(0)).isEqualToComparingFieldByFieldRecursively(result.get(0));
    }

    private LoanAccount getLoanAccount(
            LoanDetails.Type type,
            double interestRate,
            BigDecimal instalment,
            long initialBalance,
            String description) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(type)
                                .withBalance(ExactCurrencyAmount.of(BALANCE, NOK))
                                .withInterestRate(interestRate)
                                .setMonthlyAmortization(ExactCurrencyAmount.of(instalment, NOK))
                                .setInitialBalance(ExactCurrencyAmount.inNOK(initialBalance))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ID)
                                .withAccountNumber(ACCOUNT_NUMBER)
                                .withAccountName(description)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.NO, ID))
                                .build())
                .build();
    }

    private LoanFetchingResponse getLoanFetchingResponse(List<LoanAccountEntity> loanAccounts) {
        LoanFetchingResponse response = new LoanFetchingResponse();
        response.setLoanAccountEntities(loanAccounts);
        return response;
    }

    private LoanAccountEntity getLoanAccountEntity(
            String description, Map<String, LinkEntity> links) {
        LoanAccountEntity loanAccountEntity = new LoanAccountEntity();
        loanAccountEntity.setId(ID);
        loanAccountEntity.setAccountDescription(description);
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
        detailsResponse.setNominalInterestRate(2.02);
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
