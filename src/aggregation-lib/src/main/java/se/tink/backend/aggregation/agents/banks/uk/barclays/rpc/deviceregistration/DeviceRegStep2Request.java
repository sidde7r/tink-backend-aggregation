package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration;

import com.google.common.primitives.Bytes;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysCrypto;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class DeviceRegStep2Request implements Request {
    /*
    qsa=3059301306072a8648ce3d020106082a8648ce3d03010703420004001d53494089e3a2805355453b2ac4b423f09a7558ab8d14bcd30c448899200ca1fd1f0c5c305416136eadba7866a6ca1267d15fdcdd8465de656ae718c95a53
    qsd=3059301306072a8648ce3d020106082a8648ce3d0301070342000407418bf0862a978675564e7369b90ae5b8f791c832eddb702b54338f641467ef20aa32eb2c5d378236f9284dfd2ee8bce2c70a0c98ebbde87b493770d7e1b04d
    signature1=304402206de919386a455ae86908f3498fe1d60c7a76a421cefcba9a4abb33232bb745c702206065a9e4e03c2b4f5fdce26e91e07da5a8f8556a4b4b34c81932af28883915cf
    */
    private String qsa;
    private String qsd;
    private String signature1;

    public DeviceRegStep2Request(KeyPair qsa, KeyPair qsd) {
        // sign the two public keys with `qsa`
        byte[] qsaPubEncoded = qsa.getPublic().getEncoded();
        byte[] qsdPubEncoded = qsd.getPublic().getEncoded();
        byte[] sig = BarclaysCrypto.ecSignSha256(qsa, Bytes.concat(qsaPubEncoded, qsdPubEncoded));
        setSignature1(sig);
        setQsa(qsaPubEncoded);
        setQsd(qsdPubEncoded);
    }

    public String getCommandId() {
        return BarclaysConstants.COMMAND.DEVREG_STEP2;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("qsa", qsa);
        m.put("qsd", qsd);
        m.put("signature1", signature1);
        return m;
    }

    public void setQsa(byte[] qsa) {
        this.qsa = Hex.encodeHexString(qsa);
    }

    public void setQsd(byte[] qsd) {
        this.qsd = Hex.encodeHexString(qsd);
    }

    public void setSignature1(byte[] signature1) {
        this.signature1 = Hex.encodeHexString(signature1);
    }
}
