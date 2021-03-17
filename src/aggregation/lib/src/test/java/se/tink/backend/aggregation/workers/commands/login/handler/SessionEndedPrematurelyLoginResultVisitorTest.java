package se.tink.backend.aggregation.workers.commands.login.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.collection.Metric;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.histograms.Histogram;
import se.tink.libraries.provider.ProviderDto;

public class SessionEndedPrematurelyLoginResultVisitorTest {

    private static final LocalDateTimeSource TEST_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();

    private MetricRegistry metricRegistry;
    private Credentials testCredentials;

    private CredentialsRequest mockCredRequest;
    private SessionEndedPrematurelyLoginResultVisitor sessionEndedPrematurelyLoginResultVisitor;

    @Before
    public void setup() {
        testCredentials = new Credentials();
        testCredentials.setType(CredentialsTypes.PASSWORD);
        Provider testProvider = testProvider();

        mockCredRequest = mock(CredentialsRequest.class);
        when(mockCredRequest.getCredentials()).thenReturn(testCredentials);
        when(mockCredRequest.getProvider()).thenReturn(testProvider);
        when(mockCredRequest.getType()).thenReturn(CredentialsRequestType.CREATE);

        metricRegistry = new MetricRegistry();

        sessionEndedPrematurelyLoginResultVisitor =
                new SessionEndedPrematurelyLoginResultVisitor(
                        metricRegistry, mockCredRequest, TEST_DATE_TIME_SOURCE);
    }

    private Provider testProvider() {
        Provider provider = new Provider();
        provider.setName("test_name");
        provider.setType(ProviderDto.ProviderTypes.BANK);
        provider.setAccessType(Provider.AccessType.OPEN_BANKING);
        provider.setMarket("de");
        provider.setClassName("test_class_name");
        return provider;
    }

    @Test
    public void shouldFillBucketsInMetricDependingOnDaysBetweenDatesWhenOldAgentType() {
        // given

        // when
        for (int i = 0; i < 100; i++) {
            LocalDate testDate = TEST_DATE_TIME_SOURCE.now().plusDays(i).toLocalDate();
            testCredentials.setSessionExpiryDate(testDate);
            sessionEndedPrematurelyLoginResultVisitor.visit(
                    new LoginAuthenticationErrorResult(SessionError.CONSENT_EXPIRED.exception()));
        }

        // then
        assertMetricFilledProperly();
    }

    @Test
    public void shouldFillBucketsInMetricDependingOnDaysBetweenDatesWhenAgentPlatfromError() {
        // given

        // when
        for (int i = 0; i < 100; i++) {
            LocalDate testDate = TEST_DATE_TIME_SOURCE.now().plusDays(i).toLocalDate();
            testCredentials.setSessionExpiryDate(testDate);
            sessionEndedPrematurelyLoginResultVisitor.visit(
                    new AgentPlatformLoginErrorResult(
                            new AgentPlatformAuthenticationProcessException(
                                    new AgentPlatformAuthenticationProcessError(
                                            new SessionExpiredError()),
                                    "")));
        }

        // then
        assertMetricFilledProperly();
    }

    private void assertMetricFilledProperly() {
        ImmutableMap<MetricId, Metric> metrics = metricRegistry.getMetrics();
        assertThat(metrics).hasSize(1);
        Map.Entry<MetricId, Metric> metricEntry = metrics.entrySet().iterator().next();
        MetricId metricId = metricEntry.getKey();
        Metric metric = metricEntry.getValue();

        assertMetricHasExpectedLabels(metricId);
        assertThat(metricId.getMetricName()).isEqualTo("session_expired_before_expected_date");

        assertThat(metric).isInstanceOf(Histogram.class);
        assertBucketsFilledProperly((Histogram) metricEntry.getValue());
    }

    private void assertMetricHasExpectedLabels(MetricId metricId) {
        Map<String, String> labels = metricId.getLabels();
        assertThat(labels).hasSize(7);
        assertThat(labels.get("provider_name")).isEqualTo("test_name");
        assertThat(labels.get("provider_type")).isEqualTo("bank");
        assertThat(labels.get("provider_access_type")).isEqualTo("OPEN_BANKING");
        assertThat(labels.get("market")).isEqualTo("de");
        assertThat(labels.get("className")).isEqualTo("test_class_name");
        assertThat(labels.get("credential")).isEqualTo("password");
        assertThat(labels.get("request_type")).isEqualTo("CREATE");
    }

    private void assertBucketsFilledProperly(Histogram histogram) {

        assertThat(histogram.getBuckets().getBucket(0.0)).isEqualTo(1);
        assertThat(histogram.getBuckets().getBucket(1.0)).isEqualTo(2);
        assertThat(histogram.getBuckets().getBucket(5.0)).isEqualTo(6);
        assertThat(histogram.getBuckets().getBucket(10.0)).isEqualTo(11);
        assertThat(histogram.getBuckets().getBucket(30.0)).isEqualTo(31);
        assertThat(histogram.getBuckets().getBucket(50.0)).isEqualTo(51);
        assertThat(histogram.getBuckets().getBucket(70.0)).isEqualTo(71);
        assertThat(histogram.getBuckets().getBucket(80.0)).isEqualTo(81);
        assertThat(histogram.getBuckets().getBucket(85.0)).isEqualTo(86);
        assertThat(histogram.getBuckets().getBucket(89.0)).isEqualTo(90);
        assertThat(histogram.getBuckets().getBucket(90.0)).isEqualTo(91);
        assertThat(histogram.getBuckets().getBucket(Double.POSITIVE_INFINITY)).isEqualTo(100);
    }
}
