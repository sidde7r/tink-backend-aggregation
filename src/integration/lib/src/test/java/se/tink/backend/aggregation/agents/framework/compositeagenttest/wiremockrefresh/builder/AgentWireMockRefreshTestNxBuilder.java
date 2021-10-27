package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder;

import static java.util.stream.Collectors.toSet;
import static se.tink.backend.aggregation.agents.agentplatform.authentication.storage.UpgradingPersistentStorageService.MARKER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.AgentsServiceConfigurationStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.AuthenticationConfigurationStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.BuildStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.MarketCodeStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.ProviderNameStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.RefreshOrAuthOnlyStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.RefreshableItemStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step.WireMockConfigurationStep;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.command.RefreshCommand;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

/**
 * Agent WireMock Refresh Test Builder. Consider not instantiating this builder directly. Use {@link
 * AgentWireMockRefreshTest#nxBuilder()} instead
 *
 * @see AgentWireMockRefreshTest
 */
public final class AgentWireMockRefreshTestNxBuilder
        implements MarketCodeStep,
                ProviderNameStep,
                WireMockConfigurationStep,
                AgentsServiceConfigurationStep,
                RefreshOrAuthOnlyStep,
                RefreshableItemStep,
                AuthenticationConfigurationStep,
                BuildStep {

    private MarketCode marketCode;

    private String providerName;

    private WireMockTestServer wireMockServer;

    private Set<File> wireMockFiles;

    private Map<String, String> credentialFields;

    private AgentsServiceConfiguration configuration;

    private Map<String, String> loginDetails;

    private String credentialPayload;

    private Map<String, String> callbackData;

    private Map<String, String> persistentStorageData;

    private Map<String, String> sessionStorageData;

    private Map<String, String> cache;

    private TestModule agentTestModule;

    private Set<RefreshableItem> refreshableItems;

    private List<Class<? extends CompositeAgentTestCommand>> commandSequence;

    private boolean httpDebugTraceEnabled;

    private boolean dumpContentForContractFile;

    private boolean requestFlagManual;

    private boolean requestFlagCreate;

    private boolean requestFlagUpdate;

    private boolean forceAutoAuthentication;

    private boolean skipAuthentication = false;

    private UserAvailability userAvailability;

    public AgentWireMockRefreshTestNxBuilder() {
        this.configuration = new AgentsServiceConfiguration();
        this.credentialFields = new HashMap<>();
        this.callbackData = new HashMap<>();
        this.persistentStorageData = new HashMap<>();
        this.sessionStorageData = new HashMap<>();
        this.cache = new HashMap<>();
        this.refreshableItems = new HashSet<>();
        this.httpDebugTraceEnabled = false;
        this.dumpContentForContractFile = false;
        this.requestFlagManual = true;
        this.requestFlagCreate = false;
        this.requestFlagUpdate = false;
        this.forceAutoAuthentication = false;
        this.userAvailability = new UserAvailability();
    }

    @Override
    public ProviderNameStep withMarketCode(MarketCode marketCode) {
        this.marketCode = marketCode;
        return this;
    }

    @Override
    public WireMockConfigurationStep withProviderName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    @Override
    public WireMockConfigurationStep withWireMockServer(WireMockTestServer wireMockServer) {
        this.wireMockServer = wireMockServer;
        return this;
    }

    @Override
    public AgentsServiceConfigurationStep withWireMockFilePath(String wireMockFilePath) {
        this.wireMockFiles = files(Collections.singleton(wireMockFilePath));
        return this;
    }

    @Override
    public AgentsServiceConfigurationStep withWireMockFilePaths(Set<String> wireMockFilePaths) {
        this.wireMockFiles = files(wireMockFilePaths);
        return this;
    }

    @Override
    public AuthenticationConfigurationStep withConfigFile(
            AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public AuthenticationConfigurationStep withoutConfigFile() {
        return this;
    }

    @Override
    public RefreshOrAuthOnlyStep testFullAuthentication() {
        this.userAvailability.setUserPresent(true);
        this.userAvailability.setUserAvailableForInteraction(true);
        this.userAvailability.setOriginatingUserIp("127.0.0.1");
        return this;
    }

    @Override
    public RefreshOrAuthOnlyStep testAutoAuthentication() {
        this.forceAutoAuthentication = true;
        this.requestFlagManual = false;
        this.userAvailability.setUserPresent(false);
        this.userAvailability.setUserAvailableForInteraction(false);
        this.userAvailability.setOriginatingUserIp(null);
        return this;
    }

    @Override
    public RefreshOrAuthOnlyStep skipAuthentication() {
        this.skipAuthentication = true;
        return this;
    }

    @Override
    public RefreshableItemStep addRefreshableItems(RefreshableItem... items) {
        this.refreshableItems.addAll(Arrays.asList(items));
        return this;
    }

    @Override
    public RefreshableItemStep withRefreshableItems(Set<RefreshableItem> refreshableItems) {
        this.refreshableItems.addAll(refreshableItems);
        return this;
    }

    @Override
    public BuildStep testOnlyAuthentication() {
        this.refreshableItems = new HashSet<>();
        return this;
    }

    @Override
    public BuildStep withLoginDetails(Map<String, String> loginDetails) {
        this.loginDetails = loginDetails;
        return this;
    }

    @Override
    public BuildStep withCredentialPayload(String credentialPayload) {
        this.credentialPayload = credentialPayload;
        return this;
    }

    @Override
    public BuildStep addCredentialField(String key, String value) {
        this.credentialFields.put(key, value);
        return this;
    }

    @Override
    public BuildStep addCallbackData(String key, String value) {
        this.callbackData.put(key, value);
        return this;
    }

    @Override
    public BuildStep addPersistentStorageData(String key, String value) {
        this.persistentStorageData.put(key, value);
        return this;
    }

    @SneakyThrows
    @Override
    public BuildStep addPersistentStorageData(String key, Object value) {
        this.persistentStorageData.put(key, new ObjectMapper().writeValueAsString(value));
        return this;
    }

    @Override
    public BuildStep addSessionStorageData(String key, String value) {
        this.sessionStorageData.put(key, value);
        return this;
    }

    @SneakyThrows
    @Override
    public BuildStep addSessionStorageData(String key, Object value) {
        this.sessionStorageData.put(key, new ObjectMapper().writeValueAsString(value));
        return this;
    }

    @Override
    public BuildStep addPersistentStorageData(Map<String, String> values) {
        this.persistentStorageData.putAll(values);
        return this;
    }

    @SneakyThrows
    @Override
    public BuildStep addRefreshableAccessToken(RefreshableAccessToken token) {
        String json = new ObjectMapper().writeValueAsString(token);

        // Based on
        // AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory.DEFAULT_TOKEN_STORE_KEY
        this.persistentStorageData.put("RedirectTokens", json);

        // This provides compatibility for agents implementing AgentPlatformStorageMigration
        this.persistentStorageData.put(MARKER, "true");
        return this;
    }

    @SneakyThrows
    @Override
    public BuildStep addRefreshableAccessTokenJson(String json) {

        // Based on
        // AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory.DEFAULT_TOKEN_STORE_KEY
        this.persistentStorageData.put("RedirectTokens", json);

        // This provides compatibility for agents implementing AgentPlatformStorageMigration
        this.persistentStorageData.put(MARKER, "true");

        return this;
    }

    @Override
    public BuildStep addDataIntoCache(String key, String value) {
        this.cache.put(key, value);
        return this;
    }

    @Override
    public BuildStep withAgentTestModule(TestModule agentTestModule) {
        this.agentTestModule = agentTestModule;
        return this;
    }

    @Override
    public BuildStep withCommandSequence(
            List<Class<? extends CompositeAgentTestCommand>> commandSequence) {
        this.commandSequence = commandSequence;
        return this;
    }

    @Override
    public BuildStep withRequestFlagManual(boolean requestFlagManual) {
        this.requestFlagManual = requestFlagManual;
        return this;
    }

    @Override
    public BuildStep withRequestFlagCreate(boolean requestFlagCreate) {
        this.requestFlagCreate = requestFlagCreate;
        return this;
    }

    @Override
    public BuildStep withRequestFlagUpdate(boolean requestFlagUpdate) {
        this.requestFlagUpdate = requestFlagUpdate;
        return this;
    }

    @Override
    public BuildStep withUserAvailability(UserAvailability userAvailability) {
        this.userAvailability = userAvailability;
        return this;
    }

    @Override
    public BuildStep enableHttpDebugTrace() {
        this.httpDebugTraceEnabled = true;
        return this;
    }

    @Override
    public BuildStep enableDataDumpForContractFile() {
        this.dumpContentForContractFile = true;
        return this;
    }

    @Override
    public AgentWireMockRefreshTest build() {
        return new AgentWireMockRefreshTest(
                marketCode,
                providerName,
                wireMockServer,
                wireMockFiles,
                configuration,
                credentialFields,
                credentialPayload,
                callbackData,
                persistentStorageData,
                sessionStorageData,
                cache,
                agentTestModule,
                refreshableItems,
                commands(skipAuthentication),
                httpDebugTraceEnabled,
                dumpContentForContractFile,
                requestFlagManual,
                requestFlagCreate,
                requestFlagUpdate,
                forceAutoAuthentication,
                userAvailability);
    }

    private List<Class<? extends CompositeAgentTestCommand>> commands(boolean skipAuthentication) {
        return skipAuthentication
                ? ImmutableList.of(RefreshCommand.class)
                : ImmutableList.of(LoginCommand.class, RefreshCommand.class);
    }

    private static Set<File> files(Set<String> paths) {
        return paths.stream().map(File::new).collect(toSet());
    }
}
