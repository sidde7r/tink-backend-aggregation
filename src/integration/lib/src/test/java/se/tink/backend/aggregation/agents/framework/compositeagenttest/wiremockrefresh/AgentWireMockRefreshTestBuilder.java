package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.command.LoginCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.command.RefreshCommand;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

@Deprecated
public final class AgentWireMockRefreshTestBuilder {

    private final MarketCode market;

    private final String providerName;

    private final Set<String> wireMockFilePaths;

    private final Map<String, String> credentialFields;

    private String credentialPayload;

    private final Map<String, String> callbackData;

    private final Map<String, String> persistentStorageData;

    private final Map<String, String> cache;

    private final Set<RefreshableItem> refreshableItems;

    private boolean httpDebugTrace = false;

    private boolean dumpContentForContractFile = false;

    private boolean requestManual = true;

    private boolean requestCreate = false;

    private boolean requestUpdate = false;

    private boolean testAuthenticationOnly = false;

    private AgentsServiceConfiguration configuration;

    private TestModule agentTestModule;

    public AgentWireMockRefreshTestBuilder(
            MarketCode market, String providerName, String wireMockFilePath) {
        this.market = market;
        this.providerName = providerName;
        this.wireMockFilePaths = new HashSet<>(Collections.singleton(wireMockFilePath));
        this.configuration = new AgentsServiceConfiguration();
        this.credentialFields = new HashMap<>();
        this.callbackData = new HashMap<>();
        this.persistentStorageData = new HashMap<>();
        this.cache = new HashMap<>();
        this.refreshableItems = new HashSet<>();
    }

    /**
     * Use specified AgentsServiceConfiguration for agent. Agent will get an empty configuration if
     * none is specified.
     *
     * @param configuration Configuration to use for agent.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withConfigurationFile(
            AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Add credential field. e.g. username or password.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param key Key of credential field.
     * @param value Value of credential field.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addCredentialField(String key, String value) {
        credentialFields.put(key, value);
        return this;
    }

    public AgentWireMockRefreshTestBuilder addCredentialPayload(String credentialPayload) {
        this.credentialPayload = credentialPayload;
        return this;
    }

    /**
     * Add callback data to be returned from supplemental information request. TODO: This should be
     * moved to more flexible configuration file.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param key Key of callback data.
     * @param value Value of callback data.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addCallbackData(String key, String value) {
        callbackData.put(key, value);
        return this;
    }

    /**
     * Add data to be added to persistent storage. TODO: This should be moved to more flexible
     * configuration file.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param key Key of data to put to persistent storage.
     * @param value Value of data to put to persistent storage.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addPersistentStorageData(String key, String value) {
        persistentStorageData.put(key, value);
        return this;
    }

    /**
     * Add data to session cache.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param key Key of data to put to persistent storage.
     * @param value Value of data to put to persistent storage.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addDataIntoCache(String key, String value) {
        cache.put(key, value);
        return this;
    }

    /**
     * Allows adding additional dependencies via Guice dependency injection to agent. Dependencies
     * bound in the provided module will be available in the agents constructor, typically this
     * should be used to bind fake/mock versions of dependencies used in production.
     *
     * @param module Guice module to bind.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withAgentModule(TestModule module) {
        this.agentTestModule = module;
        return this;
    }

    /**
     * Add refreshable items. If not specified agent will use <code>
     * RefreshableItem.REFRESHABLE_ITEMS_ALL</code>.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param items Items to refresh.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addRefreshableItems(RefreshableItem... items) {
        this.refreshableItems.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * Add refreshable items. If not specified agent will use <code>
     * RefreshableItem.REFRESHABLE_ITEMS_ALL</code>.
     *
     * <p>Can be called multiple times to add several items.
     *
     * @param items Items to refresh.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addRefreshableItems(Set<RefreshableItem> items) {
        this.refreshableItems.addAll(items);
        return this;
    }

    /**
     * Enables http debug trace printout
     *
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withHttpDebugTrace() {
        this.httpDebugTrace = true;
        return this;
    }

    /**
     * Enables writing content for building a contract file at the end
     *
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder dumpContentForContractFile() {
        this.dumpContentForContractFile = true;
        return this;
    }

    /**
     * Add AAP file to be used.
     *
     * <p>Can be called multiple times to add several files.
     *
     * @param wireMockFilePath AAP file to be used.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder addAnotherWireMockFile(String wireMockFilePath) {
        this.wireMockFilePaths.add(wireMockFilePath);
        return this;
    }

    /**
     * Set the manual flag on the credentials request.
     *
     * @param manual Value for the request's manual flag.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withRequestFlagManual(boolean manual) {
        this.requestManual = manual;
        return this;
    }

    /**
     * Set the create flag on the credentials request.
     *
     * @param create Value for the request's create flag.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withRequestFlagCreate(boolean create) {
        this.requestCreate = create;
        return this;
    }

    /**
     * Set the update flag on the credentials request.
     *
     * @param update Value for the request's update flag.
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder withRequestFlagUpdate(boolean update) {
        this.requestUpdate = update;
        return this;
    }

    /**
     * Allows execution of refresh without any refreshable items
     *
     * @return This builder.
     */
    public AgentWireMockRefreshTestBuilder testAuthenticationOnly() {
        this.testAuthenticationOnly = true;
        return this;
    }

    public AgentWireMockRefreshTest build() {
        if (refreshableItems.isEmpty() && !testAuthenticationOnly) {
            refreshableItems.addAll(RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL));
        }

        return new AgentWireMockRefreshTest(
                market,
                providerName,
                wireMockFilePaths,
                configuration,
                credentialFields,
                credentialPayload,
                callbackData,
                persistentStorageData,
                new HashMap<>(),
                cache,
                agentTestModule,
                refreshableItems,
                ImmutableList.of(LoginCommand.class, RefreshCommand.class),
                httpDebugTrace,
                dumpContentForContractFile,
                requestManual,
                requestCreate,
                requestUpdate,
                true,
                false,
                prepareUserAvailability());
    }

    private UserAvailability prepareUserAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setOriginatingUserIp("127.0.0.1");
        userAvailability.setUserPresent(requestManual);
        userAvailability.setUserAvailableForInteraction(requestManual);
        return userAvailability;
    }
}
