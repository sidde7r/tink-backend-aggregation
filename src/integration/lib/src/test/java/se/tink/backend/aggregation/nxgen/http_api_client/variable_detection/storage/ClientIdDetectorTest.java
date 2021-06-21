package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.aggregation_agent_api_client.src.variable.InMemoryVariableStore;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;

public class ClientIdDetectorTest {
    private InMemoryVariableStore variableStore;
    private final ClientIdDetector clientIdDetector = new ClientIdDetector();

    @Before
    public void setup() {
        this.variableStore = new InMemoryVariableStore();
    }

    @Test
    public void testClientIdInsertionIsDetected() {
        boolean detected =
                clientIdDetector.detectVariableFromInsertion(
                        variableStore, "clientId", "dummyClientId");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyClientId", variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));

        detected =
                clientIdDetector.detectVariableFromInsertion(
                        variableStore, "bankname_client_id", "dummyClientId2");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyClientId2", variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));
    }

    @Test
    public void testNonClientIdInsertionIsNotDetected() {
        boolean detected =
                clientIdDetector.detectVariableFromInsertion(
                        variableStore, "dummyKey", "dummyValue");

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));
    }

    @Test
    public void testClientIdFromStorageIsDetected() {
        boolean detected =
                clientIdDetector.detectVariableFromStorage(
                        variableStore, "clientId", "dummyClientId");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyClientId", variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));

        detected =
                clientIdDetector.detectVariableFromStorage(
                        variableStore, "bankname_client_id", "dummyClientId2");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyClientId2", variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));
    }

    @Test
    public void testNonClientIdFromStorageIsNotDetected() {
        boolean detected =
                clientIdDetector.detectVariableFromStorage(variableStore, "dummyKey", "dummyValue");

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.CLIENT_ID).orElse(null));
    }
}
