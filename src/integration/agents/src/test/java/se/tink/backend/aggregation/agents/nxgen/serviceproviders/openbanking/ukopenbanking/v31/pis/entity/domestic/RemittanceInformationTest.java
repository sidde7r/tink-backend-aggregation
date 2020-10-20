package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class RemittanceInformationTest {
    @Test
    public void testShouldSerializeUnstructuredFieldOnly() throws IOException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String unstructedRemittanceInformation = "TEST_MESSAGE";
        RemittanceInformation remittanceInformation =
                RemittanceInformation.ofUnstructured(unstructedRemittanceInformation);

        // Act
        String serializedRemittanceInformation =
                objectMapper.writeValueAsString(remittanceInformation);

        // Assert
        Assert.assertTrue(
                serializedRemittanceInformation.contains(unstructedRemittanceInformation));
        Assert.assertFalse(serializedRemittanceInformation.contains("Reference"));
    }
}
