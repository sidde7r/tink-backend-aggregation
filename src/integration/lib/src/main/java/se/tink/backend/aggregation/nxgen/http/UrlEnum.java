package se.tink.backend.aggregation.nxgen.http;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UrlEnum {
    URL get();

    URL parameter(String key, String value);

    URL queryParam(String key, String value);
}
