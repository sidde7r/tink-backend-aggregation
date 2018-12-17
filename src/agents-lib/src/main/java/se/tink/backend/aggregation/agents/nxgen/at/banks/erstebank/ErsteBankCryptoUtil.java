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
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.EncryptionValuesEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ErsteBankCryptoUtil {

    public static TokenEntity getTokenFromResponse(HttpResponse response) throws LoginException {
        String location = response.getHeaders().getFirst(ErsteBankConstants.LOCATION);
        String accessToken = matchOrThrow(ErsteBankConstants.PATTERN.ACCESS_TOKEN, location);
        String tokenType = matchOrThrow(ErsteBankConstants.PATTERN.TOKEN_TYPE, location);
        String expiresIn = matchOrThrow(ErsteBankConstants.PATTERN.EXPIRES_IN, location);

        return new TokenEntity(accessToken, tokenType, expiresIn);
    }

    public static EncryptionValuesEntity getEncryptionValues(String html) throws LoginException {
        String salt = matchOrThrow(ErsteBankConstants.PATTERN.SALT, html);
        String exponent = matchOrThrow(ErsteBankConstants.PATTERN.EXPONENT, html);
        String modulus = matchOrThrow(ErsteBankConstants.PATTERN.MODULUS, html);

        return new EncryptionValuesEntity(salt, exponent, modulus);
    }

    private static String matchOrThrow(Pattern pattern, String html) throws LoginException {
        Matcher matcher = pattern.matcher(html);
        if(matcher.find()){
            return matcher.group(1);
        }

        throw LoginError.NOT_SUPPORTED.exception();
    }

    public static String getSidentityCode(String html) throws LoginException {
        return matchOrThrow(ErsteBankConstants.PATTERN.SIDENTITY_VERIFICATION_CODE, html);
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
                .getInstance(ErsteBankConstants.ENCRYPTION.RSA).generatePublic((KeySpec)(new RSAPublicKeySpec(new BigInteger(privateExponent, 16), new BigInteger(publicModulus, 16))));
        Cipher cipher = Cipher.getInstance(ErsteBankConstants.ENCRYPTION.RSA);
        cipher.init(Cipher.ENCRYPT_MODE, (Key)publicKey);

        byte[] var6 = var1.getBytes(StandardCharsets.UTF_8);

        return shift(cipher.doFinal(var6));
    }

    private static final String shift(byte[] var1) {
        int var2 = 0;
        char[] var0 = ErsteBankConstants.ENCRYPTION.HEX_DIGITS.toCharArray();

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
