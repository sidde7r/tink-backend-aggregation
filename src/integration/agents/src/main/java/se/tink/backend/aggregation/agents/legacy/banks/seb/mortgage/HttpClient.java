package se.tink.backend.aggregation.agents.legacy.banks.seb.mortgage;

import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;

public interface HttpClient {
    <T> T get(ApiRequest request, Class<T> responseModel);

    <T> T post(ApiRequest request, Class<T> responseModel);

    void attachHttpFilters(ClientFilterFactory filterFactory);
}
