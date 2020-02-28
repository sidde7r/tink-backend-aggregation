package se.tink.backend.aggregation.aggregationcontroller;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.libraries.credentials.rpc.Credentials;

public final class AggregationControllerAggregationClientTest
        extends AgentIntegrationMockServerTest {

    private static class TestModule extends AbstractModule {

        @Provider
        @Consumes({MediaType.APPLICATION_JSON, "text/json"})
        @Produces({MediaType.APPLICATION_JSON, "text/json"})
        public class DummyResponseProvider
                implements MessageBodyReader<Response>, MessageBodyWriter<Response> {

            @Override
            public boolean isReadable(
                    Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
                return aClass == Response.class;
            }

            @Override
            public Response readFrom(
                    Class<Response> aClass,
                    Type type,
                    Annotation[] annotations,
                    MediaType mediaType,
                    MultivaluedMap<String, String> multivaluedMap,
                    InputStream inputStream)
                    throws IOException, WebApplicationException {
                return null;
            }

            @Override
            public boolean isWriteable(
                    Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
                return aClass == Response.class;
            }

            @Override
            public long getSize(
                    Response response,
                    Class<?> aClass,
                    Type type,
                    Annotation[] annotations,
                    MediaType mediaType) {
                return 0;
            }

            @Override
            public void writeTo(
                    Response response,
                    Class<?> aClass,
                    Type type,
                    Annotation[] annotations,
                    MediaType mediaType,
                    MultivaluedMap<String, Object> multivaluedMap,
                    OutputStream outputStream)
                    throws IOException, WebApplicationException {}
        }

        @Override
        protected void configure() {
            ClientConfig config = new DefaultApacheHttpClient4Config();
            JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
            config.getSingletons().add(jsonProvider);
            config.getSingletons().add(new DummyResponseProvider());
            bind(ClientConfig.class).toInstance(config);
        }
    }

    @Test
    public void clientShouldTryUpdatingCredentialsAgainWhenRequestFailed() {

        prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/aggregation/aggregationcontroller_api/src/test/java/se/tink/backend/aggregation/aggregationcontroller/resources/aggregation_controller_mock_traffic.aap")));

        AggregationControllerAggregationClient client =
                Guice.createInjector(new TestModule())
                        .getInstance(AggregationControllerAggregationClient.class);

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("http://localhost:" + getPort());
        hostConfiguration.setBase64encodedclientcert("");
        hostConfiguration.setDisablerequestcompression(false);
        hostConfiguration.setApiToken("devtoken");
        hostConfiguration.setClusterId("local-development");

        /* Ugly workaround to have state support in WireMock server, we will get
        rid of this when our WireMock test tool will support states in its own
         */
        wireMockRule.listAllStubMappings().getMappings().stream()
                .filter(m -> m.getInsertionIndex() == 0)
                .findFirst()
                .get()
                .setRequiredScenarioState("Started");
        wireMockRule.listAllStubMappings().getMappings().stream()
                .filter(m -> m.getInsertionIndex() == 0)
                .findFirst()
                .get()
                .setNewScenarioState("ATTEMPT1");
        wireMockRule.listAllStubMappings().getMappings().stream()
                .filter(m -> m.getInsertionIndex() == 1)
                .findFirst()
                .get()
                .setRequiredScenarioState("ATTEMPT1");

        UpdateCredentialsStatusRequest request = new UpdateCredentialsStatusRequest();
        Credentials credentials = new Credentials();
        credentials.setId("dummy_id");
        request.setCredentials(credentials);
        credentials.setStatusUpdated(new Date(1));
        client.updateCredentials(hostConfiguration, request);
    }
}
