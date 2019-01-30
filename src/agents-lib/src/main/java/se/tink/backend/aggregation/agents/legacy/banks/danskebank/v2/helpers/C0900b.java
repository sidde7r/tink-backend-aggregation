package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import org.json.JSONException;

import org.json.JSONObject;

/* renamed from: com.danskebank.core.models.r.b */
public class C0900b {
    private String f3562a;
    private String f3563b;
    private String f3564c;
    private String f3565d;
    private String f3566e;

    protected C0900b(String str, String str2, String str3, String str4, String str5) {
        this.f3562a = str;
        this.f3563b = str2;
        this.f3564c = str3;
        this.f3565d = str4;
        this.f3566e = str5;
    }

    public static C0900b m4164a(JSONObject jSONObject) throws JSONException {
        JSONObject jSONObject2 = jSONObject.getJSONObject("Output").getJSONObject("StaticOutput").getJSONObject("ModuleOutput");
        return new C0900b(C0714l.m3213b(jSONObject2, "AutoStartToken"), C0714l.m3213b(jSONObject2, "OrderReference"), C0714l.m3213b(jSONObject2, "BankIDStatusCode"), C0714l.m3213b(jSONObject2, "BankIDStatusText"), C0714l.m3213b(jSONObject2, "BankIDToken"));
    }

    public String m4165b() {
        return this.f3562a;
    }

    public String m4166c() {
        return this.f3563b;
    }

    public String m4167d() {
        return this.f3564c;
    }

    public String m4168e() {
        return this.f3565d;
    }

    public String m4169f() {
        return this.f3566e;
    }
}
