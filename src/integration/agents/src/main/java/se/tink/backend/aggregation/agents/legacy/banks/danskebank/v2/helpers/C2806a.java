package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* renamed from: com.trifork.android.a.b.a */
public class C2806a {
    public static byte[] m12092a(int i) {
        byte[] bArr = new byte[i];
        new SecureRandom().nextBytes(bArr);
        return bArr;
    }

    public static byte[] m12096a(byte[] bArr, byte[] bArr2) {
        try {
            KeyFactory instance = KeyFactory.getInstance("RSA");
            Cipher instance2 = Cipher.getInstance(C2806a.m12090a("UlNBL05vbmUvUEtDUzFQYWRkaW5n"));
            instance2.init(
                    1, instance.generatePublic(new X509EncodedKeySpec(C2799a.m12061a(bArr, 0))));
            return instance2.doFinal(bArr2);
        } catch (Throwable e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    public static byte[] m12094a(C2807b c2807b, byte[] bArr, byte[] bArr2) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            instance.init(
                    1,
                    new SecretKeySpec(c2807b.m12097a(), c2807b.m12098b()),
                    new IvParameterSpec(bArr2));
            return instance.doFinal(bArr);
        } catch (Throwable e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    public static byte[] m12095a(Key key, byte[] bArr, byte[] bArr2) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (bArr2 != null) {
                instance.init(2, key, new IvParameterSpec(bArr2));
            }
            return instance.doFinal(bArr);
        } catch (Throwable e) {
            throw new RuntimeException("Error decrypting", e);
        }
    }

    public static byte[] m12093a(C2807b c2807b, C2807b c2807b2) {
        try {
            Cipher instance = Cipher.getInstance("AESWrap");
            Key secretKeySpec = new SecretKeySpec(c2807b2.m12097a(), c2807b2.m12098b());
            Key secretKeySpec2 = new SecretKeySpec(c2807b.m12097a(), c2807b.m12098b());
            instance.init(3, secretKeySpec);
            return instance.wrap(secretKeySpec2);
        } catch (Throwable e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    public static Key m12091a(C2807b c2807b, byte[] bArr) {
        try {
            Cipher instance = Cipher.getInstance("AESWrap");
            instance.init(4, new SecretKeySpec(c2807b.m12097a(), c2807b.m12098b()));
            return instance.unwrap(bArr, "AES128", 3);
        } catch (Throwable e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    private static String m12090a(String str) {
        return new String(C2799a.m12060a(str, 0));
    }
}
