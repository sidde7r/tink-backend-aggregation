package se.tink.backend.aggregation.workers.commands;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.stubbing.Answer;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.ExcludedDebugClusters;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogStorageHandler;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogsSaver;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogsSaverProvider;
import se.tink.backend.aggregation.storage.logs.SaveLogsResult;
import se.tink.backend.aggregation.storage.logs.handlers.AgentDebugLogConstants.AapLogsCatalog;
import se.tink.backend.aggregation.workers.commands.payment.PaymentsLegalConstraintsProvider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.payments_legal_constraints.PaymentsLegalConstraints;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@Slf4j
public class DebugAgentWorkerCommandTest {

    private static final List<SignableOperationStatuses> ALL_TRANSFER_STATUSES =
            allTransferStatuses();
    private static final List<SignableOperationStatuses> TRANSFER_STATUSES_THAT_REQUIRE_LOGGING =
            asList(SignableOperationStatuses.CANCELLED, SignableOperationStatuses.FAILED);
    private static final List<SignableOperationStatuses>
            TRANSFER_STATUSES_THAT_DO_NOT_REQUIRE_LOGGING =
                    removeFromList(ALL_TRANSFER_STATUSES, TRANSFER_STATUSES_THAT_REQUIRE_LOGGING);

    private static final List<CredentialsRequestType> ALL_REQUEST_TYPES =
            allCredentialsRequestTypes();
    private static final List<CredentialsRequestType> ALL_NOT_TRANSFER_REQUEST_TYPES =
            allCredentialsRequestTypesExcept(CredentialsRequestType.TRANSFER);

    private static final List<CredentialsStatus> ALL_CREDENTIALS_STATUSES =
            allCredentialsStatuses();
    private static final List<CredentialsStatus> CREDENTIALS_STATUSES_THAT_REQUIRE_LOGGING =
            asList(
                    CredentialsStatus.AUTHENTICATION_ERROR,
                    CredentialsStatus.TEMPORARY_ERROR,
                    CredentialsStatus.UNCHANGED);
    private static final List<CredentialsStatus> CREDENTIALS_STATUSES_THAT_DO_NOT_REQUIRE_LOGGING =
            removeFromList(ALL_CREDENTIALS_STATUSES, CREDENTIALS_STATUSES_THAT_REQUIRE_LOGGING);

    private static final List<String> EXCLUDED_CLUSTERS =
            asList("cluster1", "cluster2", "cluster3");
    private static final String SAMPLE_EXCLUDED_CLUSTER = "cluster2";
    private static final String SAMPLE_NOT_EXCLUDED_CLUSTER = "cluster_not_excluded";

    private static final String SAMPLE_APP_ID_WITH_TINK_LICENSE = "app_id_with_tink_license";
    private static final String SAMPLE_APP_ID_WITHOUT_TINK_LICENSE = "app_id_without_tink_license";

    private static final String AAP_STORAGE_DESCRIPTION = "HTTP s3://aap/storage";
    private static final String AAP_LTS_STORAGE_DESCRIPTION = "HTTP s3://aap/lts/storage";
    private static final String JSON_STORAGE_DESCRIPTION = "HTTP s3://json/storage";

    private static final UUID TRANSFER_ID = UUID.fromString("614fe246-5614-491d-ae2a-e88736fdac20");

    private AgentWorkerCommandContext context;
    private AgentDebugLogStorageHandler agentDebugLogStorageHandler;
    private AgentDebugLogsSaver agentDebugLogsSaver;
    private AgentDebugLogsSaverProvider agentDebugLogsSaverProvider;
    private PaymentsLegalConstraintsProvider paymentsLegalConstraintsProvider;
    private StringBuilder logResultsBuilder;

    private DebugAgentWorkerCommand command;

