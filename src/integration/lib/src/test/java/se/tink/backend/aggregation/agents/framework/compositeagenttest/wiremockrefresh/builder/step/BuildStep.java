package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.builder.step;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.libraries.credentials.service.UserAvailability;

public interface BuildStep {

    BuildStep withLoginDetails(Map<String, String> loginDetails);

    BuildStep withCredentialPayload(String credentialPayload);

    /**
     * Add credential field to the map
     *
     * <p>Can be called multiple times to add several items
     *
     * <p>Example:
     *
     * <pre>
     * .addCredentialField(Key.USERNAME.getFieldKey(), DUMMY_USERNAME)
     * .addCredentialField(Key.PASSWORD.getFieldKey(), DUMMY_PASSWORD)
     * </pre>
     */
    BuildStep addCredentialField(String key, String value);

    BuildStep addCallbackData(String key, String value);

    /**
     * Add data to persistent storage map
     *
     * <p>Can be called multiple times to add several items
     */
    BuildStep addPersistentStorageData(String key, String value);

    BuildStep addPersistentStorageData(String key, Object value);

    /**
     * Add data to session storage map
     *
     * <p>Can be called multiple times to add several items
     */
    BuildStep addSessionStorageData(String key, String value);

    BuildStep addSessionStorageData(String key, Object value);

    BuildStep addPersistentStorageData(Map<String, String> values);

    /** Add RefreshableAccessToken to persistent storage map */
    BuildStep addRefreshableAccessToken(RefreshableAccessToken token);

    /** Add RefreshableAccessToken as json string to persistent storage map */
    BuildStep addRefreshableAccessTokenJson(String json);

    BuildStep addDataIntoCache(String key, String value);

    BuildStep withAgentTestModule(TestModule agentTestModule);

    BuildStep withCommandSequence(List<Class<? extends CompositeAgentTestCommand>> commandSequence);

    BuildStep withRequestFlagManual(boolean requestFlagManual);

    BuildStep withRequestFlagCreate(boolean requestFlagCreate);

    BuildStep withRequestFlagUpdate(boolean requestFlagUpdate);

    BuildStep withUserAvailability(UserAvailability userAvailability);

    /** Enable printing of http debug trace */
    BuildStep enableHttpDebugTrace();

    /** Enable printing of wire mock server logs */
    BuildStep enableWireMockServerLogs();

    /**
     * Enable printing of processed data from .aap file (response body mapped to tink model) Output
     * can be used to fill contract json file
     *
     * <p>Search "This is the content for building the contract file" phrase in console output
     */
    BuildStep enableDataDumpForContractFile();

    AgentWireMockRefreshTest build();
}
