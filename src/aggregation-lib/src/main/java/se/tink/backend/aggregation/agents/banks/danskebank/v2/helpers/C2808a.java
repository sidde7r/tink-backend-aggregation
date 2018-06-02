package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import java.io.UnsupportedEncodingException;
import org.json.JSONObject;

public class C2808a {
    private final byte[] f10336a;
    private final JSONObject f10337b;

    public C2808a(String str, String str2, byte[] bArr, String str3) {
        this.f10336a = bArr;
        this.f10337b = new JSONObject();
        try {
            this.f10337b.put("alg", str);
            this.f10337b.put("enc", str2);
            this.f10337b.put("iv", C2809b.m12106a(this.f10336a));
            if (str3 != null) {
                this.f10337b.put("kid", str3);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create JWE", e);
        }
    }

    public JSONObject m12100a() {
        return this.f10337b;
    }

    public byte[] m12101b() {
        try {
            return this.f10337b.toString().getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            return this.f10337b.toString().getBytes();
        }
    }
}
