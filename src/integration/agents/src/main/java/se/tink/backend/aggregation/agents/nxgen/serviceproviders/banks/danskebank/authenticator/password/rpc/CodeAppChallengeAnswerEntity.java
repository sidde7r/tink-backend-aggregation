package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppChallengeAnswerEntity {
    private String codeAppSerialNumber;
    private String response;
    private String signedResponse;
    private String codeAppIP;

    private CodeAppChallengeAnswerEntity(String codeAppSerialNumber, String response, String signedResponse,
            String codeAppIP) {
        this.codeAppSerialNumber = codeAppSerialNumber;
        this.response = response;
        this.signedResponse = signedResponse;
        this.codeAppIP = codeAppIP;
    }

    public static CodeAppChallengeAnswerEntity createFromPollResponse(PollCodeAppResponse response) {
        return new CodeAppChallengeAnswerEntity(
                response.getCodeAppSerialNumber(),
                response.getPayload().getResponse(),
                response.getPayload().getSignedResponse(),
                response.getCodeAppIP()
        );
    }
}
