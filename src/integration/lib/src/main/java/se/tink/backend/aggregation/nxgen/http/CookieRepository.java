package se.tink.backend.aggregation.nxgen.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Cookie;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class CookieRepository {

    private static final String COOKIE_STORE_KEY =
            DefaultResponseStatusHandler.class.getSimpleName()
                    + "_"
                    + CookieRepository.class.getSimpleName();

    private List<CookieEntity> cookieEntities = new LinkedList<>();

    public void addCookie(String name, String value) {
        cookieEntities.add(new CookieEntity(name, value, null, null, 1));
    }

    public void addCookie(String name, String value, String path, String domain, int version) {
        cookieEntities.add(new CookieEntity(name, value, path, domain, version));
    }

    @JsonIgnore
    public List<Cookie> getCookies() {
        return cookieEntities.stream()
                .map(e -> new Cookie(e.name, e.value, e.path, e.domain, e.version))
                .collect(Collectors.toList());
    }

    public static CookieRepository getInstance(SessionStorage sessionStorage) {
        return sessionStorage
                .get(COOKIE_STORE_KEY, CookieRepository.class)
                .orElseGet(CookieRepository::new);
    }

    public void save(SessionStorage sessionStorage) {
        sessionStorage.put(COOKIE_STORE_KEY, this);
    }

    @JsonObject
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class CookieEntity {
        private String name;
        private String value;
        private String path;
        private String domain;
        private int version;
    }
}