    private void resetTest() {
        context = mock(AgentWorkerCommandContext.class);
        agentDebugLogStorageHandler = mock(AgentDebugLogStorageHandler.class);
        logResultsBuilder = new StringBuilder();

        agentDebugLogsSaver = mock(AgentDebugLogsSaver.class);
        when(agentDebugLogsSaver.saveAapLogs(AapLogsCatalog.DEFAULT))
                .thenReturn(SaveLogsResult.saved(AAP_STORAGE_DESCRIPTION));
        when(agentDebugLogsSaver.saveAapLogs(AapLogsCatalog.LTS_PAYMENTS))
                .thenReturn(SaveLogsResult.saved(AAP_LTS_STORAGE_DESCRIPTION));
        when(agentDebugLogsSaver.saveJsonLogs())
                .thenReturn(SaveLogsResult.saved(JSON_STORAGE_DESCRIPTION));

        agentDebugLogsSaverProvider = mock(AgentDebugLogsSaverProvider.class);
        when(agentDebugLogsSaverProvider.createLogsSaver(any(), any()))
                .thenReturn(agentDebugLogsSaver);

        Map<String, PaymentsLegalConstraints> legalConstraintsMap =
                ImmutableMap.of(
                        SAMPLE_APP_ID_WITH_TINK_LICENSE, mockPaymentsLegalConstraints(true),
                        SAMPLE_APP_ID_WITHOUT_TINK_LICENSE, mockPaymentsLegalConstraints(false));
        paymentsLegalConstraintsProvider =
                mock(PaymentsLegalConstraintsProvider.class, Answers.RETURNS_DEEP_STUBS);
        when(paymentsLegalConstraintsProvider.getForAppId(anyString()))
                .thenAnswer(
                        (Answer<PaymentsLegalConstraints>)
                                invocation -> {
                                    String appId = invocation.getArgument(0);
                                    return legalConstraintsMap.get(appId);
                                });
    }

    private void reconstructCommand() {
        command =
                new DebugAgentWorkerCommand(
                        context,
                        agentDebugLogStorageHandler,
                        agentDebugLogsSaverProvider,
                        paymentsLegalConstraintsProvider,
                        logResultsBuilder);
    }

