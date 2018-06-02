package se.tink.backend.categorization.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.WebResource;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.categorization.rpc.CategorizationLabel;
import se.tink.backend.categorization.rpc.CategorizationResult;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static junit.framework.TestCase.assertEquals;

public class CategorizationServiceTest {
    private static final int WIREMOCK_SERVER_PORT = 33445;
    private static final String WIREMOCK_URL = "http://localhost:" + WIREMOCK_SERVER_PORT;
    private CategorizationService categorizationService;

    @Rule
    public WireMockRule wm = new WireMockRule(WIREMOCK_SERVER_PORT);

    @Before
    public void setUp() throws Exception {
        WebResource jerseyResource = InterContainerJerseyClientFactory.withoutPinning().build()
                .resource(WIREMOCK_URL);
        BasicWebServiceClassBuilder serviceClassBuilder = new BasicWebServiceClassBuilder(jerseyResource);
        categorizationService = serviceClassBuilder.build(CategorizationService.class);
    }

    @Test
    public void emptyResponse() throws Exception {
        wm.stubFor(get(urlMatching("/categorize/se.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"labels\":[]}")));

        CategorizationResult response = categorizationService.category("se", "");

        assertEquals(response.getLabels(), Collections.emptyList());
    }

    @Test
    public void responseMapping() throws Exception {
        wm.stubFor(get(urlMatching("/categorize/se.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"labels\":["
                                + "{\"label\":\"expenses:food.restaurants\",\"percentage\":0.8},"
                                + "{\"label\":\"expenses:food.coffee\",\"percentage\":0.1},"
                                + "{\"label\":\"expenses:food.other\",\"percentage\":0.05}"
                                + "]}")));

        CategorizationResult response = categorizationService.category("se", "McCafe");
        ImmutableList<CategorizationLabel> labels = ImmutableList.copyOf(response.getLabels());

        ImmutableList<CategorizationLabel> expectedLabels = ImmutableList.of(
                new CategorizationLabel("expenses:food.restaurants", 0.8),
                new CategorizationLabel("expenses:food.coffee", 0.1),
                new CategorizationLabel("expenses:food.other", 0.05)
        );

        assertEquals(expectedLabels, labels);
    }

}
