package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

/* renamed from: com.trifork.android.a.a.h */
public class C2805h {
    private final C0575g f10331a;
    private final String f10332b;
    private String f10333c;

    private static String m12080a(C0575g c0575g, String str) {
        String c = c0575g.m2108c();
        String d = c0575g.m2109d();
        String e = c0575g.m2110e();
        return String.format(
                "%1$s_%2$s_%3$s_%4$s", new Object[] {str, e, c.toUpperCase(), d.toUpperCase()});
    }

    public C2805h(C0575g c0575g, String str) {
        this.f10331a = c0575g;
        this.f10332b = str;
        this.f10333c = C2809b.m12102a();
    }

    private String m12081e() {
        String a = C2805h.m12080a(this.f10331a, this.f10332b);
        String f = this.f10331a.m2111f();
        String g = this.f10331a.m2112g();
        String i = this.f10331a.m2114i();
        g = String.format("%1s/%2s", new Object[] {g, i});
        byte[] bytes = f.getBytes();
        byte[] bytes2 = g.getBytes();
        f = C2809b.m12106a(bytes);
        g = C2809b.m12106a(bytes2);
        return String.format("%1$s$%2$s$%3$s$%4$s", new Object[] {a, this.f10333c, f, g});
    }

    public String m12083a(C2807b c2807b) {
        String e = m12081e();
        String a = C2809b.m12106a(c2807b.m12097a());
        e = String.format("%1$s$%2$s$%3$s", new Object[] {e, "ICR", a});
        if (this.f10331a.m2115l()) {
            System.out.println("tokenMessage: " + e);
        }
        return e;
    }

    public byte[] m12086b(C2807b c2807b) {
        return m12083a(c2807b).getBytes();
    }

    public String m12084a(byte[] bArr) {
        String e = m12081e();
        String b = C2799a.m12063b(bArr, 0);
        e = String.format("%1$s$%2$s$%3$s", new Object[] {e, "SSO", b});
        if (this.f10331a.m2115l()) {
            System.out.println("tokenMessage: " + e);
        }
        return e;
    }

    public byte[] m12087b(byte[] bArr) {
        return m12084a(bArr).getBytes();
    }

    public String m12082a() {
        String e = m12081e();
        String m = this.f10331a.m2116m();
        return String.format("%1$s$%2$s$%3$s", new Object[] {e, "I00", m});
    }

    public byte[] m12085b() {
        return m12082a().getBytes();
    }

    public String m12088c() {
        String e = m12081e();
        String m = this.f10331a.m2116m();
        return String.format("%1$s$%2$s$%3$s", new Object[] {e, "S01", m});
    }

    public byte[] m12089d() {
        return m12088c().getBytes();
    }
}
