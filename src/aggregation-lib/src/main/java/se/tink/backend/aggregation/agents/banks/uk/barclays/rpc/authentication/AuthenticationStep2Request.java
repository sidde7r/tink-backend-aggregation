package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class AuthenticationStep2Request implements Request {
    /*
    deviceTime=1500573071848
    signature2=3044022043cbf0c9eb10e5a24f619cd40a7658840bb3b36044b45c78134e4dcb0df28a12022083158b9508c96c85334635aa17048b1ea190c5fabf1747d099198d3159415abd
    hasOTPSeed=N
    signature1=3046022100f3c838fc356e84d098a38a7f4a5ada6ea5537efdf382bcc5abf0e8b3a042d3e0022100d0ee3c72b0332e5bc7b4b4e080f8ff6cd9048443a8cac2c42275dc90bda1df53
    keyConfirmationMsg=cea064ab708ccc823931ebdc7270d1630c1106cf9bc6df27c558fd0ad43f8bf6f45e28aecd4b34b81a534e8d977580c2
     */

    private String signature1;
    private String signature2;
    private String keyConfirmationMsg;

    public String getCommandId() {
        return BarclaysConstants.COMMAND.AUTH_STEP2;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("deviceTime", getCurrentTime());
        m.put("signature2", signature2);
        m.put("hasOTPSeed", "N");
        m.put("signature1", signature1 + "\n");
        m.put("keyConfirmationMsg", keyConfirmationMsg);
        return m;
    }

    private String getCurrentTime() {
        Long now = Instant.now().toEpochMilli();
        return now.toString();
    }

    public void setSignature2(byte[] signature2) {
        this.signature2 = Hex.encodeHexString(signature2);
    }

    public void setSignature1(byte[] signature1) {
        this.signature1 = Hex.encodeHexString(signature1);
    }

    public void setKeyConfirmationMsg(byte[] keyConfirmationMsg) {
        this.keyConfirmationMsg = Hex.encodeHexString(keyConfirmationMsg);
    }
}
