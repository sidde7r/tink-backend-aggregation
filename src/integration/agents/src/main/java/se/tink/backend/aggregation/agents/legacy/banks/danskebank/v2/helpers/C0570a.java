package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

/* renamed from: com.danskebank.core.h.e.a.a */
public enum C0570a {
    REI(false, "REI"),
    GLA(false, "GLA"),
    RES(true, "RES"),
    COS(true, "COS"),
    NSP(true, "GLA"),
    NSS(true, "NSS"),
    MHS(true, "MHS");

    private final boolean f1552h;
    private final String f1553i;

    private C0570a(boolean z, String str) {
        this.f1552h = z;
        this.f1553i = str;
    }

    public boolean m2097a() {
        return this.f1552h;
    }

    public String m2098b() {
        return this.f1553i;
    }
}
