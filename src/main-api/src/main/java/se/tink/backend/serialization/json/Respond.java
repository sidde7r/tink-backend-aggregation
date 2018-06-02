package se.tink.backend.serialization.json;

import com.fasterxml.jackson.databind.util.JSONPObject;

public class Respond {

    public static Object jsonOrJsonp(String callbackParameter, Object body) {
        if (callbackParameter != null && callbackParameter.length() > 0) {
            return new JSONPObject(callbackParameter, body);
        } else {
            return body;
        }
    }
}
