package se.tink.backend.aggregation.nxgen.http.serializecontainer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.nxgen.http.filter.filters.persistent.Header;

public class SerializeContainer {
    private List<Cookie> cookies = Lists.newArrayList();
    private HashSet<Header> headers = Sets.newHashSet();

    public List<Cookie> getCookies() {
        return cookies != null ? cookies : Lists.newArrayList();
    }

    public HashSet<Header> getHeaders() {
        return headers != null ? headers : Sets.newHashSet();
    }

    @JsonIgnore
    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(HashSet<Header> headers) {
        this.headers = headers;
    }

    /*
     * Special deserialization since there isn't any public constructor on cookie-class that can be used by Jacksson
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
                        new BasicClientCookie(
                                (String) row.get(SerializeConstants.NAME),
                                (String) row.get(SerializeConstants.VALUE));

                cookie.setDomain((String) row.get(SerializeConstants.DOMAIN));
                cookie.setPath((String) row.get(SerializeConstants.PATH));
                cookie.setVersion((int) row.get(SerializeConstants.VERSION));
                cookie.setSecure((boolean) row.get(SerializeConstants.SECURE));
                cookie.setComment((String) row.get(SerializeConstants.COMMENT));
                Optional.ofNullable((String) row.get(SerializeConstants.PATH))
                        .ifPresent(path -> cookie.setAttribute(SerializeConstants.PATH, path));
                Optional.ofNullable((String) row.get(SerializeConstants.DOMAIN))
                        .ifPresent(
                                domain -> cookie.setAttribute(SerializeConstants.DOMAIN, domain));
                cookie.setAttribute(SerializeConstants.HTTP_ONLY, null);
                cookie.setAttribute(
                        SerializeConstants.SECURE,
                        (boolean) row.get(SerializeConstants.SECURE)
                                ? SerializeConstants.TRUE
                                : null);

                this.cookies.add(cookie);
            }
        }
    }
}
