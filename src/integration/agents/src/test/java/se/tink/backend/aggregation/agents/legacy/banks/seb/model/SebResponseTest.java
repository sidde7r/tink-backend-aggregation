package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SebResponseTest {

    @Test
    public void sebResponseTest() {
        String source =
                "{\n"
                        + "  \"d\": {\n"
                        + "    \"__type\": \"SEB_CS.SEBCSService\",\n"
                        + "    \"ServiceInfo\": {\n"
                        + "      \"SEBCS_Service\": \"\",\n"
                        + "      \"minusL\": \"\",\n"
                        + "      \"Type\": \"WantToWorkHere-Call us\",\n"
                        + "      \"SMMVersion\": 0,\n"
                        + "      \"Timestamp\": null,\n"
                        + "      \"SubId\": 0,\n"
                        + "      \"CSBusinessObject\": \"\",\n"
                        + "      \"VersionStamp\": \"\",\n"
                        + "      \"SMMName\": \"\",\n"
                        + "      \"ClientType\": \"\",\n"
                        + "      \"PreviousElapseTime\": 0,\n"
                        + "      \"PreviousElapseTimeSpecified\": false,\n"
                        + "      \"CodePage\": \"\",\n"
                        + "      \"Accounting\": {\n"
                        + "        \"CorrelationID\": null,\n"
                        + "        \"Language\": \"\",\n"
                        + "        \"CountryCode\": null,\n"
                        + "        \"Channel\": null,\n"
                        + "        \"InvoiceHint\": \"\",\n"
                        + "        \"Brand\": \"\",\n"
                        + "        \"ParentReqId\": null,\n"
                        + "        \"CurrentReqId\": null,\n"
                        + "        \"ZipOptions\": null,\n"
                        + "        \"Saml\": null\n"
                        + "      },\n"
                        + "      \"TimeoutMillis\": 0,\n"
                        + "      \"TimeoutMillisSpecified\": false\n"
                        + "    },\n"
                        + "    \"ServiceInput\": null,\n"
                        + "    \"UserCredentials\": null,\n"
                        + "    \"ResultInfo\": {\n"
                        + "      \"GatewayReturnCode\": 0,\n"
                        + "      \"GatewayReturnCodeText\": \"\",\n"
                        + "      \"Message\": [\n"
                        + "        {\n"
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
                        + "        }\n"
                        + "      ],\n"
                        + "      \"HostReturnCode\": 2000,\n"
                        + "      \"HostReturnCodeText\": \"\",\n"
                        + "      \"ResponseTimes\": {\n"
                        + "        \"QueueTime\": 0,\n"
                        + "        \"ExecuteTime\": 0,\n"
                        + "        \"HostTime\": 0,\n"
                        + "        \"GateWayTime\": 0,\n"
                        + "        \"HTTPServerTime\": 138\n"
                        + "      },\n"
                        + "      \"MiddlewareReport\": {\n"
                        + "        \"HttpServer\": \"A3\",\n"
                        + "        \"TGWInstance\": null,\n"
                        + "        \"IMS\": null,\n"
                        + "        \"AS400\": null\n"
                        + "      },\n"
                        + "      \"CurrentTic\": \"20060819:57:20.8726025\"\n"
                        + "    },\n"
                        + "    \"VODB\": {\n"
                        + "      \"DBZV170\": null,\n"
                        + "      \"DBZV160\": null,\n"
                        + "      \"PCBW5211\": {\n"
                        + "        \"ROW_ID\": 0,\n"
                        + "        \"UPPDRAG_BESKRIV\": \"\",\n"
                        + "        \"SEB_KUND_NR\": \"***MASK***\",\n"
                        + "        \"KONTO_NR\": \"***MASK***\",\n"
                        + "        \"MOTT_KONTO_NR\": \"***MASK***\",\n"
                        + "        \"MOTT_KONTO_TYP\": \"BG\",\n"
                        + "        \"BETAL_DATUM\": \"2020-06-08\",\n"
                        + "        \"UPPDRAG_BEL\": 2053.54,\n"
                        + "        \"MOTTAGAR_INFO\": \"***MASK***\",\n"
                        + "        \"KK_TXT\": \"\",\n"
                        + "        \"MEDD_TXT1\": null,\n"
                        + "        \"MEDD_TXT2\": null,\n"
                        + "        \"MEDD_TXT3\": null,\n"
                        + "        \"MEDD_TXT4\": null,\n"
                        + "        \"NOTERING\": \"***MASK***\",\n"
                        + "        \"MOTTAGAR_REG_FL\": \"\",\n"
                        + "        \"FAKTURA_ID\": null,\n"
                        + "        \"BEL_ANDR_KOD\": null,\n"
                        + "        \"SUTI\": null\n"
                        + "      },\n"
                        + "      \"PCBW1241\": null,\n"
                        + "      \"PCBW1242\": null,\n"
                        + "      \"PCBW1243\": null\n"
                        + "    }\n"
                        + "  }\n"
                        + "}";
        SebResponse sebResponse =
                SerializationUtils.deserializeFromString(source, SebResponse.class);
        assertThat(
                        sebResponse
                                .getFirstErrorWithErrorText()
                                .orElseThrow(IllegalArgumentException::new)
                                .getErrorCode())
                .isEqualTo("PCB046H");
    }
}
