package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AuthResponse {
    private String distId;
    private String authProcId;
    private String smid;
    private String response;
    private String agreementId;
    private String cardNumber;
    private String authenticationMeanId;
    private String challenge;
    private String deviceFingerprint;

    public static Builder builder() {
        return new Builder();
    }

    public String getUrlEncodedFormat() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<DIST_ID>");
        xmlBuilder.append(distId);
        xmlBuilder.append("</DIST_ID><AUTH_PROC_ID>");
        xmlBuilder.append(authProcId);
        xmlBuilder
                .append("</AUTH_PROC_ID><MEAN_ID>UCR</MEAN_ID><EAI_AUTH_TYPE>UCR</EAI_AUTH_TYPE><EBANKING_USER_ID><PERS_ID>12345678910000007</PERS_ID><SMID>");
        xmlBuilder.append(smid);
        xmlBuilder.append("</SMID><AGRE_ID>");
        xmlBuilder.append(agreementId);
        xmlBuilder
                .append("</AGRE_ID></EBANKING_USER_ID><EBANKING_USER_AUTHENTICITY_VALIDATION><VALIDATION_DATE></VALIDATION_DATE><VALID></VALID><AUTHENTICATION_MEAN_ID>");
        xmlBuilder.append(authenticationMeanId);
        xmlBuilder
                .append("</AUTHENTICATION_MEAN_ID></EBANKING_USER_AUTHENTICITY_VALIDATION><CHALLENGE_RESPONSE><VALUE>");
        xmlBuilder.append(response);
        xmlBuilder.append("</VALUE><CHALLENGE>");
        xmlBuilder.append(challenge);
        xmlBuilder.append("</CHALLENGE><AUTH_FACTOR_ID>");
        xmlBuilder.append(cardNumber);
        xmlBuilder.append("</AUTH_FACTOR_ID>");
        xmlBuilder.append("</CHALLENGE_RESPONSE>");
        xmlBuilder.append("<DEVICE_ID>");
        xmlBuilder.append("<FINGER_PRINT>" + deviceFingerprint + "</FINGER_PRINT>");
        xmlBuilder.append("<NAME>iPhone</NAME>");
        xmlBuilder.append("</DEVICE_ID>");
        String right = xmlBuilder.toString();
        try {
            right = URLEncoder.encode(URLEncoder.encode(right, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return "AUTH=" + right;
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
        private String deviceFingerprint;

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

        public Builder withDeviceFingerprint(String fingerprint) {
            this.deviceFingerprint = fingerprint;
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
            authResponse.deviceFingerprint = deviceFingerprint;
            return authResponse;
        }
    }
}