    @Test
    public void should_not_log_anything_when_cluster_is_disabled() {
        runTestCases(
                testCasesWithDisabledCluster(),
                testCase -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    verifyNoInteractions(agentDebugLogsSaver);

                    if (testCase.isTransferRequest()) {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Payment Credentials Status: TEMPORARY_ERROR"
                                                + "\n"
                                                + "Skipping logs: disabled on cluster");
                    } else {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Credential Status: TEMPORARY_ERROR"
                                                + "\n"
                                                + "Skipping logs: disabled on cluster");
                    }
                });
    }

    private static List<TestCase> testCasesWithDisabledCluster() {
        return new TestCasesGenerator()
                // main test parameters
                .clusterIds(SAMPLE_EXCLUDED_CLUSTER)

                // forcing debug that shouldn't work
                .debugLogPercentages(100)
                .isUserDebugFlags(true)
                .isCredentialsDebugFlags(true)

                // other params that shouldn't have any impact
                .requestTypes(ALL_REQUEST_TYPES)
                .loggingModes(
                        LoggingMode.LOGGING_MASKER_COVERS_SECRETS,
                        LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                .credentialsStatuses(CredentialsStatus.TEMPORARY_ERROR)

                // generate
                .generateTestCases();
    }

    @Test
    public void should_not_log_anything_when_log_masker_might_not_cover_all_sensitive_values() {
        runTestCases(
                testCasesWithNotSafeLoggingMode(),
                testCase -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    verifyNoInteractions(agentDebugLogsSaver);

                    if (testCase.isTransferRequest()) {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Payment Credentials Status: TEMPORARY_ERROR"
                                                + "\n"
                                                + "Skipping logs: logging masker may not cover all secrets");
                    } else {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Credential Status: TEMPORARY_ERROR"
                                                + "\n"
                                                + "Skipping logs: logging masker may not cover all secrets");
                    }
                });
    }

    private static List<TestCase> testCasesWithNotSafeLoggingMode() {
        return new TestCasesGenerator()
                // main test parameters
                .loggingModes(null, LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)

                // forcing debug that shouldn't work
                .debugLogPercentages(100)
                .isUserDebugFlags(true)
                .isCredentialsDebugFlags(true)

                // other params that shouldn't have any impact
                .requestTypes(ALL_REQUEST_TYPES)
                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                .credentialsStatuses(CredentialsStatus.TEMPORARY_ERROR)

                // generate
                .generateTestCases();
    }

    @Test
    public void
            should_log_only_transfers_with_statuses_that_require_logging_when_debug_is_not_forced() {
        runTestCases(
                testCasesForTransferStatusesThatRequireLoggingAndDebugNotForced(),
                (testCase, shouldBeLogged) -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    if (shouldBeLogged) {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Payment Credentials Status: "
                                                + testCase.getCredentialsStatus()
                                                + "\n"
                                                + "Flushed transfer (614fe2465614491dae2ae88736fdac20) debug log for further investigation: HTTP s3://aap/storage"
                                                + "\n"
                                                + "Flushed transfer (614fe2465614491dae2ae88736fdac20) json logs: HTTP s3://json/storage");
                        verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.DEFAULT);
                        verify(agentDebugLogsSaver).saveJsonLogs();

                    } else {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Payment Credentials Status: "
                                                + testCase.getCredentialsStatus()
                                                + "\n"
                                                + "Skipping logs: should not log");
                    }

                    verifyNoMoreInteractions(agentDebugLogsSaver);
                });
    }

    private static List<Pair<TestCase, Boolean>>
            testCasesForTransferStatusesThatRequireLoggingAndDebugNotForced() {
        List<Pair<TestCase, Boolean>> testCasesThatShouldBeLogged =
                new TestCasesGenerator()
                                // main test parameters
                                .requestTypes(CredentialsRequestType.TRANSFER)
                                .transferStatuses(TRANSFER_STATUSES_THAT_REQUIRE_LOGGING)

                                // enable logging in general
                                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                                // do not force debug
                                .debugLogPercentages(0)

                                // other params that shouldn't have any impact
                                .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                                .isUserDebugFlags(false).isCredentialsDebugFlags(false)

                                // generate
                                .generateTestCases().stream()
                                .map(testCase -> Pair.of(testCase, true))
                                .collect(toList());

        List<Pair<TestCase, Boolean>> testCasesThatShouldNotBeLogged =
                new TestCasesGenerator()
                                // main test parameters
                                .requestTypes(CredentialsRequestType.TRANSFER)
                                .transferStatuses(TRANSFER_STATUSES_THAT_DO_NOT_REQUIRE_LOGGING)
                                // do not force debug
                                .debugLogPercentages(0)

                                // enable logging in general
                                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                                // other params that shouldn't have any impact
                                .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                                .isUserDebugFlags(true).isCredentialsDebugFlags(true)

                                // generate
                                .generateTestCases().stream()
                                .map(testCase -> Pair.of(testCase, false))
                                .collect(toList());

        return Stream.of(testCasesThatShouldBeLogged, testCasesThatShouldNotBeLogged)
                .flatMap(List::stream)
                .collect(toList());
    }

    @Test
    public void should_log_transfers_with_all_statuses_when_debug_is_forced() {
        runTestCases(
                testCasesForTransfersWithForcedDebug(),
                testCase -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    assertThat(logResultsBuilder)
                            .hasToString(
                                    "Payment Credentials Status: "
                                            + testCase.getCredentialsStatus()
                                            + "\n"
                                            + "Flushed transfer (614fe2465614491dae2ae88736fdac20) debug log for further investigation: HTTP s3://aap/storage"
                                            + "\n"
                                            + "Flushed transfer (614fe2465614491dae2ae88736fdac20) json logs: HTTP s3://json/storage");

                    verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.DEFAULT);
                    verify(agentDebugLogsSaver).saveJsonLogs();
                    verifyNoMoreInteractions(agentDebugLogsSaver);
                });
    }

    private static List<TestCase> testCasesForTransfersWithForcedDebug() {
        return new TestCasesGenerator()
                // main test parameters
                .requestTypes(CredentialsRequestType.TRANSFER)
                .transferStatuses(addNullToList(ALL_TRANSFER_STATUSES))
                // force debug
                .debugLogPercentages(100)

                // enable logging in general
                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                // other params that shouldn't have any impact
                .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                .isUserDebugFlags(false)
                .isCredentialsDebugFlags(false)

                // generate
                .generateTestCases();
    }

    @Test
    public void should_log_transfers_in_aap_and_lts_catalogs_if_app_is_on_tinks_license() {
        runTestCases(
                testCasesForTransfersOnTinksLicense(),
                testCase -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    assertThat(logResultsBuilder)
                            .hasToString(
                                    "Payment Credentials Status: "
                                            + testCase.getCredentialsStatus()
                                            + "\n"
                                            + "Flushed transfer (614fe2465614491dae2ae88736fdac20) debug log for further investigation: HTTP s3://aap/storage"
                                            + "\n"
                                            + "Flushed transfer to long term storage for payments disputes (614fe2465614491dae2ae88736fdac20) debug log for further investigation: HTTP s3://aap/lts/storage"
                                            + "\n"
                                            + "Flushed transfer (614fe2465614491dae2ae88736fdac20) json logs: HTTP s3://json/storage");

                    verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.DEFAULT);
                    verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.LTS_PAYMENTS);
                    verify(agentDebugLogsSaver).saveJsonLogs();
                    verifyNoMoreInteractions(agentDebugLogsSaver);
                });
    }

    private static List<TestCase> testCasesForTransfersOnTinksLicense() {
        return new TestCasesGenerator()
                // main test parameters
                .requestTypes(CredentialsRequestType.TRANSFER)
                .appIds(SAMPLE_APP_ID_WITH_TINK_LICENSE)
                // force debug
                .debugLogPercentages(100)

                // enable logging in general
                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                // combinations of other params that shouldn't have any impact
                .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                .isUserDebugFlags(false)

                // generate
                .generateTestCases();
    }

    @Test
    public void should_log_only_credentials_with_status_requiring_logging_if_debug_is_not_forced() {
        runTestCases(
                testCasesWithCredentialsWithStatusRequiringLoggingAndDebugNotForced(),
                (testCase, shouldBeLogged) -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    if (shouldBeLogged) {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Credential Status: "
                                                + testCase.getCredentialsStatus()
                                                + "\n"
                                                + "Flushed http logs: HTTP s3://aap/storage"
                                                + "\n"
                                                + "Flushed http json logs: HTTP s3://json/storage");
                        verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.DEFAULT);
                        verify(agentDebugLogsSaver).saveJsonLogs();

                    } else {
                        assertThat(logResultsBuilder)
                                .hasToString(
                                        "Credential Status: "
                                                + testCase.getCredentialsStatus()
                                                + "\n"
                                                + "Skipping logs: should not log");
                    }
                    verifyNoMoreInteractions(agentDebugLogsSaver);
                });
    }

    private static List<Pair<TestCase, Boolean>>
            testCasesWithCredentialsWithStatusRequiringLoggingAndDebugNotForced() {
        List<Pair<TestCase, Boolean>> testCasesThatShouldBeLogged =
                new TestCasesGenerator()
                                // main test parameters
                                .requestTypes(ALL_NOT_TRANSFER_REQUEST_TYPES)
                                .credentialsStatuses(CREDENTIALS_STATUSES_THAT_REQUIRE_LOGGING)

                                // do not force debug
                                .debugLogPercentages(0).isCredentialsDebugFlags(false)
                                .isUserDebugFlags(false)

                                // enable logging in general
                                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                                // generate
                                .generateTestCases().stream()
                                .map(testCase -> Pair.of(testCase, true))
                                .collect(toList());

        List<Pair<TestCase, Boolean>> testCasesThatShouldNotBeLogged =
                new TestCasesGenerator()
                                // main test parameters
                                .requestTypes(ALL_NOT_TRANSFER_REQUEST_TYPES)
                                .credentialsStatuses(
                                        CREDENTIALS_STATUSES_THAT_DO_NOT_REQUIRE_LOGGING)

                                // do not force debug
                                .debugLogPercentages(0).isCredentialsDebugFlags(false)
                                .isUserDebugFlags(false)

                                // enable logging in general
                                .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                                .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                                // generate
                                .generateTestCases().stream()
                                .map(testCase -> Pair.of(testCase, false))
                                .collect(toList());

        return Stream.of(testCasesThatShouldBeLogged, testCasesThatShouldNotBeLogged)
                .flatMap(List::stream)
                .collect(toList());
    }

    @Test
    public void should_log_all_certain_credentials_requests_types_if_debug_is_forced() {
        runTestCases(
                testParamsShouldLogAllCertainCredentialsRequestsTypesIfDebugIsForced(),
                testCase -> {
                    // given
                    context = mockContext(testCase);
                    reconstructCommand();

                    // when
                    command.doPostProcess();

                    // then
                    assertThat(logResultsBuilder)
                            .hasToString(
                                    "Credential Status: "
                                            + testCase.getCredentialsStatus()
                                            + "\n"
                                            + "Flushed http logs: HTTP s3://aap/storage"
                                            + "\n"
                                            + "Flushed http json logs: HTTP s3://json/storage");

                    verify(agentDebugLogsSaver).saveAapLogs(AapLogsCatalog.DEFAULT);
                    verify(agentDebugLogsSaver).saveJsonLogs();
                    verifyNoMoreInteractions(agentDebugLogsSaver);
                });
    }

    private static List<TestCase>
            testParamsShouldLogAllCertainCredentialsRequestsTypesIfDebugIsForced() {
        List<TestCase> testCasesWithDebugForcedByIsUserDebugFlag =
                new TestCasesGenerator()
                        // main test parameters
                        .requestTypes(ALL_NOT_TRANSFER_REQUEST_TYPES)
                        .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                        .isUserDebugFlags(true)

                        // do not force debug other way
                        .debugLogPercentages(0)
                        .isCredentialsDebugFlags(false)

                        // enable logging in general
                        .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                        .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                        // generate
                        .generateTestCases();

        List<TestCase> testCasesWithDebugForcedByIsCredentialsDebugFlag =
                new TestCasesGenerator()
                        // main test parameters
                        .requestTypes(ALL_NOT_TRANSFER_REQUEST_TYPES)
                        .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                        .isCredentialsDebugFlags(true)

                        // do not force debug other way
                        .debugLogPercentages(0)
                        .isUserDebugFlags(true)

                        // enable logging in general
                        .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                        .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                        // generate
                        .generateTestCases();

        List<TestCase> testCasesWithDebugForcedByDebugLogFrequencySetTo100 =
                new TestCasesGenerator()
                        // main test parameters
                        .requestTypes(ALL_NOT_TRANSFER_REQUEST_TYPES)
                        .credentialsStatuses(ALL_CREDENTIALS_STATUSES)
                        .debugLogPercentages(100)

                        // do not force debug other way
                        .isUserDebugFlags(true)
                        .isCredentialsDebugFlags(true)

                        // enable logging in general
                        .clusterIds(SAMPLE_NOT_EXCLUDED_CLUSTER)
                        .loggingModes(LoggingMode.LOGGING_MASKER_COVERS_SECRETS)

                        // generate
                        .generateTestCases();

        return Stream.of(
                        testCasesWithDebugForcedByIsUserDebugFlag,
                        testCasesWithDebugForcedByIsCredentialsDebugFlag,
                        testCasesWithDebugForcedByDebugLogFrequencySetTo100)
                .flatMap(List::stream)
                .collect(toList());
    }

    private static List<SignableOperationStatuses> allTransferStatuses() {
        return Stream.of(SignableOperationStatuses.values()).collect(toList());
    }

    private static List<CredentialsRequestType> allCredentialsRequestTypes() {
        return Stream.of(CredentialsRequestType.values()).collect(toList());
    }

    @SuppressWarnings("SameParameterValue")
    private static List<CredentialsRequestType> allCredentialsRequestTypesExcept(
            CredentialsRequestType excludedType) {
        return Stream.of(CredentialsRequestType.values())
                .filter(type -> type != excludedType)
                .collect(toList());
    }

    private static List<CredentialsStatus> allCredentialsStatuses() {
        return Stream.of(CredentialsStatus.values()).collect(toList());
    }

    private static <T> List<T> removeFromList(List<T> list, List<T> itemsToRemove) {
        List<T> copy = new ArrayList<>(list);
        copy.removeAll(itemsToRemove);
        return copy;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> addNullToList(List<T> list) {
        List<T> copy = new ArrayList<>(list);
        copy.add(null);
        return copy;
    }

    private static PaymentsLegalConstraints mockPaymentsLegalConstraints(boolean isTinkLicense) {
        PaymentsLegalConstraints paymentsLegalConstraints = mock(PaymentsLegalConstraints.class);
        when(paymentsLegalConstraints.isOnTinksLicense()).thenReturn(isTinkLicense);
        return paymentsLegalConstraints;
    }

    private static AgentWorkerCommandContext mockContext(TestCase testCase) {
        return new ContextMocker(testCase).mockContext();
    }

    @RequiredArgsConstructor
    private static class ContextMocker {

        private final TestCase testCase;

        private Provider provider;
        private Credentials credentials;

        public AgentWorkerCommandContext mockContext() {
            AgentWorkerCommandContext context =
                    mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);

            AgentsServiceConfiguration agentsServiceConfiguration =
                    AgentsServiceConfiguration.builder()
                            .excludedDebugClusters(
                                    ExcludedDebugClusters.builder()
                                            .excludedClusters(EXCLUDED_CLUSTERS)
                                            .build())
                            .aggregationWorker(
                                    AggregationWorkerConfiguration.builder()
                                            .debugLogFrequencyPercent(
                                                    testCase.getDebugLogFrequencyPercent())
                                            .build())
                            .build();
            when(context.getAgentsServiceConfiguration()).thenReturn(agentsServiceConfiguration);

            provider = mock(Provider.class);
            credentials = mock(Credentials.class);
            when(credentials.getStatus()).thenReturn(testCase.getCredentialsStatus());
            when(credentials.isDebug()).thenReturn(testCase.isCredentialsDebug());

            LogMasker logMasker = mock(LogMasker.class);
            when(logMasker.shouldLog(provider)).thenReturn(testCase.getLoggingModeForProvider());
            when(context.getLogMasker()).thenReturn(logMasker);

            CredentialsRequest request = buildRequest();
            when(context.getRequest()).thenReturn(request);

            when(context.getAppId()).thenReturn(testCase.getAppId());
            when(context.getClusterId()).thenReturn(testCase.getClusterId());
            return context;
        }

        private CredentialsRequest buildRequest() {
            if (testCase.getRequestType() == CredentialsRequestType.TRANSFER) {
                return buildTransferRequest();
            }
            return buildCredentialsRequest();
        }

        private TransferRequest buildTransferRequest() {
            TransferRequest request = mock(TransferRequest.class, Answers.RETURNS_DEEP_STUBS);
            when(request.getTransfer().getId()).thenReturn(TRANSFER_ID);
            when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);
            when(request.getProvider()).thenReturn(provider);
            when(request.getCredentials()).thenReturn(credentials);
            when(request.getSignableOperation().getStatus())
                    .thenReturn(testCase.getTransferStatus());
            when(request.getUser().isDebug()).thenReturn(testCase.isUserDebug());
            return request;
        }

        private CredentialsRequest buildCredentialsRequest() {
            CredentialsRequest request = mock(TransferRequest.class, Answers.RETURNS_DEEP_STUBS);
            when(request.getType()).thenReturn(testCase.getRequestType());
            when(request.getProvider()).thenReturn(provider);
            when(request.getCredentials()).thenReturn(credentials);
            when(request.getUser().isDebug()).thenReturn(testCase.isUserDebug());
            return request;
        }
    }

    /*
    This method exists because running many tests in a loop turns out to be much faster than using junitparams
     */
    private void runTestCases(List<TestCase> testCases, Consumer<TestCase> testCaseConsumer) {
        for (TestCase testCase : testCases) {
            try {
                resetTest();
                testCaseConsumer.accept(testCase);
            } catch (RuntimeException e) {
                throw new RuntimeException("Error when running test case: " + testCase, e);
            }
        }
        log.info("Run {} test cases", testCases.size());
    }

    private void runTestCases(
            List<Pair<TestCase, Boolean>> testCases,
            BiConsumer<TestCase, Boolean> testCaseConsumer) {
        for (Pair<TestCase, Boolean> args : testCases) {
            try {
                resetTest();
                testCaseConsumer.accept(args.getLeft(), args.getRight());
            } catch (AssertionError e) {
                throw new RuntimeException("Error when running test case: " + args.getLeft(), e);
            }
        }
        log.info("Run {} test cases", testCases.size());
    }

    @ToString
    @Getter
    @Builder
    private static class TestCase {
        private final String clusterId;
        private final int debugLogFrequencyPercent;

        private final LoggingMode loggingModeForProvider;

        private final CredentialsRequestType requestType;
        private final CredentialsStatus credentialsStatus;
        private final boolean isUserDebug;
        private final boolean isCredentialsDebug;
        private final SignableOperationStatuses transferStatus;

        private final String appId;

        private boolean isTransferRequest() {
            return requestType == CredentialsRequestType.TRANSFER;
        }
    }

    private static class TestCasesGenerator {

        /*
        Defaults
         */
        private List<String> clusterIds = singletonList(null);
        private List<CredentialsRequestType> requestTypes = singletonList(null);
        private List<Integer> debugLogFrequencyPercentages = singletonList(null);
        private List<LoggingMode> loggingModes = singletonList(null);
        private List<CredentialsStatus> credentialsStatuses = singletonList(null);
        private List<Boolean> isUserDebugFlags = singletonList(false);
        private List<Boolean> isCredentialsDebugFlags = singletonList(false);
        private List<SignableOperationStatuses> transferStatuses = singletonList(null);
        private List<String> appIds = singletonList(null);

        public TestCasesGenerator clusterIds(String... clusterIds) {
            this.clusterIds = asList(clusterIds);
            return this;
        }

        public TestCasesGenerator requestTypes(CredentialsRequestType... requestTypes) {
            this.requestTypes = asList(requestTypes);
            return this;
        }

        public TestCasesGenerator requestTypes(List<CredentialsRequestType> requestTypes) {
            this.requestTypes = requestTypes;
            return this;
        }

        public TestCasesGenerator debugLogPercentages(Integer... debugLogFrequencyPercentages) {
            this.debugLogFrequencyPercentages = asList(debugLogFrequencyPercentages);
            return this;
        }

        public TestCasesGenerator loggingModes(LoggingMode... loggingModes) {
            this.loggingModes = asList(loggingModes);
            return this;
        }

        public TestCasesGenerator credentialsStatuses(CredentialsStatus... credentialsStatuses) {
            this.credentialsStatuses = asList(credentialsStatuses);
            return this;
        }

        public TestCasesGenerator credentialsStatuses(List<CredentialsStatus> credentialsStatuses) {
            this.credentialsStatuses = credentialsStatuses;
            return this;
        }

        public TestCasesGenerator isUserDebugFlags(Boolean... isUserDebugFlags) {
            this.isUserDebugFlags = asList(isUserDebugFlags);
            return this;
        }

        public TestCasesGenerator isCredentialsDebugFlags(Boolean... isCredentialsDebugFlags) {
            this.isCredentialsDebugFlags = asList(isCredentialsDebugFlags);
            return this;
        }

        public TestCasesGenerator transferStatuses(
                List<SignableOperationStatuses> transferStatuses) {
            this.transferStatuses = transferStatuses;
            return this;
        }

        public TestCasesGenerator appIds(String... appIds) {
            this.appIds = asList(appIds);
            return this;
        }

        public List<TestCase> generateTestCases() {
            return cartesianProduct(
                            clusterIds,
                            requestTypes,
                            debugLogFrequencyPercentages,
                            loggingModes,
                            credentialsStatuses,
                            isUserDebugFlags,
                            isCredentialsDebugFlags,
                            transferStatuses,
                            appIds)
                    .stream()
                    .map(
                            params -> {
                                String clusterId = (String) params.get(0);
                                CredentialsRequestType requestType =
                                        (CredentialsRequestType) params.get(1);
                                Integer debugLogFrequency = (Integer) params.get(2);
                                LoggingMode loggingMode = (LoggingMode) params.get(3);
                                CredentialsStatus credentialsStatus =
                                        (CredentialsStatus) params.get(4);
                                Boolean isUserDebug = (Boolean) params.get(5);
                                Boolean isCredentialsDebug = (Boolean) params.get(6);
                                SignableOperationStatuses transferStatus =
                                        (SignableOperationStatuses) params.get(7);
                                String appId = (String) params.get(8);

                                return TestCase.builder()
                                        .clusterId(clusterId)
                                        .requestType(requestType)
                                        .debugLogFrequencyPercent(debugLogFrequency)
                                        .loggingModeForProvider(loggingMode)
                                        .credentialsStatus(credentialsStatus)
                                        .isUserDebug(isUserDebug)
                                        .isCredentialsDebug(isCredentialsDebug)
                                        .transferStatus(transferStatus)
                                        .appId(appId)
                                        .build();
                            })
                    .collect(toList());
        }

        private static List<List<Object>> cartesianProduct(List<?>... lists) {
            List<Set<Integer>> allIndexesForAllLists =
                    Stream.of(lists)
                            .map(
                                    list ->
                                            IntStream.range(0, list.size())
                                                    .boxed()
                                                    .collect(Collectors.toSet()))
                            .collect(toList());

            Set<List<Integer>> cartesianProductOfIndexes =
                    Sets.cartesianProduct(allIndexesForAllLists);

            return cartesianProductOfIndexes.stream()
                    .map(
                            indexes -> {
                                List<Object> objects = new ArrayList<>();

                                for (int listIndex = 0; listIndex < lists.length; listIndex++) {
                                    List<?> listToTakeObjectsFrom = lists[listIndex];
                                    int indexToTakeObjectFrom = indexes.get(listIndex);
                                    objects.add(listToTakeObjectsFrom.get(indexToTakeObjectFrom));
                                }

                                return objects;
                            })
                    .collect(toList());
        }
    }
}
