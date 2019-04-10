package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import com.google.common.base.Preconditions;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DeviceToken {
    private static final String JWT_HEADER =
            Base64.encodeBase64URLSafeString(
                    SerializationUtils.serializeToString(new DeviceToken.Header())
                            .getBytes(SdcConstants.Jwt.UTF8));
    private static final String PERIOD = ".";

    private final SignKeys signKeys;
    private final ChallengeResponse challenge;
    private final String deviceId;

    public DeviceToken(ChallengeResponse challenge, SdcDevice device) {
        if (device == null || device.needsPinning()) {
            throw new IllegalStateException("No pinned deviced available");
        }
        this.challenge = challenge;
        this.deviceId = device.getSignedDeviceId();
        this.signKeys = device.getSignKeys();
    }

    public String signToken() {
        Payload payload = new Payload(this.challenge.getValue(), this.deviceId);
        byte[] jwtPayload =
                SerializationUtils.serializeToString(payload).getBytes(SdcConstants.Jwt.UTF8);

        String signable = JWT_HEADER + PERIOD + Base64.encodeBase64URLSafeString(jwtPayload);

        byte[] signature =
                RSA.signSha256(
                        this.signKeys.getPrivateKey(), signable.getBytes(SdcConstants.Jwt.UTF8));

        String sig = Base64.encodeBase64URLSafeString(signature);

        return signable + PERIOD + sig;
    }

    @JsonObject
    private static class Header {
        public String alg = SdcConstants.Jwt.JWT_ALGORITHM;
        public String typ = SdcConstants.Jwt.JWT_TYPE;

        public Header setTyp(String typ) {
            this.typ = typ;
            return this;
        }

        public Header setAlg(String alg) {
            this.alg = alg;
            return this;
        }
    }

    @JsonObject
    public static class Payload {
        private final long exp;
        private final long iat;
        private String challenge;
        private String device_id;

        public Payload(String challenge, String deviceId) {
            Preconditions.checkNotNull(challenge);
            Preconditions.checkNotNull(deviceId);

            // create a token valid for 3 minutes
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, SdcConstants.Jwt.JWT_EXPIRE_TIME);
            this.iat = new Date().toInstant().getEpochSecond();
            this.exp = calendar.getTime().toInstant().getEpochSecond();

            this.challenge = challenge;
            this.device_id = deviceId;
        }

        public long getExp() {
            return this.exp;
        }

        public long getIat() {
            return this.iat;
        }

        public String getChallenge() {
            return this.challenge;
        }

        public Payload setChallenge(String challenge) {
            this.challenge = challenge;
            return this;
        }

        public String getDevice_id() {
            return this.device_id;
        }

        public Payload setDevice_id(String device_id) {
            this.device_id = device_id;
            return this;
        }
    }
}
