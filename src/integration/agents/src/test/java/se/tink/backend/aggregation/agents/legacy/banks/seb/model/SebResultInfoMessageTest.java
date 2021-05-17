package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class SebResultInfoMessageTest {

    @Test
    public void testSebResultInfoMessage() {
        String source =
                "{\n"
                        + "          \"TableName\": null,\n"
                        + "          \"ErrorRowId\": 0,\n"
                        + "          \"ErrorColumnName\": \"BETAL_DATUM       \",\n"
                        + "          \"Level\": \"2\",\n"
                        + "          \"ErrorCode\": \"PCB046H\",\n"
                        + "          \"ErrorText\": \"Datumet då pengarna ska nå mottagaren ligger för nära i tiden. Välj ett senare datum.                                             \"\n"
                        + "        },\n"
                        + "        {\n"
                        + "          \"TableName\": null,\n"
                        + "          \"ErrorRowId\": 0,\n"
                        + "          \"ErrorColumnName\": \"                  \",\n"
                        + "          \"Level\": \"2\",\n"
                        + "          \"ErrorCode\": \"PCB046M\",\n"
                        + "          \"ErrorText\": \"Du kan läsa om bryttiderna under Hjälp.                                                                                           \"\n"
                        + "        }";
        ResultInfoMessage resultInfoMessage =
                SerializationUtils.deserializeFromString(source, ResultInfoMessage.class);
        Assert.assertEquals("PCB046H", resultInfoMessage.getErrorCode());
    }

    @Test
    public void testSignableStatus() {
        String source =
                "{\n"
                        + "  \"TableName\": null,\n"
                        + "  \"ErrorRowId\": 0,\n"
                        + "  \"ErrorColumnName\": \"KONTO_NR          \",\n"
                        + "  \"Level\": \"2\",\n"
                        + "  \"ErrorCode\": \"PCB03H6\",\n"
                        + "  \"ErrorText\": \"Bankgirokontot accepterar inte OCR-nummer. Skriv in valfritt meddelande i mottagarmeddelande under meddelandetyp.                 \"\n"
                        + "}";
        ResultInfoMessage resultInfoMessage =
                SerializationUtils.deserializeFromString(source, ResultInfoMessage.class);

        try {
            resultInfoMessage.abortTransferAndThrow();
        } catch (TransferExecutionException e) {
            Assert.assertEquals(
                    SignableOperationStatuses.CANCELLED, e.getSignableOperationStatus());
        }
    }
}
