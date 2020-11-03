package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankSELoanFetcherTest {

    private SwedbankSEApiClient apiClient;
    private SwedbankSELoanFetcher fetcher;
    private EngagementOverviewResponse engagementOverviewResponse;

    @Before
    public void before() {
        apiClient = Mockito.mock(SwedbankSEApiClient.class);
        fetcher = new SwedbankSELoanFetcher(apiClient);
        engagementOverviewResponse =
                SerializationUtils.deserializeFromString(
                        SwedBankSELoansFetcherTestData.ENGAGEMENT_OVERVIEW,
                        EngagementOverviewResponse.class);
    }

    @Test
    public void fetchLoans_parseMembershipLoan() {

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
        fetcher.fetchLoans(loanAccounts, loanOverviewResponse, engagementOverviewResponse);

        List<LoanAccount> membershipLoans =
                loanAccounts.stream()
                        .filter(l -> LoanDetails.Type.MEMBERSHIP.equals(l.getDetails().getType()))
                        .collect(Collectors.toList());

        assertThat(membershipLoans).hasSize(1);
        LoanAccount membershipLoan = membershipLoans.get(0);

        assertThat(membershipLoan.getExactBalance().getDoubleValue()).isEqualTo(-88888.0d);
        assertThat(membershipLoan.getExactBalance().getCurrencyCode()).isEqualTo("SEK");
        assertThat(membershipLoan.getAccountNumber()).isEqualTo("111 111 111-1");
        assertThat(membershipLoan.getDetails().getApplicants()).hasSize(2);
    }

    @Test
    public void fetchLoans_parseMortgage() {
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
        fetcher.fetchLoans(loanAccounts, loanOverviewResponse, engagementOverviewResponse);

        List<LoanAccount> mortageList =
                loanAccounts.stream()
                        .filter(l -> LoanDetails.Type.MORTGAGE.equals(l.getDetails().getType()))
                        .collect(Collectors.toList());

        assertThat(mortageList).hasSize(5);
        LoanAccount mortgage =
                mortageList.stream()
                        .filter(l -> !l.getDetails().getApplicants().isEmpty())
                        .findFirst()
                        .get();

        assertThat(mortgage.getExactBalance().getDoubleValue()).isEqualTo(-333000.0d);
        assertThat(mortgage.getExactBalance().getCurrencyCode()).isEqualTo("SEK");
        assertThat(mortgage.getAccountNumber()).isEqualTo("555 555 555-2");
        assertThat(mortgage.getDetails().getSecurity().contains("QUITE")).isTrue();
        assertThat(mortgage.getDetails().getApplicants()).hasSize(1);
        assertThat(mortgage.getDetails().hasCoApplicant()).isFalse();
    }
}
