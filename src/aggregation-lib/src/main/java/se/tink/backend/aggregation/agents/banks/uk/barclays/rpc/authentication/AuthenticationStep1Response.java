package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationStep1Response extends Response {
    /*
    {
        "qea": "3059301306072a8648ce3d020106082a8648ce3d030107034200041137fb8eb80e85d0f5beb0aee1c13a366b546592e63404049bc538b1da95d6690b7e0a8c9b58be9025a36d50ceaeb22967c8d0a6452c51da73a744b192ec1816",
        "registeredServices": null,
        "qeb": "3059301306072a8648ce3d020106082a8648ce3d03010703420004f8de7141f8f1cbaddfd6b0b931240847c5029e44fa16631e8290156e85665dec77fcb13ceeb8d7d7f19e9f9653f0d4c08df5d5dd4822b34166990628af95379d",
        "op_msg": null,
        "appServerSeed": "b6fd03e82376798f353fcba0fc59dc24835a0e4f3a98c633d45e9554f2b5a919",
        "op_status": "00000",
        "op_gsn": "635p-01-04",
        "isFeatureOnHighRisk": false,
        "re": "9219461f93cb906825322197f937b864500b252f414d843e33cc0b648b5bce5a",
        "sid": "3623651614427014",
        "deviceRegistrationStatus": 0
    }
     */
    private String qea;
    private String qeb;
    private String appServerSeed;
    private String sid;

    public String getQea() {
        return qea;
    }

    public void setQea(String qea) {
        this.qea = qea;
    }

    public byte[] getQeb() {
        try {
            return Hex.decodeHex(qeb.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setQeb(String qeb) {
        this.qeb = qeb;
    }

    public String getAppServerSeed() {
        return appServerSeed;
    }

    public void setAppServerSeed(String appServerSeed) {
        this.appServerSeed = appServerSeed;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}

