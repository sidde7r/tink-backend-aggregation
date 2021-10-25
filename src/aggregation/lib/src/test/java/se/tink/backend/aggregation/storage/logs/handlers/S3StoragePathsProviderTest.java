package se.tink.backend.aggregation.storage.logs.handlers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Getter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RunWith(JUnitParamsRunner.class)
public class S3StoragePathsProviderTest {

    private S3StoragePathsProvider pathsProvider;

    private void createPathsProviderForTestCase(TestCase testCase) {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getId()).thenReturn(testCase.getCredentialsId());
        when(credentials.getUserId()).thenReturn(testCase.getUserId());

        Provider provider = mock(Provider.class);
        when(provider.getName()).thenReturn(testCase.getProviderName());
        when(provider.getClassName()).thenReturn(testCase.getAgentClassName());

        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
        when(localDateTimeSource.now()).thenReturn(testCase.getNow());

        AgentsServiceConfiguration agentsServiceConfiguration =
                mock(AgentsServiceConfiguration.class, Answers.RETURNS_DEEP_STUBS);
        when(agentsServiceConfiguration
                        .getAggregationWorker()
                        .getLongTermStorageDisputeBasePrefix())
                .thenReturn(testCase.getLongTermStorageDisputeBasePrefix());

        pathsProvider =
                new S3StoragePathsProvider(
                        agentsServiceConfiguration, credentials, provider, localDateTimeSource);
        pathsProvider.setOperationName(testCase.getOperationName());
        pathsProvider.setAppId(testCase.getAppId());
    }

    @Test
    @Parameters(method = "test_cases_for_default_raw_logs_paths")
    public void should_return_correct_default_raw_logs_path(TestCase testCase) {
        // given
        createPathsProviderForTestCase(testCase);

        // when
        String path = pathsProvider.getRawLogDefaultPath(testCase.getLogContent());

        // then
        assertThat(path).isEqualTo(testCase.getExpectedPath());
    }

    @SuppressWarnings("unused")
    private static Object[] test_cases_for_default_raw_logs_paths() {
        return Stream.of(
                        TestCase.builder()
                                .credentialsId("credentialsId")
                                .userId("userId")
                                .providerName("providerName")
                                .agentClassName("agentName")
                                .now(LocalDateTime.of(2021, 1, 11, 0, 0, 0))
                                .logContent("line1")
                                .expectedPath(
                                        format(
                                                "providerName_2021-01-11--01.00.00.000_uuserId_ccredentialsId_%sB_1.log",
                                                "line1".getBytes().length))
                                .build(),
                        TestCase.builder()
                                .credentialsId("credentialsId2")
                                .userId("userId2")
                                .providerName("providerName2")
                                .agentClassName("agentName2")
                                .now(LocalDateTime.of(2022, 2, 13, 1, 1, 1, 987654321))
                                .logContent("line1\nline2\nline3")
                                .expectedPath(
                                        format(
                                                "providerName2_2022-02-13--02.01.01.987_uuserId2_ccredentialsId2_%sB_3.log",
                                                "line1\nline2\nline3".getBytes().length))
                                .build())
                .toArray();
    }

    @Test
    @Parameters(method = "test_cases_for_payments_raw_logs_paths")
    public void should_return_correct_payments_raw_logs_path(TestCase testCase) {
        // given
        createPathsProviderForTestCase(testCase);

        // when
        String path = pathsProvider.getRawLogsPaymentsLtsPath(testCase.getLogContent());

        // then
        assertThat(path).isEqualTo(testCase.getExpectedPath());
    }

    @SuppressWarnings("unused")
    private static Object[] test_cases_for_payments_raw_logs_paths() {
        return Stream.of(
                        TestCase.builder()
                                .credentialsId("credentialsId")
                                .userId("userId")
                                .providerName("providerName")
                                .agentClassName("agentName")
                                .now(LocalDateTime.of(2021, 1, 11, 0, 0, 0))
                                .longTermStorageDisputeBasePrefix(null)
                                .logContent("line1\n")
                                .expectedPath(
                                        format(
                                                "/2021/1/11/providerName_2021-01-11--01.00.00.000_uuserId_ccredentialsId_%sB_1.log",
                                                "line1\n".getBytes().length))
                                .build(),
                        TestCase.builder()
                                .credentialsId("credentialsId2")
                                .userId("userId2")
                                .providerName("providerName2")
                                .agentClassName("agentName2")
                                .now(LocalDateTime.of(2022, 2, 13, 1, 1, 1, 987654321))
                                .longTermStorageDisputeBasePrefix("lts")
                                .logContent("line1\n\nline3\n")
                                .expectedPath(
                                        format(
                                                "lts/2022/2/13/providerName2_2022-02-13--02.01.01.987_uuserId2_ccredentialsId2_%sB_3.log",
                                                "line1\n\nline3\n".getBytes().length))
                                .build())
                .toArray();
    }

    @Test
    @Parameters(method = "test_cases_for_json_logs_paths")
    public void should_return_correct_json_logs_path(TestCase testCase) {
        // given
        createPathsProviderForTestCase(testCase);

        // when
        String path = pathsProvider.getJsonLogPath(testCase.getLogContent());

        // then
        assertThat(path).isEqualTo(testCase.getExpectedPath());
    }

    @SuppressWarnings("unused")
    private static Object[] test_cases_for_json_logs_paths() {
        return Stream.of(
                        TestCase.builder()
                                .credentialsId("credentialsId")
                                .userId("userId")
                                .providerName("providerName")
                                .agentClassName("agentName")
                                .now(LocalDateTime.of(2021, 1, 11, 0, 0, 0))
                                .operationName("operationName")
                                .appId("appId")
                                .logContent("line1")
                                .expectedPath(
                                        format(
                                                "bank-http-logs/agentName/2021-01-11/operationName/providerName/appId/providerName_2021-01-11--01.00.00.000_uuserId_ccredentialsId_%sB_1.json",
                                                "line1".getBytes().length))
                                .build(),
                        TestCase.builder()
                                .credentialsId("credentialsId2")
                                .userId("userId2")
                                .providerName("providerName2")
                                .agentClassName("agentName2")
                                .now(LocalDateTime.of(2022, 2, 13, 1, 1, 1, 333222111))
                                .operationName("operationName2")
                                .appId("appId2")
                                .longTermStorageDisputeBasePrefix("lts")
                                .logContent("line1\nline2\n")
                                .expectedPath(
                                        format(
                                                "bank-http-logs/agentName2/2022-02-13/operationName2/providerName2/appId2/providerName2_2022-02-13--02.01.01.333_uuserId2_ccredentialsId2_%sB_2.json",
                                                "line1\nline2\n".getBytes().length))
                                .build())
                .toArray();
    }

    @Getter
    @Builder
    private static class TestCase {
        private final String userId;
        private final String credentialsId;
        private final String agentClassName;
        private final String providerName;
        private final String operationName;
        private final String appId;
        private final LocalDateTime now;

        private final String longTermStorageDisputeBasePrefix;

        private final String logContent;
        private final String expectedPath;
    }
}
