package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import org.assertj.core.api.Assertions;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class TestAsserts {

    public static void assertHttpRequestsEquals(HttpRequest actual, HttpRequest expected) {
        Assertions.assertThat(actual.getBody()).isEqualTo(expected.getBody());
        Assertions.assertThat(actual.getUrl()).isEqualTo(expected.getUrl());
        Assertions.assertThat(actual.getMethod()).isEqualTo(expected.getMethod());
        Assertions.assertThat(actual.getHeaders()).isEqualTo(expected.getHeaders());
    }
}
