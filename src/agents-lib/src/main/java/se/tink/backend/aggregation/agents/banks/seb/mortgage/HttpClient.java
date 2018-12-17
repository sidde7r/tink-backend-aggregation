package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;

public interface HttpClient {
    <T> T get(ApiRequest request, Class<T> responseModel);

    <T> T post(ApiRequest request, Class<T> responseModel);

    void attachHttpFilters(ClientFilterFactory filterFactory);
}
