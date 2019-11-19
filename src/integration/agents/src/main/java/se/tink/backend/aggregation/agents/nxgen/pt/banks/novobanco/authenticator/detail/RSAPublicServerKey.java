package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class RSAPublicServerKey {

    public PublicKey getKey() {
        try {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(getModulus(), getExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private BigInteger getExponent() {
        return new BigInteger("65537");
    }

    private BigInteger getModulus() {
        return new BigInteger(
                "21859154041351019418148875602634032830014743458199618077541386494"
                        + "080530136672189636386362401967247453492718482843129906108898809327833037582761065733192"
                        + "469207311301691262713882285370464063520971278472815755778367610857075313340027861163572"
                        + "826934392818021116362257229985555403490622249195270896484079657367214406718699740077124"
                        + "837200915232449006376121206522694791606829964887466630446999515682878112366422038955276"
                        + "769891088115109657842560824227882052439445658478817339686409631922014268940408209022732"
                        + "611100082017399945529840464715286964418137298458853715629444548696099622338261660080548"
                        + "337625860841604851383596743369");
    }
}
