package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import org.json.JSONException;


/* renamed from: com.trifork.android.a.a.f */
public class C2804f {
    private C2807b f10330a;

    public static String[] m12072a(C0575g c0575g) {
        return c0575g.m2113h();
    }

    public C2803e m12073a(C0575g c0575g, String str) {
        if (this.f10330a == null) {
            throw new IllegalStateException("cannot add authentication header before logon has been made (i.e a kek has been generated)");
        }
        byte[] a = C2806a.m12092a(16);
        C2808a c2808a = new C2808a("A128KW", "A128CBC", a, null);
        C2807b c2807b = new C2807b();
        C2803e c2803e = new C2803e(c2808a, C2806a.m12093a(c2807b, this.f10330a), C2806a.m12094a(c2807b, new C2805h(c0575g, str).m12089d(), a));
        m12071a(c0575g, c2808a, a, this.f10330a, c2807b);
        return c2803e;
    }

    public C2803e m12077b(C0575g c0575g, String str) {
        m12070a();
        byte[] a = C2806a.m12092a(16);
        C2808a c2808a = new C2808a("RSA1_5", "A128CBC", a, c0575g.m2107b()); // jweheader
        C2807b c2807b = new C2807b(); // symkey
        C2803e c2803e = new C2803e(c2808a, C2806a.m12096a(c0575g.m2106a(), c2807b.m12097a()), C2806a.m12094a(c2807b, new C2805h(c0575g, str).m12086b(this.f10330a), a));
        m12071a(c0575g, c2808a, a, this.f10330a, c2807b);
        return c2803e;
    }

    public C2803e m12074a(C0575g c0575g, String str, byte[] bArr) {
        m12070a();
        byte[] a = C2806a.m12092a(16);
        C2808a c2808a = new C2808a("A128KW", "A128CBC", a, null);
        byte[] b = new C2805h(c0575g, str).m12087b(bArr);
        C2807b c2807b = new C2807b();
        C2803e c2803e = new C2803e(c2808a, C2806a.m12093a(c2807b, this.f10330a), C2806a.m12094a(c2807b, b, a));
        m12071a(c0575g, c2808a, a, this.f10330a, c2807b);
        return c2803e;
    }

    public String m12076a(byte[] bArr) {
        m12070a();
        byte[] a = C2806a.m12092a(16);
        C2808a c2808a = new C2808a("A128KW", "A128CBC", a, null);
        C2807b c2807b = new C2807b();
        return new C2803e(c2808a, C2806a.m12093a(c2807b, this.f10330a), C2806a.m12094a(c2807b, bArr, a)).m12068a();
    }

    public String m12075a(String str) throws JSONException {
        String[] split = str.split("\\."); // FH TextUtils.split(str, "\\.");
        if (split.length != 3) {
            return null;
        }
        return C2809b.m12104a(C2806a.m12091a(this.f10330a, C2809b.m12111c(split[1])), C2809b.m12105a(C2809b.m12112d(split[0]), "iv"), split[2]);
    }

    public byte[] m12078b(String str) {
        if (this.f10330a == null) {
            throw new IllegalStateException("cannot decrypt authentication header before kek has been made (i.e a kek has been generated)");
        }
        return C2806a.m12095a(this.f10330a.m12099c(), C2799a.m12060a(str, 0), new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0});
    }

    private void m12070a() {
        if (this.f10330a == null) {
            this.f10330a = new C2807b();
        }
    }

    public C2803e m12079c(C0575g c0575g, String str) {
        byte[] a = C2806a.m12092a(16);
        C2808a c2808a = new C2808a("RSA1_5", "A128CBC", a, c0575g.m2107b());
        C2807b c2807b = new C2807b();
        C2803e c2803e = new C2803e(c2808a, C2806a.m12096a(c0575g.m2106a(), c2807b.m12097a()), C2806a.m12094a(c2807b, new C2805h(c0575g, str).m12085b(), a));
        m12071a(c0575g, c2808a, a, null, c2807b);
        return c2803e;
    }

    private void m12071a(C0575g c0575g, C2808a c2808a, byte[] bArr, C2807b c2807b, C2807b c2807b2) {
        // if (c0575g.m2115l()) {
        if (false) { // Just debugs stuff
            if (c2808a != null) {
                System.out.println("getSecurityResponse header: " + c2808a.m12100a());
            }
            if (c2808a != null) {
                System.out.println("getSecurityResponse iv: " + C2809b.m12110c(bArr));
            }
            if (c2807b != null) {
                System.out.println("getSecurityResponse kek: " + C2809b.m12110c(c2807b.m12097a()));
            }
            if (c2807b2 != null) {
                System.out.println("getSecurityResponse cek: " + C2809b.m12110c(c2807b2.m12097a()));
            }
        }
    }
}
