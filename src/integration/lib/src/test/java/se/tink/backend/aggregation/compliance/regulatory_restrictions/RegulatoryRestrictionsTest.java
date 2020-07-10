package se.tink.backend.aggregation.compliance.regulatory_restrictions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class RegulatoryRestrictionsTest {
    private static final Account ACCOUNT = new Account();
    private static final Provider PROVIDER = new Provider();
    private static final RegulatoryRestrictionsMetrics regulatoryRestrictionsMetrics =
            new RegulatoryRestrictionsMetrics(new MetricRegistry());
    private RegulatoryRestrictions regulatoryRestrictions;
    private CredentialsRequest credentialsRequest;

    @BeforeClass
    public static void init() {
        ACCOUNT.setType(AccountTypes.CHECKING);
    }

    @Before
    public void initTestCase() {
        regulatoryRestrictions = new RegulatoryRestrictions(regulatoryRestrictionsMetrics);
        credentialsRequest = mock(CredentialsRequest.class);
        when(credentialsRequest.getProvider()).thenReturn(PROVIDER);
    }

    @Test
    public void shouldRestrictPaymentAccount() {
        List<List<DataFetchingRestrictions>> restrictionSets =
                Arrays.asList(
                        getRestrictPaymentAccountsOnly(),
                        getRestrictPaymentAndUndeterminedAccounts());
        for (List<DataFetchingRestrictions> restrictionSet : restrictionSets) {
            when(credentialsRequest.getDataFetchingRestrictions()).thenReturn(restrictionSet);
            boolean shouldBeRestricted =
                    regulatoryRestrictions.shouldAccountBeRestricted(
                            credentialsRequest,
                            ACCOUNT,
                            Optional.of(Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT));
            assertThat(shouldBeRestricted).isTrue();
        }
    }

    @Test
    public void shouldRestrictUndeterminedPaymentAccount() {
        when(credentialsRequest.getDataFetchingRestrictions())
                .thenReturn(getRestrictPaymentAndUndeterminedAccounts());
        boolean shouldBeRestricted =
                regulatoryRestrictions.shouldAccountBeRestricted(
                        credentialsRequest,
                        ACCOUNT,
                        Optional.of(
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT));
        assertThat(shouldBeRestricted).isTrue();
    }

    @Test
    public void shouldNotRestrictUndeterminedPaymentAccount() {
        when(credentialsRequest.getDataFetchingRestrictions())
                .thenReturn(getRestrictPaymentAccountsOnly());
        boolean shouldBeRestricted =
                regulatoryRestrictions.shouldAccountBeRestricted(
                        credentialsRequest,
                        ACCOUNT,
                        Optional.of(
                                Psd2PaymentAccountClassificationResult
                                        .UNDETERMINED_PAYMENT_ACCOUNT));
        assertThat(shouldBeRestricted).isFalse();
    }

    @Test
    public void shouldNeverRestrictNonPaymentAccount() {
        List<List<DataFetchingRestrictions>> restrictionSets =
                Arrays.asList(
                        getNoRestrictions(),
                        getRestrictPaymentAccountsOnly(),
                        getRestrictPaymentAndUndeterminedAccounts());
        for (List<DataFetchingRestrictions> restrictionSet : restrictionSets) {
            when(credentialsRequest.getDataFetchingRestrictions()).thenReturn(restrictionSet);
            boolean shouldBeRestricted =
                    regulatoryRestrictions.shouldAccountBeRestricted(
                            credentialsRequest,
                            ACCOUNT,
                            Optional.of(
                                    Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT));
            assertThat(shouldBeRestricted).isFalse();
        }
    }

    @Test
    public void shouldNeverRestrictIfNoRestrictionsGiven() {
        when(credentialsRequest.getDataFetchingRestrictions()).thenReturn(getNoRestrictions());
        boolean shouldBeRestricted =
                regulatoryRestrictions.shouldAccountBeRestricted(
                        credentialsRequest,
                        ACCOUNT,
                        Optional.of(Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT));
        assertThat(shouldBeRestricted).isFalse();
    }

    @Test
    public void shouldNeverRestrictIfNoClassification() {
        List<List<DataFetchingRestrictions>> restrictionSets =
                Arrays.asList(
                        getNoRestrictions(),
                        getRestrictPaymentAccountsOnly(),
                        getRestrictPaymentAndUndeterminedAccounts());
        for (List<DataFetchingRestrictions> restrictionSet : restrictionSets) {
            when(credentialsRequest.getDataFetchingRestrictions()).thenReturn(restrictionSet);
            boolean shouldBeRestricted =
                    regulatoryRestrictions.shouldAccountBeRestricted(
                            credentialsRequest, ACCOUNT, Optional.empty());
            assertThat(shouldBeRestricted).isFalse();
        }
    }

    private List<DataFetchingRestrictions> getRestrictPaymentAndUndeterminedAccounts() {
        return Arrays.asList(
                DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS,
                DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_UNDETERMINED_PAYMENT_ACCOUNTS);
    }

    private List<DataFetchingRestrictions> getRestrictPaymentAccountsOnly() {
        return Collections.singletonList(
                DataFetchingRestrictions.RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS);
    }

    private List<DataFetchingRestrictions> getNoRestrictions() {
        return Collections.emptyList();
    }
}
