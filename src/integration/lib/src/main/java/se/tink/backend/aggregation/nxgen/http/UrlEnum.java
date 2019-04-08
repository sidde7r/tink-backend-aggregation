package se.tink.backend.aggregation.nxgen.http;

public interface UrlEnum {
    URL get();

    URL parameter(String key, String value);

    URL queryParam(String key, String value);
}
