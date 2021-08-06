package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RedsysGlobalConsentGeneratorTest {

    private RedsysGlobalConsentGenerator generator;
    private AgentComponentProvider componentProviderMock;
    private ManualAuthenticateRequest requestMock;
    private RefreshScope scope = new RefreshScope();
    private LocalDateTimeSource localDateTimeSource;

    @Before
    public void setUp() throws Exception {
        this.componentProviderMock = mock(AgentComponentProvider.class);
        this.requestMock = mock(ManualAuthenticateRequest.class);
        this.scope = new RefreshScope();
        this.requestMock.setRefreshScope(scope);
        this.localDateTimeSource = new ConstantLocalDateTimeSource();
        given(componentProviderMock.getLocalDateTimeSource()).willReturn(localDateTimeSource);
    }

    @Test
    public void shouldMapAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.SAVING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RedsysGlobalConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAvailableAccountsWithBalances()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(consentRequestBody.isRecurringIndicator()).isFalse();
        assertThat(consentRequestBody.getFrequencyPerDay()).isEqualTo(1);
        assertThat(consentRequestBody.isCombinedServiceIndicator()).isFalse();
    }

    @Test
    public void shouldMapAllAccountItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.LOAN_ACCOUNTS,
                        RefreshableItem.INVESTMENT_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RedsysGlobalConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAvailableAccountsWithBalances()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(consentRequestBody.isRecurringIndicator()).isFalse();
        assertThat(consentRequestBody.getFrequencyPerDay()).isEqualTo(1);
        assertThat(consentRequestBody.isCombinedServiceIndicator()).isFalse();
    }

    @Test
    public void shouldMapTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RedsysGlobalConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAllPsd2()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plusDays(90)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(consentRequestBody.isRecurringIndicator()).isTrue();
        assertThat(consentRequestBody.getFrequencyPerDay()).isEqualTo(4);
        assertThat(consentRequestBody.isCombinedServiceIndicator()).isFalse();
    }

    @Test
    public void shouldMapAllTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.INVESTMENT_TRANSACTIONS,
                        RefreshableItem.LOAN_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RedsysGlobalConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAllPsd2()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plusDays(90)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    public void shouldMapAccountAndTransactionsItemsCorrectly() {
        // given
        scope.setRefreshableItemsIn(
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CHECKING_TRANSACTIONS,
                        RefreshableItem.SAVING_TRANSACTIONS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator = RedsysGlobalConsentGenerator.of(componentProviderMock, getAllScopes());

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAllPsd2()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plusDays(90)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(consentRequestBody.isRecurringIndicator()).isTrue();
        assertThat(consentRequestBody.getFrequencyPerDay()).isEqualTo(4);
        assertThat(consentRequestBody.isCombinedServiceIndicator()).isFalse();
    }

    @Test
    public void shouldExtendScopeCorrectly() {
        // given
        scope.setRefreshableItemsIn(Sets.newHashSet(RefreshableItem.CHECKING_ACCOUNTS));
        given(requestMock.getRefreshScope()).willReturn(scope);
        given(componentProviderMock.getCredentialsRequest()).willReturn(requestMock);
        generator =
                RedsysGlobalConsentGenerator.of(
                        componentProviderMock, Sets.newHashSet(RedsysScope.ALL_PSD2));

        // when
        ConsentRequestBody consentRequestBody = generator.generate();

        // then
        assertThat(consentRequestBody.getAccess()).isNotNull();
        assertThat(consentRequestBody.getAccess().getAllPsd2()).isNotNull();
        assertThat(consentRequestBody.getValidUntil())
                .isEqualTo(
                        localDateTimeSource
                                .now()
                                .toLocalDate()
                                .plusDays(90)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(consentRequestBody.isRecurringIndicator()).isTrue();
        assertThat(consentRequestBody.getFrequencyPerDay()).isEqualTo(4);
        assertThat(consentRequestBody.isCombinedServiceIndicator()).isFalse();
    }

    public ImmutableSet<RedsysScope> getAllScopes() {
        Set<RedsysScope> set = new HashSet<>();
        set.add(RedsysScope.AVAILABLE_ACCOUNTS_WITH_BALANCES);
        set.add(RedsysScope.ALL_PSD2);

        return ImmutableSet.<RedsysScope>builder().addAll(set).build();
    }
}
