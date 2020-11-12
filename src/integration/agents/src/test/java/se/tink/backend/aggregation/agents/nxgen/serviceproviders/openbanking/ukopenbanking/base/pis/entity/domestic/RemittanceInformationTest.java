package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Test
    public void testShouldSerializeBothUnstructuredAndReferenceFields() throws IOException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String value = "TEST_MESSAGE";
        RemittanceInformation remittanceInformation =
                RemittanceInformation.ofUnstructuredAndReference(value);

        // Act
        String serializedRemittanceInformation =
                objectMapper.writeValueAsString(remittanceInformation);

        // Assert
        Pattern pattern = Pattern.compile(value);
        Matcher matcher = pattern.matcher(serializedRemittanceInformation);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        Assert.assertEquals(2, count);
        Assert.assertTrue(serializedRemittanceInformation.contains("Reference"));
        Assert.assertTrue(serializedRemittanceInformation.contains("Unstructured"));
    }
}
