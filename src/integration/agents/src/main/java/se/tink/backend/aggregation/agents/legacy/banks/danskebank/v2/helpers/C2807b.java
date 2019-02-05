package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/* renamed from: com.trifork.android.a.b.b */
public class C2807b {
    private final byte[] f10334a;
    private final SecretKey f10335b;

    public C2807b(int i) {
        try {
            KeyGenerator instance = KeyGenerator.getInstance(m12098b());
            instance.init(128);
            this.f10335b = instance.generateKey();
            this.f10334a = this.f10335b.getEncoded();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to generate symmetric key", e);
        }
    }

    public C2807b() {
        this(128);
    }

    public byte[] m12097a() {
        byte[] obj = new byte[this.f10334a.length];
        System.arraycopy(this.f10334a, 0, obj, 0, this.f10334a.length);
        return obj;
    }

    public String m12098b() {
        return "AES";
    }

    public SecretKey m12099c() {
        return this.f10335b;
    }
}
