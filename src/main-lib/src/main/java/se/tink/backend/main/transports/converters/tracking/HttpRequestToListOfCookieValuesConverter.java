package se.tink.backend.main.transports.converters.tracking;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestToListOfCookieValuesConverter {
    public static Map<String, String> getCookies(HttpServletRequest httpRequest) {
        List<Cookie> cookies = Arrays.asList(httpRequest.getCookies());
        Map<String, String> map = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            map.put(cookie.getName(), cookie.getValue());
        }
        return map;
    }
}
