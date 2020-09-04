package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator;

import java.util.Map;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AuthenticationTestData {
    public static final String OTP_SECRET_KEY =
            "GIYXQ53CGI2TC6DMM5TWK5RVNBTGC6DOORVG253CNRXWI23IOQ3G63TSN5VHEOLMPFWGYNRXNEZDIYTGMJXWUMJTHFUHO6RY";
    public static final String APP_ID = "appId";

    public static VerificationOnboardingResponse verificationOnboardingResponse(
            boolean onboardingRequired, boolean syncWalletRequired, String registerToken) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"header\": {\n"
                        + "        \"command-result\": \"0\",\n"
                        + "        \"clientId\": \"3796ac3e-afb9-4103-b289-f243d924aab5\",\n"
                        + "        \"command-result-description\": \"\",\n"
                        + "        \"requestId\": \"\",\n"
                        + "        \"requestid\": \"d8108e22-3ff6-4002-82bb-8f19e3a82ddc:node-1\",\n"
                        + "        \"command-result-details\": \"\",\n"
                        + "        \"command-request-version\": \"v1\",\n"
                        + "        \"command-request-service\": \"bancoposta\",\n"
                        + "        \"command-request-command\": \"verificaOnboarding\",\n"
                        + "        \"command-result-reason\": \"\",\n"
                        + "        \"status\": \"COMPLETED\"\n"
                        + "    },\n"
                        + "    \"body\": {\n"
                        + "        \"syncWalletRequired\": "
                        + syncWalletRequired
                        + ",\n"
                        + "        \"conti\": [\n"
                        + "            {\n"
                        + "                \"filiale\": \"1234567 \",\n"
                        + "                \"panMascheratoPostamat\": \"****1234567\",\n"
                        + "                \"numeroConto\": \"1234567\",\n"
                        + "                \"postamatAttivo\": true,\n"
                        + "                \"presenzaPCR\": false,\n"
                        + "                \"categoria\": \"1210 \",\n"
                        + "                \"utenteBloccato\": false,\n"
                        + "                \"aliasPostamat\": \"1234567\",\n"
                        + "                \"tipoConto\": \"C\",\n"
                        + "                \"attivo\": true\n"
                        + "            },\n"
                        + "            {\n"
                        + "                \"filiale\": \"1234567 \",\n"
                        + "                \"panMascheratoPostamat\": \"****1234567\",\n"
                        + "                \"numeroConto\": \"1234567\",\n"
                        + "                \"postamatAttivo\": true,\n"
                        + "                \"presenzaPCR\": false,\n"
                        + "                \"categoria\": \"1210 \",\n"
                        + "                \"utenteBloccato\": false,\n"
                        + "                \"aliasPostamat\": \"1234567\",\n"
                        + "                \"tipoConto\": \"C\",\n"
                        + "                \"attivo\": false\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"onboardingRequiredSkipTime\": 0,\n"
                        + "        \"onboardingRequired\": "
                        + onboardingRequired
                        + ",\n"
                        + "        \"postepay\": [],\n"
                        + "        \"arcotWorkMode\": false,\n"
                        + "        \"sh2LibSyncRequired\": false,\n"
                        + "        \"registerToken\": "
                        + registerToken
                        + ",\n"
                        + "        \"migrationRequired\": false,\n"
                        + "        \"libretti\": []\n"
                        + "    }\n"
                        + "}",
                VerificationOnboardingResponse.class);
    }

    public static Map<String, String> registerAppResponseWithPinError() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"command-success\": false,\n"
                        + "    \"command-error-code\": \"PIN-ERR-1\",\n"
                        + "    \"command-error-message\": \"User PIN creation required\",\n"
                        + "    \"command-result-type\": \"JSON\",\n"
                        + "    \"command-result\": {}\n"
                        + "}",
                Map.class);
    }

    public static Map<String, String> registerAppResponseWithMaxDeviceReachedError() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"command-success\": false,\n"
                        + "    \"command-error-code\": \"DEVICE-ERR-2\",\n"
                        + "    \"command-error-message\": \"Maximum limit devices reached\",\n"
                        + "    \"command-result-type\": \"JSON\",\n"
                        + "    \"command-result\": {}\n"
                        + "}",
                Map.class);
    }

    public static Map<String, String> registerAppResponseWithDefaultError() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"command-success\": false,\n"
                        + "    \"command-error-code\": \"UNKNOWN_CODE\",\n"
                        + "    \"command-error-message\": \"Message\",\n"
                        + "    \"command-result-type\": \"JSON\",\n"
                        + "    \"command-result\": {}\n"
                        + "}",
                Map.class);
    }
}
