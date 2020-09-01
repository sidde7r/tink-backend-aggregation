package se.tink.backend.aggregation.aggregationcontroller;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.libraries.credentials.rpc.Credentials;

public final class AggregationControllerWrapperTest {

    private static WireMockTestServer server;
    private static AggregationControllerAggregationClient client;
    private static ControllerWrapper wrapper;
    private static HostConfiguration hostConfiguration;

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

    private UpdateCredentialsStatusRequest createUpdateCredentialsRequest(String credentialsId) {
        UpdateCredentialsStatusRequest request = new UpdateCredentialsStatusRequest();
        Credentials credentials = new Credentials();
        credentials.setId(credentialsId);
        request.setCredentials(credentials);
        credentials.setStatusUpdated(new Date(1));
        return request;
    }

    private UpdateTransactionsRequest createUpdateTransactionRequest(String credentialsId) {
        UpdateTransactionsRequest request = new UpdateTransactionsRequest();
        request.setCredentials(credentialsId);
        request.setTransactions(new ArrayList<>());
        request.setUser("Dummy_User");
        request.setRequestType(CredentialsRequestType.CREATE);
        request.setAggregationId("foo");
        request.setUserTriggered(false);
        request.setTopic("foo");
        return request;
    }

    @BeforeClass
    public static void setUp() {
        // given
        client =
                Guice.createInjector(new TestModule())
                        .getInstance(AggregationControllerAggregationClientImpl.class);

        server =
                new WireMockTestServer(
                        ImmutableSet.of(
                                new AapFileParser(
                                        new ResourceFileReader()
                                                .read(
                                                        "src/aggregation/aggregationcontroller_api/src/test/java/se/tink/backend/aggregation/aggregationcontroller/resources/aggregation_controller_mock_traffic.aap"))));

        hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("http://localhost:" + server.getHttpPort());
        hostConfiguration.setBase64encodedclientcert("");
        hostConfiguration.setDisablerequestcompression(false);
        hostConfiguration.setApiToken("devtoken");
        hostConfiguration.setClusterId("local-development");

        wrapper = ControllerWrapper.of(client, hostConfiguration);
    }

    @Test
    public void clientShouldSerializeNullValuesAgainstNonCornwallWithoutAnyException() {
        // given
        UpdateTransactionsRequest request = createUpdateTransactionRequest("not_dummy_cornwall");

        // when
        wrapper.updateTransactionsAsynchronously(request);
    }

    @Test
    public void clientShouldRemoveNullsAgainstCornwall() {
        // given
        hostConfiguration.setClusterId("cornwall-production");
        UpdateTransactionsRequest request = createUpdateTransactionRequest("dummy_cornwall");

        // when
        wrapper.updateTransactionsAsynchronously(request);
    }

    @Test
    public void clientShouldErrorOutWithoutCornwall() {
        // given
        UpdateTransactionsRequest request = createUpdateTransactionRequest("dummy_cornwall");

        // when
        Throwable thrown = catchThrowable(() -> wrapper.updateTransactionsAsynchronously(request));

        Assert.assertThat(thrown, instanceOf(UniformInterfaceException.class));
        // then
        // (expecting UniformInterfaceException)
    }
}
