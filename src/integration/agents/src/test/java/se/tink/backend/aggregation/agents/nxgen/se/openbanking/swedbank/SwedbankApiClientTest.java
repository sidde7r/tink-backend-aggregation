package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankAccessAccountCheckEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.SwedbankAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.storage.AgentTemporaryStorageProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient.UnleashClientProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SwedbankApiClientTest {

    private final List<String> IBANS = Collections.emptyList();
    private SwedbankApiClient underTest;
    private AgentComponentProvider componentProvider;
    private AgentConfiguration<SwedbankConfiguration> agentConfiguration;

    @Before
    public void setUp() throws CertificateException {

        componentProvider =
                new AgentComponentProvider(
                        mock(TinkHttpClientProvider.class),
                        mock(SupplementalInformationProvider.class),
                        mock(AgentContextProvider.class),
                        mock(GeneratedValueProvider.class),
                        mock(UnleashClientProvider.class),
                        mock(AgentTemporaryStorageProvider.class));
        agentConfiguration = mock(AgentConfiguration.class);
        when(agentConfiguration.getQsealc())
                .thenReturn(
                        "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");
        underTest =
                new SwedbankApiClient(
                        mock(TinkHttpClient.class),
                        mock(PersistentStorage.class),
                        agentConfiguration,
                        mock(QsealcSigner.class),
                        componentProvider,
                        mock(SwedbankMarketConfiguration.class));
    }

    @Test
    public void shouldReturnAllFieldsWithGivenIbans() {

        // given
        RefreshInformationRequest requestWithAllRefreshableItems =
                getRequestWithAllAccountAndTransactionRefreshableItems();
        // when
        when(componentProvider.getCredentialsRequest()).thenReturn(requestWithAllRefreshableItems);
        // then
        assertEquals(
                ObjectUtils.toString(new SwedbankAccessEntity().addIbans(IBANS)),
                ObjectUtils.toString(underTest.getAccessEntity(IBANS)));
    }

    @Test
    public void shouldReturnAccountFieldsWithGivenIbans() {

        // given
        RefreshInformationRequest requestWithAllRefreshableItems =
                getRequestWithAccountRefreshableItems();
        // when
        when(componentProvider.getCredentialsRequest()).thenReturn(requestWithAllRefreshableItems);
        // then
        assertEquals(
                ObjectUtils.toString(new SwedbankAccessAccountCheckEntity().addIbans(IBANS)),
                ObjectUtils.toString(underTest.getAccessEntity(IBANS)));
    }

    @Test
    public void shouldReturnAllRefreshableItemByDefaultAccountFieldsWithGivenIbans() {

        // given
        RefreshInformationRequest requestWithAllRefreshableItems =
                getRequestWithAccountWithNoRefreshableItems();
        // when
        when(componentProvider.getCredentialsRequest()).thenReturn(requestWithAllRefreshableItems);
        // then
        assertEquals(
                ObjectUtils.toString(new SwedbankAccessEntity().addIbans(IBANS)),
                ObjectUtils.toString(underTest.getAccessEntity(IBANS)));
    }

    private RefreshInformationRequest getRequestWithAllAccountAndTransactionRefreshableItems() {
        Set<RefreshableItem> allItems =
                Sets.newHashSet(RefreshableItem.allRefreshableItemsAsArray());
        RefreshInformationRequest refreshInformationRequest = new RefreshInformationRequest();
        allItems.add(RefreshableItem.IDENTITY_DATA);
        refreshInformationRequest.setItemsToRefresh(allItems);
        return refreshInformationRequest;
    }

    private RefreshInformationRequest getRequestWithAccountRefreshableItems() {
        Set<RefreshableItem> allItems = Sets.newHashSet(RefreshableItem.REFRESHABLE_ITEMS_ACCOUNTS);
        RefreshInformationRequest refreshInformationRequest = new RefreshInformationRequest();
        refreshInformationRequest.setItemsToRefresh(allItems);
        return refreshInformationRequest;
    }

    private RefreshInformationRequest getRequestWithAccountWithNoRefreshableItems() {
        Set<RefreshableItem> allItems = Collections.emptySet();
        RefreshInformationRequest refreshInformationRequest = new RefreshInformationRequest();
        refreshInformationRequest.setItemsToRefresh(allItems);
        return refreshInformationRequest;
    }
}
