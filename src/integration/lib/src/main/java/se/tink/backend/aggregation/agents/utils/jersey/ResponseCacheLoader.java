package se.tink.backend.aggregation.agents.utils.jersey;

import com.google.common.cache.CacheLoader;
import com.sun.jersey.api.client.ClientResponse;

/**
 * User of this CacheLoader need to implement error handling if needed. Jersey's ClientResponse is
 * sent together with the deserialized entity in the EntityResponse object in order for callers to
 * verify http status
 *
 * <p>The load method will throw an ExecutionException with the real exception in the cause
 * parameter. More information: https://github.com/google/guava/wiki/CachesExplained#population
 */
public class ResponseCacheLoader extends CacheLoader<EntityIdentifier, EntityResponse> {

    private final WebResourceBuilderFactory factory;

    public ResponseCacheLoader(WebResourceBuilderFactory factory) {
        this.factory = factory;
    }

    @Override
    public EntityResponse load(EntityIdentifier entityIdentifier) throws Exception {
        ClientResponse response = get(entityIdentifier.url);
        Object entity = response.getEntity(entityIdentifier.entityClass);
        return new EntityResponse(response, entity);
    }

    private ClientResponse get(String url) throws Exception {
        return this.factory.createWebResourceBuilder(url).get(ClientResponse.class);
    }
}
