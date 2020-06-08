package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import io.vavr.control.Either;
import java.io.File;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms.MultiFactorSmsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class N26AuthenticatorTestData {

    static final String ACCESS_TOKEN = "9addde4c-01a2-4a19-83f1-33d33d60735c";
    static final String REFRESH_TOKEN = "44fa311c-2e00-4a9e-aac1-677a889b9be5";

    private static final String authenticationResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/authentication_response.json";
    private static final String multifactorSmsResponseFilePath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26/resources/multifactor_sms_response.json";

    static Either<ErrorResponse, AuthenticationResponse> getTokenResponse() {
        AuthenticationResponse response =
                SerializationUtils.deserializeFromString(
                        new File(authenticationResponseFilePath), AuthenticationResponse.class);
        return Either.right(response);
    }

    static MultiFactorSmsResponse getInitSms2faResponse() {
        return SerializationUtils.deserializeFromString(
                new File(multifactorSmsResponseFilePath), MultiFactorSmsResponse.class);
    }
}
