package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import java.util.HashMap;
import java.util.Map;

public class C2803e {
    private final C2808a f10327a;
    private final byte[] f10328b;
    private final byte[] f10329c;

    public C2803e(C2808a c2808a, byte[] bArr, byte[] bArr2) {
        this.f10327a = c2808a;
        this.f10328b = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.f10328b, 0, bArr.length);
        this.f10329c = new byte[bArr2.length];
        System.arraycopy(bArr2, 0, this.f10329c, 0, bArr2.length);
    }

    public String m12068a() {
        String a = C2809b.m12106a(this.f10327a.m12101b());
        String a2 = C2809b.m12106a(this.f10328b);
        String a3 = C2809b.m12106a(this.f10329c);
        return String.format("%1s.%2s.%3s", new Object[] {
                a, a2, a3
        });
    }

    public Map<String,String> m12069b() {
        Map<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("Authorization", m12068a());
        return hashMap;
    }
}
