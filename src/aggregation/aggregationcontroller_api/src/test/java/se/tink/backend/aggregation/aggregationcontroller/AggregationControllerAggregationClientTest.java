package se.tink.backend.aggregation.aggregationcontroller;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.storage.database.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.libraries.credentials.rpc.Credentials;

public final class AggregationControllerAggregationClientTest {

    private static final String CLUSTER_ID = "local-development";
    private static AggregationControllerAggregationClient client;
    private static ClusterConfiguration clusterConfiguration;

    private static class TestModule extends AbstractModule {

        @Provider
        @Consumes({MediaType.APPLICATION_JSON, "text/json"})
        @Produces({MediaType.APPLICATION_JSON, "text/json"})
        public static class DummyResponseProvider
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
                    throws WebApplicationException {
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
                    throws WebApplicationException {}
        }

        @Override
        protected void configure() {
            ClientConfig config = new DefaultApacheHttpClient4Config();
            JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
            config.getSingletons().add(jsonProvider);
            config.getSingletons().add(new DummyResponseProvider());
            bind(ClientConfig.class).toInstance(config);
        }

        @Provides
        @Singleton
        @Named("clusterConfigurations")
        public Map<String, ClusterConfiguration> clusterConfigurations() {
            return Collections.singletonMap(CLUSTER_ID, clusterConfiguration);
        }
    }

    private UpdateCredentialsStatusRequest createUpdateCredentialsRequest(String credentialsId) {
        UpdateCredentialsStatusRequest request = new UpdateCredentialsStatusRequest();
        Credentials credentials = new Credentials();
        credentials.setId(credentialsId);
        request.setCredentials(credentials);
        credentials.setStatusUpdated(new Date(1));
        return request;
    }

    @BeforeClass
    public static void setUp() {
        // given
        WireMockTestServer server =
                new WireMockTestServer(
                        ImmutableSet.of(
                                new AapFileParser(
                                        ResourceFileReader.read(
                                                "src/aggregation/aggregationcontroller_api/src/test/java/se/tink/backend/aggregation/aggregationcontroller/resources/aggregation_controller_mock_traffic.aap"))));

        clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHost("http://localhost:" + server.getHttpPort());
        clusterConfiguration.setBase64encodedclientcert("");
        clusterConfiguration.setDisablerequestcompression(false);
        clusterConfiguration.setApiToken("devtoken");
        clusterConfiguration.setClusterId(CLUSTER_ID);

        client =
                Guice.createInjector(new TestModule())
                        .getInstance(AggregationControllerAggregationClientImpl.class);
    }

    @Test
    public void clientShouldTryUpdatingCredentialsAgainWhenRequestFailedWithStatusCode502() {
        // given
        UpdateCredentialsStatusRequest request = createUpdateCredentialsRequest("dummy_id");
        HostConfiguration hostConfiguration =
                HostConfigurationConverter.convert(clusterConfiguration);

        // when
        client.updateCredentials(hostConfiguration, request);

        // then
        // (No exception is thrown)
    }

    @Test(expected = UniformInterfaceException.class)
    public void clientShouldFailWhenRequestFailedWithStatusCode500() {
        // given
        UpdateCredentialsStatusRequest request =
                createUpdateCredentialsRequest("dummy_id_error_500");
        HostConfiguration hostConfiguration =
                HostConfigurationConverter.convert(clusterConfiguration);

        // when
        client.updateCredentials(hostConfiguration, request);

        // then
        // (expecting UniformInterfaceException)
    }
}
