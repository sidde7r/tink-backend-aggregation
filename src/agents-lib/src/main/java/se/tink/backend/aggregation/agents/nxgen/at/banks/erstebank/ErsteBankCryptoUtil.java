package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.authenticator.entity.EncryptionValuesEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ErsteBankCryptoUtil {

    public static TokenEntity getTokenFromResponse(HttpResponse response){
        String location = response.getHeaders().getFirst("Location");
        String accessToken = match(ErsteBankConstants.PATTERN.ACCESS_TOKEN, location);
        String tokenType = match(ErsteBankConstants.PATTERN.TOKEN_TYPE, location);
        String expiresIn = match(ErsteBankConstants.PATTERN.EXPIRES_IN, location);

        return new TokenEntity(accessToken, tokenType, expiresIn);
    }

    public static EncryptionValuesEntity getEncryptionValues(String html){
        String salt = match(ErsteBankConstants.PATTERN.SALT, html);
        String exponent = match(ErsteBankConstants.PATTERN.EXPONENT, html);
        String modulus = match(ErsteBankConstants.PATTERN.MODULUS, html);

        return new EncryptionValuesEntity(salt, exponent, modulus);
    }

    private static String match(Pattern pattern, String html){
        Matcher matcher = pattern.matcher(html);
        if(matcher.find()){
            return matcher.group(1);
        }

        throw new IllegalStateException("Cannot match pattern: " + pattern.toString());
    }


    public static String getRSAPassword(String salt, String privateExponent, String publicModulus, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidKeySpecException {
        return getRsa(salt, privateExponent, publicModulus, password);
    }

    private static String getRsa(String salt, String privateExponent, String publicModulus, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
            BadPaddingException, IllegalBlockSizeException {

        StringBuilder builder = new StringBuilder();
        builder.append(salt);
        builder.append("\t");
        builder.append(password);

        String var1 = builder.toString();

        PublicKey publicKey = KeyFactory
                .getInstance("RSA").generatePublic((KeySpec)(new RSAPublicKeySpec(new BigInteger(privateExponent, 16), new BigInteger(publicModulus, 16))));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, (Key)publicKey);

        byte[] var6 = var1.getBytes(StandardCharsets.UTF_8);

        return shift(cipher.doFinal(var6));
    }

    private static final String shift(byte[] var1) {
        int var2 = 0;
        char[] var0 = "0123456789ABCDEF".toCharArray();

        char[] var6 = new char[var1.length * 2];

        for(int var3 = var1.length; var2 < var3; ++var2) {
            int var4 = var1[var2] & 255;
            int var5 = var2 * 2;
            var6[var5] = var0[var4 >>> 4];
            var6[var5 + 1] = var0[var4 & 15];
        }

        return new String(var6);
    }

}
