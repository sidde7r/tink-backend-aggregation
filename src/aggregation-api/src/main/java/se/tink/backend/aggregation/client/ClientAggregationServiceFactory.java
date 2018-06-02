package se.tink.backend.aggregation.client;

import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.rpc.User;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientAggregationServiceFactory implements AggregationServiceFactory {

    private ServiceClassBuilder builder;

    /**
     * Helper constructor to make it more enjoyable to create a factory for a basic URL.
     * <p>
     * Not exposing constructor immediately to make it explicit that we are not making pinned calls.
     * 
     * @param url
     *            to point the factory to.
     */
    public static ClientAggregationServiceFactory buildWithoutPinning(String url) {
        return new ClientAggregationServiceFactory(url);
    }

    private ClientAggregationServiceFactory(String url) {
        this(new BasicWebServiceClassBuilder(
                InterContainerJerseyClientFactory.withoutPinning().build().resource(url)));
    }

    public ClientAggregationServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    @Override
    public AggregationService getAggregationService() {
        return builder.build(AggregationService.class);
    }

    @Override
    public CreditSafeService getCreditSafeService() {
        return builder.build(CreditSafeService.class);
    }

    @Override
    public AggregationService getAggregationService(User routeObject) {
        return builder.build(AggregationService.class, routeObject.getId());
    }
}
