package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import com.google.common.base.Charsets;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MobileBankingEncryptionHelper;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.strings.StringUtils;

/* renamed from: com.danskebank.core.h.e.a.e */
public class C0576e extends C0575g {
    private static final Base64 BASE64_CODEC = new Base64();
    private Credentials credentials;

    public C0576e(Credentials credentials) {
        this.credentials = credentials;
    }

    public byte[] m2106a() {
        // FH
        /*
         * try { return C0567e.m2093b(C0411a.m953t().getApplicationContext()).getBytes("UTF-8"); } catch
         * (UnsupportedEncodingException e) { C2797e.m12056d("Encryption", e.getStackTrace().toString()); return null; }
         */

        String codeKey = "CA4jCnwGUzMrPT5zKw0rGHw7DCktKwwzMQceG3QvJhY/PCh0DwQlDBB/Ii1+AQE6MBE5MQMDbEE2KD5XRV9QI2dfNBwMZTQocDMIHH4AHA8OMBEWRzV1dgMuO3cINhVAQiEIAxJyPhFGCxg8YQkIe3YPBlUCPCw1MFIFM3EnMy4iUDpeYQUOXB5jGAFwECY8OyU/Al8UF3d7WyJBFFYuRWpxMW0bKgcWfSstCzQFJDEDRScqKxc0MRA0dRgVGSwWLXB5KTYhGjNRACAfFHItJ1sFCjx5MxMeAxRj";
        String iconKey = "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA+xpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1wTU06T3JpZ2luYWxEb2N1bWVudElEPSJ1dWlkOjlFM0U1QzlBOEM4MURCMTE4NzM0REI1OEZEREU0QkE3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjE3NzhFOEI2Qzg4MzExREY5RUVDQ0ZBMkY0NDBBMUZEIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjE3NzhFOEI1Qzg4MzExREY5RUVDQ0ZBMkY0NDBBMUZEIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzUgTWFjaW50b3NoIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6ODRFRUJEQkRCRjI0NjgxMThGNjJEREIwMzAzMDgzREUiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MDI4MDExNzQw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";

        byte[] firstKey = BASE64_CODEC
                .decode(codeKey);
        byte[] secondKey = MobileBankingEncryptionHelper.reverse(
                iconKey.substring(0, firstKey.length)).getBytes(
                Charsets.US_ASCII);

        byte[] finalKey = MobileBankingEncryptionHelper.xor(firstKey, secondKey);

        return finalKey;
    }

    // "sg_prod_key_identify"
    public String m2107b() {
        return "TRIFORKP00000010";
    }

    // public String m2119c() {
    // "server_sg_country" / "server_sg_language"
    public String m2108c() {
        return "SE";
    }

    // public String m2120d() {
    // "server_sg_country" / "server_sg_language"
    public String m2109d() {
        return "SE";
    }

    // public String m2121e() {
    // "server_sg_brand_id"
    public String m2110e() {
        return "OEB";
    }

    // public String m2122f() {
    public String m2111f() {
        return StringUtils.hashAsUUID("TINK-" + credentials.getUsername());
        // TODO Fix persistent
        //return "a2d48b4c-fa0c-4be4-966f-01182f9873ab"; // FH Generated UUID for Installation
                                                       // C2792b.m12031a(C0411a.m953t());
    }

    // public String m2123g() {
    public String m2112g() {
        return "Mobilbank";
    }

    public String[] m2113h() {
        String str = "APP";
        String str2 = "M";
        String str3 = "%1$s/%2$s (%3$s; %4$s; %5$s; %6$s; %7$s) %8$s/%9$s";
        Object[] objArr = new Object[9];
        objArr[0] = m2112g();
        objArr[1] = m2114i();
        objArr[2] = str;
        objArr[3] = m2115l() ? "DebugDevice" : m2127k();
        objArr[4] = "Android";
        objArr[5] = m2126j();
        objArr[6] = m2109d().toUpperCase();
        objArr[7] = str2;
        objArr[8] = m2111f();
        str = String.format(str3, objArr);
        return new String[] {
                "User-Agent", str
        };
    }
    
    //public String m2125i() {    
    public String m2114i() {
        return "3.10"; // FH strings C0411a.m953t().getString(C0726m.release_info);
    }

  //  public String m2126j() {
    public String m2126j() {
        return "6.0.1"; // FH BUILD VERSION.RELEASE ad.m955b().m12030c();
    }

    // public String m2127k() {
    public String m2127k() {
        String a = "Nexus 5X"; // FH BUILD.MODEL ad.m955b().m12028a();
        if (a != null) {
            return a.replace("(", "").replace(")", "").replace(".", "").replace(",", "").replace(";", "")
                    .replace("/", "").replace("\\", "");
        }
        return a;
    }

    public boolean m2115l() {
        return false;
    }
}
