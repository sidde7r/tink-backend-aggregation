package se.tink.backend.aggregation.nxgen.http.filter.filterable.request;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface RequestBuilderProvidable {

    RequestBuilder request(String url);

    RequestBuilder request(URL url);
}
