package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.eac.CertificateBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants.Encryption;

/**
 * Utils used in legacy auto auth flow.
 *
 * <p>Delete after all credentials migrated to new flow
 */
@SuppressWarnings("all")
public class FortisUtils {

    public static String calculateChallenge(
            String muid, String password, String agreementId, String challenge, String processId) {

        return m3274(
                Encryption.LEGACY_OCRA_S064,
                m3277(muid, "4"),
                null,
                challenge,
                m3271(String.format("%s%s", password, agreementId)),
                m3268(processId),
                null);
    }

    private static String m3268(String str) {
        return m3275(str.getBytes());
    }

    private static String m3271(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bArr = str.getBytes(StandardCharsets.ISO_8859_1);
        messageDigest.update(bArr, 0, bArr.length);
        return m3275(messageDigest.digest());
    }

    private static Mac wlm3253(byte[] bArr) {
        Mac mac = null;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(wlm3249(bArr));
        } catch (Throwable e) {
        }
        return mac;
    }

    private static Key wlm3249(byte[] bArr) {
        byte[] bArr2 = bArr;
        if (bArr.length == 0) {
            bArr2 = new byte[32];
        }
        return new SecretKeySpec(bArr2, "HmacSHA256");
    }

    private static byte[] wlm3248(byte[] bArr, Mac mac) {
        mac.update(bArr);
        byte[] doFinal = mac.doFinal();
        mac.reset();
        return doFinal;
    }

    private static byte[] wlm3250(String str) {
        String str2 = str;
        if (str.length() % 2 == 1) {
            str2 = "0" + str;
        }
        byte[] bArr = new byte[(str2.length() / 2)];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) Integer.parseInt(str2.substring(i * 2, (i * 2) + 2), 16);
        }
        return bArr;
    }

    private static String m3275(byte[] bArr) {
        char[] toCharArray = "0123456789ABCDEF".toCharArray();
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = toCharArray[i2 >>> 4];
            cArr[(i * 2) + 1] = toCharArray[i2 & 15];
        }
        return new String(cArr);
    }

    private static String m3274(
            String str,
            String str2,
            String str3,
            String str4,
            String str5,
            String str6,
            String str7) {
        int[] iArr = new int[] {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
        String str8 = "";
        int length = str.getBytes().length;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        String str9 = str.split(":")[1];
        String str10 = str.split(":")[2];
        if (str9.toLowerCase().indexOf("sha1") > 1) {
            str8 = "HmacSHA1";
        }
        if (str9.toLowerCase().indexOf("sha256") > 1) {
            str8 = "HmacSHA256";
        }
        if (str9.toLowerCase().indexOf("sha512") > 1) {
            str8 = "HmacSHA512";
        }
        int intValue = Integer.decode(str9.substring(str9.lastIndexOf('-') + 1));
        if (str10.toLowerCase().startsWith("c")) {
            str3 = m3272(str3, 16, true);
            i = 8;
        }
        if (str10.toLowerCase().startsWith("q") || str10.toLowerCase().contains("-q")) {
            str4 = m3272(str4, 256, false);
            i2 = 128;
        }
        if (str10.toLowerCase().indexOf("psha1") > 1) {
            str5 = m3272(str5, 40, true);
            i3 = 20;
        }
        if (str10.toLowerCase().indexOf("psha256") > 1) {
            str5 = m3272(str5, 64, true);
            i3 = 32;
        }
        if (str10.toLowerCase().indexOf("psha512") > 1) {
            str5 = m3272(str5, 128, true);
            i3 = 64;
        }
        if (str10.toLowerCase().indexOf("s064") > 1) {
            str6 = m3272(str6, 128, true);
            i4 = 64;
        }
        if (str10.toLowerCase().indexOf("s128") > 1) {
            str6 = m3272(str6, 256, true);
            i4 = 128;
        }
        if (str10.toLowerCase().indexOf("s256") > 1) {
            str6 = m3272(str6, 512, true);
            i4 = 256;
        }
        if (str10.toLowerCase().indexOf("s512") > 1) {
            str6 = m3272(str6, 1024, true);
            i4 = 512;
        }
        if (str10.toLowerCase().startsWith("t") || str10.toLowerCase().indexOf("-t") > 1) {
            str7 = m3272(str7, 16, true);
            i5 = 8;
        }
        byte[] obj = new byte[((((((length + i) + i2) + i3) + i4) + i5) + 1)];
        byte[] bytes = str.getBytes();
        System.arraycopy(bytes, 0, obj, 0, bytes.length);
        obj[bytes.length] = (byte) 0;
        if (i > 0) {
            bytes = m3279(str3);
            System.arraycopy(bytes, 0, obj, length + 1, bytes.length);
        }
        if (i2 > 0) {
            bytes = m3279(str4);
            System.arraycopy(bytes, 0, obj, (length + 1) + i, bytes.length);
        }
        if (i3 > 0) {
            bytes = m3279(str5);
            System.arraycopy(bytes, 0, obj, ((length + 1) + i) + i2, bytes.length);
        }
        if (i4 > 0) {
            bytes = m3279(str6);
            System.arraycopy(bytes, 0, obj, (((length + 1) + i) + i2) + i3, bytes.length);
        }
        if (i5 > 0) {
            bytes = m3279(str7);
            System.arraycopy(bytes, 0, obj, ((((length + 1) + i) + i2) + i3) + i4, bytes.length);
        }
        byte[] arr = m3280(str8, m3279(str2), obj);
        int i6 = arr[arr.length - 1] & 15;
        return m3272(
                Integer.toString(
                        (((((arr[i6] & CertificateBody.profileType) << 24)
                                                        | ((arr[i6 + 1] & 255) << 16))
                                                | ((arr[i6 + 2] & 255) << 8))
                                        | (arr[i6 + 3] & 255))
                                % iArr[intValue]),
                intValue,
                true);
    }

    private static String m3272(String str, int i, boolean z) {
        StringBuilder stringBuilder = new StringBuilder(str);
        if (z) {
            String value = stringBuilder.toString();
            while (value.length() < i) {
                value = m3270(value);
            }
            stringBuilder = new StringBuilder(value);
        } else {
            while (stringBuilder.length() < i) {
                stringBuilder.append("0");
            }
        }
        return stringBuilder.toString();
    }

    private static byte[] m3279(String str) {
        byte[] toByteArray = new BigInteger("10" + str, 16).toByteArray();
        byte[] obj = new byte[(toByteArray.length - 1)];
        System.arraycopy(toByteArray, 1, obj, 0, obj.length);
        return obj;
    }

    private static String m3270(String str) {
        return "0" + str;
    }

    private static byte[] m3280(String str, byte[] bArr, byte[] bArr2) {
        try {
            Mac instance = Mac.getInstance(str);
            instance.init(new SecretKeySpec(bArr, "RAW"));
            return instance.doFinal(bArr2);
        } catch (Throwable e) {
            return null;
        }
    }

    private static byte[] wlm3254(byte[] bArr, byte[]... bArr2) {
        int length = bArr.length;
        for (byte[] length2 : bArr2) {
            length += length2.length;
        }
        byte[] copyOf = Arrays.copyOf(bArr, length);
        int length3 = bArr.length;
        for (byte[] obj : bArr2) {
            System.arraycopy(obj, 0, copyOf, length3, obj.length);
            length3 += obj.length;
        }
        return copyOf;
    }

    private static String wlm3252(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bArr) {
            String str = Integer.toHexString(b & 255);
            if (str.length() == 1) {
                stringBuilder.append("0").append(str);
            } else {
                stringBuilder.append(str);
            }
        }
        return stringBuilder.toString();
    }

    private static String m3277(String str, String str2) {
        String toUpperCase =
                wlm3252(wlm3251(Base64.getDecoder().decode(str), str2.getBytes(), 32))
                        .toUpperCase();
        if (toUpperCase.length() > 64) {
            return toUpperCase.substring(0, 64);
        }
        return toUpperCase;
    }

    private static byte[] wlm3251(byte[] bArr, byte[] bArr2, int i) {
        Mac mac = wlm3253(bArr);
        byte[] bArr3 = new byte[0];
        byte[] bArr4 = new byte[0];
        int ceil = (int) Math.ceil(((double) i) / 32.0d);
        for (int i2 = 0; i2 < ceil; i2++) {
            bArr4 = wlm3248(wlm3254(bArr4, bArr2, wlm3250(Integer.toHexString(i2 + 1))), mac);
            bArr3 = wlm3254(bArr3, bArr4);
        }
        return Arrays.copyOfRange(bArr3, 0, i);
    }
}
