package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankSELoanFetcherTest {

    private SwedbankSEApiClient apiClient;
    private SwedbankSELoanFetcher fetcher;

    @Before
    public void before() {
        apiClient = Mockito.mock(SwedbankSEApiClient.class);
        fetcher = new SwedbankSELoanFetcher(apiClient);
    }

    @Test
    public void fetchLoans_parseMembershipLoan() {
        Mockito.when(apiClient.engagementOverview())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedBankSELoansFetcherTestData.ENGAGEMENT_OVERVIEW,
                                EngagementOverviewResponse.class));

        LoanOverviewResponse loanOverviewResponse =
                SerializationUtils.deserializeFromString(
                        SwedBankSELoansFetcherTestData.OVERVIEW_OF_LOANS,
                        LoanOverviewResponse.class);

        Mockito.when(
                        apiClient.loadDetailsEntity(
                                argThat(
                                        a ->
                                                Optional.ofNullable(a.getUri())
                                                        .orElse("")
                                                        .contains("MEMBERSHIPLOAN"))))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedBankSELoansFetcherTestData.MAMBERSHIP_CONSUMTION_LOAN_DETAILS,
                                DetailedLoanResponse.class));

        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();
        fetcher.fetchLoans(loanAccounts, loanOverviewResponse);

        List<LoanAccount> membershipLoans =
                loanAccounts.stream()
                        .filter(l -> LoanDetails.Type.MEMBERSHIP.equals(l.getDetails().getType()))
                        .collect(Collectors.toList());

        assertEquals(1, membershipLoans.size());
        LoanAccount membershipLoan = membershipLoans.get(0);

        assertEquals(Double.valueOf(-88888.0d), membershipLoan.getBalance().getValue());
        assertEquals("SEK", membershipLoan.getBalance().getCurrency());

        assertEquals("111 111 111-1", membershipLoan.getAccountNumber());

        assertEquals(2, membershipLoan.getDetails().getApplicants().size());
    }

    @Test
    public void fetchLoans_parseMortgage() {
        Mockito.when(apiClient.engagementOverview())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedBankSELoansFetcherTestData.ENGAGEMENT_OVERVIEW,
                                EngagementOverviewResponse.class));

        LoanOverviewResponse loanOverviewResponse =
                SerializationUtils.deserializeFromString(
                        SwedBankSELoansFetcherTestData.OVERVIEW_OF_LOANS,
                        LoanOverviewResponse.class);

        Mockito.when(
                        apiClient.loadDetailsEntity(
                                argThat(
                                        a ->
                                                Optional.ofNullable(a.getUri())
                                                        .orElse("")
                                                        .contains("MORTGAGE"))))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                SwedBankSELoansFetcherTestData.MOTGAGE_DETAILS,
                                DetailedLoanResponse.class));

        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();
        fetcher.fetchLoans(loanAccounts, loanOverviewResponse);

        List<LoanAccount> mortageList =
                loanAccounts.stream()
                        .filter(l -> LoanDetails.Type.MORTGAGE.equals(l.getDetails().getType()))
                        .collect(Collectors.toList());

        assertEquals(5, mortageList.size());
        LoanAccount mortgage =
                mortageList.stream()
                        .filter(l -> !l.getDetails().getApplicants().isEmpty())
                        .findFirst()
                        .get();

        assertEquals(Double.valueOf(-333000.0d), mortgage.getBalance().getValue());
        assertEquals("SEK", mortgage.getBalance().getCurrency());

        assertEquals("555 555 555-2", mortgage.getAccountNumber());
        assertTrue(mortgage.getDetails().getSecurity().contains("QUITE"));

        assertEquals(1, mortgage.getDetails().getApplicants().size());
        assertFalse(mortgage.getDetails().hasCoApplicant());
    }
}
