package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class JWEAccessTokenConstants {
    public static final String TOKEN_KEY_ID =
            "A06EBED334D90E34CF92511EBE314E105862EC44CADF9E44787FBE9BDC3C285B";
    private static final BigInteger RSA_EXPONENT = BigInteger.valueOf(65537);
    private static final BigInteger MODULUS =
            new BigInteger(
                    "21744331546081361438243283574903001883280001819181248442218527290269704446006"
                            + "6635012914904126145962376702243144244370229727868109355780426477813656999843"
                            + "5224894844468664413976664163600118462815005830806246935626398784113723986777"
                            + "5655833117936385648910249837166073869869722571038230581222832272668446052808"
                            + "6102479621475827613144406471217530111212559987522974670157996320543773960034"
                            + "1102404766850988783192856343129984184208614736931477950154703179136147334184"
                            + "1585599243628757575279100934594687025085509337687783432919805210487794888852"
                            + "9124965525014955500933923804345232442235538211917738279686595480864055389111"
                            + "31452291");

    public static PublicKey getRSATokenPublicKey() {
        try {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(MODULUS, RSA_EXPONENT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
