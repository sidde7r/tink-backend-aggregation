package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.aggregation_agent_api_client.src.variable.InMemoryVariableStore;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;

public class ConsentIdDetectorTest {
    private InMemoryVariableStore variableStore;
    private final ConsentIdDetector consentIdDetector = new ConsentIdDetector();

    @Before
    public void setup() {
        this.variableStore = new InMemoryVariableStore();
    }

    @Test
    public void testClientIdInsertionIsDetected() {
        boolean detected =
                consentIdDetector.detectVariableFromInsertion(
                        variableStore, "consentId", "dummyConsentId");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyConsentId", variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));

        detected =
                consentIdDetector.detectVariableFromInsertion(
                        variableStore, "bankname_consent_id", "dummyConsentId2");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyConsentId2", variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));
    }

    @Test
    public void testNonClientIdInsertionIsNotDetected() {
        boolean detected =
                consentIdDetector.detectVariableFromInsertion(
                        variableStore, "dummyKey", "dummyValue");

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));
    }

    @Test
    public void testClientIdFromStorageIsDetected() {
        boolean detected =
                consentIdDetector.detectVariableFromStorage(
                        variableStore, "consentId", "dummyConsentId");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyConsentId", variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));

        detected =
                consentIdDetector.detectVariableFromStorage(
                        variableStore, "bankname_consent_id", "dummyConsentId2");

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "dummyConsentId2", variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));
    }

    @Test
    public void testNonClientIdFromStorageIsNotDetected() {
        boolean detected =
                consentIdDetector.detectVariableFromStorage(
                        variableStore, "dummyKey", "dummyValue");

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.CONSENT_ID).orElse(null));
    }
}
