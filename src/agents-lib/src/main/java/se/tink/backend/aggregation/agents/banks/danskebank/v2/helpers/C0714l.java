package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import org.json.JSONException;

import org.json.JSONObject;

public class C0714l {

    public static String m3213b(JSONObject jSONObject, String str) throws JSONException {
        if (!jSONObject.has(str) || jSONObject.isNull(str)) {
            return null;
        }
        return jSONObject.getString(str);
    }
    
    public static String m3198a(JSONObject jSONObject, String str, String str2) throws JSONException {
        if (!jSONObject.has(str) || jSONObject.isNull(str)) {
            return str2;
        }
        return jSONObject.getString(str);
    }
}
