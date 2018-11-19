package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

public class AuthResponse {
    private String distId;
    private String authProcId;
    private String smid;
    private String response;
    private String agreementId;
    private String cardNumber;
    private String authenticationMeanId;
    private String challenge;


    /*
    AUTH=
        <DIST_ID>49FB001</DIST_ID>
    <AUTH_PROC_ID>KiCtc3tA0Ko2RVHOUs5luKf</AUTH_PROC_ID>
    <MEAN_ID>UCR</MEAN_ID>
    <EAI_AUTH_TYPE>UCR</EAI_AUTH_TYPE>
    <EBANKING_USER_ID>
        <PERS_ID>12345678910000007</PERS_ID>
        <SMID>


        1880625810</SMID>
        <AGRE_ID>E3749089</AGRE_ID>
    </EBANKING_USER_ID>
    <EBANKING_USER_AUTHENTICITY_VALIDATION>
        <VALIDATION_DATE />
        <VALID />
        <AUTHENTICATION_MEAN_ID>

        08

        </AUTHENTICATION_MEAN_ID></EBANKING_USER_AUTHENTICITY_VALIDATION><CHALLENGE_RESPONSE><VALUE>

        02867281

        </VALUE><CHALLENGE>

        27565597

        </CHALLENGE><AUTH_FACTOR_ID>

        67030416073389570

        </AUTH_FACTOR_ID></CHALLENGE_RESPONSE><DEVICE_ID><FINGER_PRINT>873AF0C8042D980D7465A7F85F115B231BB6</FINGER_PRINT><NAME>iPhone</NAME></DEVICE_ID>
     */




    public static Builder builder() {
        return new Builder();
    }

    public static String xmlBuilder(AuthResponse authResponse) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("AUTH=");
        xmlBuilder.append("<DIST_ID>");
        xmlBuilder.append(authResponse.distId);
        xmlBuilder.append("</DIST_ID><AUTH_PROC_ID>");
        xmlBuilder.append(authResponse.authProcId);
        xmlBuilder.append("</AUTH_PROC_ID><MEAN_ID>UCR</MEAN_ID><EAI_AUTH_TYPE>UCR</EAI_AUTH_TYPE><EBANKING_USER_ID><PERS_ID>12345678910000007</PERS_ID><SMID>");
        xmlBuilder.append(authResponse.smid);
        xmlBuilder.append("</SMID><AGRE_ID>");
        xmlBuilder.append(authResponse.agreementId);
        xmlBuilder.append("</AGRE_ID></EBANKING_USER_ID><EBANKING_USER_AUTHENTICITY_VALIDATION><VALIDATION_DATE /><VALID /><AUTHENTICATION_MEAN_ID>");
        xmlBuilder.append(authResponse.authenticationMeanId);
        xmlBuilder.append("</AUTHENTICATION_MEAN_ID></EBANKING_USER_AUTHENTICITY_VALIDATION><CHALLENGE_RESPONSE><VALUE>");
        xmlBuilder.append(authResponse.response);
        xmlBuilder.append("</VALUE><CHALLENGE>");
        xmlBuilder.append(authResponse.challenge);
        xmlBuilder.append("</CHALLENGE><AUTH_FACTOR_ID>");
        xmlBuilder.append(authResponse.cardNumber);

        return xmlBuilder.toString();
    }

    public static final class Builder {

        private String distId;
        private String authProcId;
        private String smid;
        private String agreementId;
        private String challenge;
        private String response;
        private String authenticationMeanId;
        private String cardNumber;

        public Builder withDistId(String distId) {
            this.distId = distId;
            return this;
        }

        public Builder withAuthProcId(String authProcId) {
            this.authProcId = authProcId;
            return this;
        }

        public Builder withSmid(String smid) {
            this.smid = smid;
            return this;
        }
        public Builder withAgreementId(String agreementId) {
            this.agreementId = agreementId;
            return this;
        }
        public Builder withChallenge(String challenge) {
            this.challenge = challenge;
            return this;
        }
        public Builder withResponse(String response) {
            this.response = response;
            return this;
        }
        public Builder withAuthenticationMeanId(String authenticationMeanId) {
            this.authenticationMeanId = authenticationMeanId;
            return this;
        }
        public Builder withCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }


        public AuthResponse build() {

            AuthResponse authResponse = new AuthResponse();
            authResponse.distId = distId;
            authResponse.authProcId = authProcId;
            authResponse.smid = smid;
            authResponse.response = response;
            authResponse.agreementId = agreementId;
            authResponse.challenge = challenge;
            authResponse.authenticationMeanId = authenticationMeanId;
            authResponse.cardNumber = cardNumber;
            return authResponse;
        }
    }
}
