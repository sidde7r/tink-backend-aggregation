package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class C2809b {
    private static DateFormat f10338a;

    static {
        f10338a = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        f10338a.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String m12102a() {
        return f10338a.format(GregorianCalendar.getInstance().getTime());
    }

    public static String m12103a(String str) {
        return str.replaceAll("_", "/").replaceAll("-", "\\+");
    }

    public static String m12108b(String str) {
        return str.replaceAll("/", "_").replaceAll("\\+", "-");
    }

    public static String m12106a(byte[] bArr) {
        String str;
        byte[] encodeBase64 = Base64.encodeBase64(bArr);
        try {
            str = new String(encodeBase64, "UTF8");
        } catch (UnsupportedEncodingException e) {
            str = new String(encodeBase64);
        }
        return C2809b.m12108b(str).replaceAll("=", "");
    }

    public static String m12109b(byte[] bArr) {
        return C2809b.m12107a(bArr, 3);
    }

    public static String m12107a(byte[] bArr, int i) {
        String str;
        byte[] c = C2799a.m12065c(bArr, i);
        try {
            str = new String(c, "UTF8");
        } catch (UnsupportedEncodingException e) {
            str = new String(c);
        }
        return C2809b.m12108b(str);
    }

    public static byte[] m12111c(String str) {
        return C2799a.m12061a(C2809b.m12103a(str).getBytes(), 3);
    }

    public static String m12110c(byte[] bArr) {
        Formatter formatter = new Formatter();
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            formatter.format("%02x", new Object[] {Byte.valueOf(bArr[i])});
        }
        String formatter2 = formatter.toString();
        formatter.close();
        return formatter2;
    }

    public static JSONObject m12112d(String str) throws JSONException {
        String str2;
        byte[] c = C2809b.m12111c(str);
        try {
            str2 = new String(c, "UTF8");
        } catch (UnsupportedEncodingException e) {
            str2 = new String(c);
        }
        return new JSONObject(str2);
    }

    public static String m12105a(JSONObject jSONObject, String str) throws JSONException {
        return jSONObject.getString(str);
    }

    public static String m12104a(Key key, String str, String str2) {
        return new String(C2806a.m12095a(key, C2809b.m12111c(str2), C2809b.m12111c(str)));
    }
}
