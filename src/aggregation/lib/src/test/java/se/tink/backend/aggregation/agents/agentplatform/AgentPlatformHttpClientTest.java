package se.tink.backend.aggregation.agents.agentplatform;

import static org.assertj.core.api.Assertions.assertThat;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import agents_platform_agents_framework.org.springframework.util.LinkedMultiValueMap;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class AgentPlatformHttpClientTest extends WireMockIntegrationTest {

    private static final String PATH = "/test-path";

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class SamplePayload {
        String a;
        int b;
    }

    @Test
    public void shoudExchange() throws URISyntaxException {
        // given
        URI uri = new URI(getOrigin() + PATH);
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(PATH))
                        .withHeader("header", new EqualToPattern("headerValue"))
                        .withRequestBody(new EqualToPattern("{\"a\":\"bbb\",\"b\":6}"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody("{\"a\":\"aaa\",\"b\":\"3\"}")
                                        .withHeader("h1", "v1")
                                        .withHeader("Content-Type", "application/json")));

        AgentPlatformHttpClient adapter = new AgentPlatformHttpClient(httpClient);
        LinkedMultiValueMap headers = new LinkedMultiValueMap<String, String>();
        headers.add("header", "headerValue");
        headers.add("Content-Type", "application/json");
        RequestEntity request =
                new RequestEntity<>(new SamplePayload("bbb", 6), headers, HttpMethod.POST, uri);

        // when
        ResponseEntity response = adapter.exchange(request, SamplePayload.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders()).containsKey("Content-Type").containsKey("h1");
        assertThat(response.getHeaders().get("Content-Type")).contains("application/json");
        assertThat(response.getHeaders().get("h1")).contains("v1");

        assertThat(response.getBody()).isNotNull().extracting("A", "B").containsExactly("aaa", 3);
    }
}
