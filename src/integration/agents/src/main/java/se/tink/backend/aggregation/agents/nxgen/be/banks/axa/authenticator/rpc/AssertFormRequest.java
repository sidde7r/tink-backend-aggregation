package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AssertRequestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;

public class AssertFormRequest extends BaseRequest<AssertRequestData> {

    private AssertFormRequest(AssertRequestData data, List<HeaderEntity> headers) {
        super(data, headers);
    }

    public static AssertFormRequest createUCRAssertFormRequest(
            String assertionId, String fch, String uid) {
        AssertRequestData data = AssertRequestData.createUCRAssertData(assertionId, fch);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createCardNumberAssertFormRequest(
            String assertionId, String fch, String cardNumber, String uid) {
        AssertRequestData data =
                AssertRequestData.createCardNumberAssertData(assertionId, fch, cardNumber);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createOtpAssertAuthenticationRequest(
            String assertionId, String fch, String cardNumber, String otp, String uid) {
        AssertRequestData data =
                AssertRequestData.createOtpAssertData(assertionId, fch, cardNumber, otp);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createProfileNameAssertFormRequest(
            String assertionId, String fch, String uid, String firstName) {
        AssertRequestData data =
                AssertRequestData.createProfileNameAssertData(assertionId, fch, firstName);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createAssertRegistrationRequest(
            String assertionId, String signedFch, String ecRawPublicKey, String uid) {
        AssertRequestData data =
                AssertRequestData.createAssertRegistrationRequest(
                        assertionId, signedFch, ecRawPublicKey);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createAssertConfirmationRequest(
            String assertionId, String fch, String payload, String signedPayload, String uid) {
        AssertRequestData data =
                AssertRequestData.createAssertConfirmationData(
                        assertionId, fch, payload, signedPayload);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    public static AssertFormRequest createAssertAuthenticationRequest(
            String assertionId, String signedFch, String uid) {
        AssertRequestData data =
                AssertRequestData.createAssertAuthenticationData(assertionId, signedFch);
        return new AssertFormRequest(data, createHeaders(uid));
    }

    private static List<HeaderEntity> createHeaders(String uid) {
        return Collections.singletonList(new HeaderEntity(uid));
    }
}
