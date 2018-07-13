package se.tink.backend.aggregation.agents.banks.handelsbanken.v6;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CardListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CardTransactionListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LinkEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.utils.jersey.EntityIdentifier;
import se.tink.backend.aggregation.agents.utils.jersey.EntityResponse;
import se.tink.backend.aggregation.agents.utils.jersey.ResponseCacheLoader;
import se.tink.backend.aggregation.agents.utils.jersey.WebResourceBuilderFactory;

public class HandelsbankenAPIAgent implements WebResourceBuilderFactory {

    private final Client client;
    private final String defaultUserAgent;

    private final LoadingCache<EntityIdentifier, EntityResponse> responseCache;

    public HandelsbankenAPIAgent(Client client, String defaultUserAgent) {
        this.client = client;
        this.defaultUserAgent = defaultUserAgent;

        // Cache only lives for one agent and agent is torn down and rebuild between every refresh anyway
        // Expirary is therefore just hängslen and liverem
        this.responseCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new ResponseCacheLoader(this));
    }

    @Override
    public WebResource.Builder createWebResourceBuilder(String url) throws Exception {
        return createClientRequest(url);
    }

    WebResource.Builder createClientRequest(String url) {
        WebResource.Builder clientRequest = client.resource(url).accept(MediaType.APPLICATION_JSON);

        clientRequest = clientRequest.header("User-Agent", defaultUserAgent);

        clientRequest = clientRequest.header("X-SHB-DEVICE-NAME", "iOS;Tink;Tink"); // iOS;Apple;iPhone9.3
        clientRequest = clientRequest.header("X-SHB-DEVICE-MODEL", "IOS-11.1.2,7.8.2,iPhone9.3,SEPRIV");
        clientRequest = clientRequest.header("X-SHB-DEVICE-CLASS", "APP");
        clientRequest = clientRequest.header("X-SHB-APP-VERSION", "3.3:8.4");
        return clientRequest;
    }

    public AccountListResponse fetchAccountListResponse(LinkEntity link) throws Exception {
        EntityResponse response = responseCache.get(EntityIdentifier.create(link.getHref(), AccountListResponse.class));
        return response.getEntity(AccountListResponse.class);
    }

    public CardListResponse fetchCardListResponse(LinkEntity link) throws Exception {
        EntityResponse response = responseCache.get(EntityIdentifier.create(link.getHref(), CardListResponse.class));
        return response.getEntity(CardListResponse.class);
    }

    public TransactionListResponse fetchTransactionListResponse(LinkEntity link) throws Exception {
        EntityResponse response = responseCache.get(EntityIdentifier.create(link.getHref(), TransactionListResponse.class));
        return response.getEntity(TransactionListResponse.class);
    }

    public CardTransactionListResponse fetchCardTransactionListResponse(LinkEntity link) throws Exception {
        EntityResponse response = responseCache.get(EntityIdentifier.create(link.getHref(), CardTransactionListResponse.class));
        return response.getEntity(CardTransactionListResponse.class);
    }

    public void invalidateResponseCache() {
        responseCache.invalidateAll();
    }
}
