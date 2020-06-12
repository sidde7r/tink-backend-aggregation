package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class SebResultInfoMessageTest {

    @Test
    public void testSebResultInfoMessage() throws IOException {
        String source =
                "{\"TableName\":null,\"ErrorRowId\":0,\"ErrorColumnName\":\"BETAL_DATUM       \",\"Level\":\"2\",\"ErrorCode\":\"PCB046H\",\"ErrorText\":\"Datumet då pengarna ska nå mottagaren ligger för nära i tiden. Välj ett senare datum.                                             \"},{\"TableName\":null,\"ErrorRowId\":0,\"ErrorColumnName\":\"                  \",\"Level\":\"2\",\"ErrorCode\":\"PCB046M\",\"ErrorText\":\"Du kan läsa om bryttiderna under Hjälp.                                                                                           \"}],\"HostReturnCode\":2000,\"HostReturnCodeText\":\"\",\"ResponseTimes\":{\"QueueTime\":0,\"ExecuteTime\":0,\"HostTime\":0,\"GateWayTime\":0,\"HTTPServerTime\":138},\"MiddlewareReport\":{\"HttpServer\":\"A3\",\"TGWInstance\":null,\"IMS\":null,\"AS400\":null},\"CurrentTic\":\"20060819:57:20.8726025\"},\"VODB\":{\"DBZV170\":null,\"DBZV160\":null,\"PCBW5211\":{\"ROW_ID\":0,\"UPPDRAG_BESKRIV\":\"\",\"SEB_KUND_NR\":\"81080300760009\",\"KONTO_NR\":\"53830351976\",\"MOTT_KONTO_NR\":\"4624292\",\"MOTT_KONTO_TYP\":\"BG\",\"BETAL_DATUM\":\"2020-06-08\",\"UPPDRAG_BEL\":2053.54,\"MOTTAGAR_INFO\":\"184551074005653\",\"KK_TXT\":\"\",\"MEDD_TXT1\":null,\"MEDD_TXT2\":null,\"MEDD_TXT3\":null,\"MEDD_TXT4\":null,\"NOTERING\":\"Krav Billecta\",\"MOTTAGAR_REG_FL\":\"\",\"FAKTURA_ID\":null,\"BEL_ANDR_KOD\":null,\"SUTI\":null},\"PCBW1241\":null,\"PCBW1242\":null,\"PCBW1243\":null}}}";
        ObjectMapper objectMapper = new ObjectMapper();
        ResultInfoMessage resultInfoMessage =
                objectMapper.readValue(source, ResultInfoMessage.class);
        Assert.assertEquals("PCB046H", resultInfoMessage.getErrorCode());
    }
}
