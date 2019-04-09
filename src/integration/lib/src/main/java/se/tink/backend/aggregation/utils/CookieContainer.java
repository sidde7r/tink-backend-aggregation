package se.tink.backend.aggregation.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.libraries.net.TinkApacheHttpClient4;

/** Stores a list of cookies that can be serialized with Jacksson */
public class CookieContainer {

    private List<Cookie> cookies = Lists.newArrayList();

    public List<Cookie> getCookies() {
        return cookies;
    }

    @JsonIgnore
    public void setCookiesFromClient(TinkApacheHttpClient4 client) {
        cookies = client.getClientHandler().getCookieStore().getCookies();
    }

    @JsonIgnore
    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    /**
     * Special deserialization since there isn't any public constructor on cookie-class that can be
     * used by Jacksson
     */
    @JsonProperty
    public void setCookies(Object cookies) {

        if (cookies == null) {
            return;
        }

        if (cookies instanceof List) {
            List<HashMap<String, Object>> inputList = (List<HashMap<String, Object>>) cookies;

            for (HashMap<String, Object> row : inputList) {
                BasicClientCookie cookie =
                        new BasicClientCookie((String) row.get("name"), (String) row.get("value"));

                cookie.setDomain((String) row.get("domain"));
                cookie.setPath((String) row.get("path"));
                cookie.setVersion((int) row.get("version"));
                cookie.setSecure((boolean) row.get("secure"));
                cookie.setComment((String) row.get("comment"));

                this.cookies.add(cookie);
            }
        }
    }
}
