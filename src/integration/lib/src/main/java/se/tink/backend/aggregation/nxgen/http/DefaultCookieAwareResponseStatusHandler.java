package se.tink.backend.aggregation.nxgen.http;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class DefaultCookieAwareResponseStatusHandler extends DefaultResponseStatusHandler {

    private final SessionStorage sessionStorage;

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        CookieRepository cookieRepository = CookieRepository.getInstance(sessionStorage);
        httpResponse
                .getCookies()
                .forEach(
                        cookie ->
                                cookieRepository.addCookie(
                                        cookie.getName(),
                                        cookie.getValue(),
                                        cookie.getPath(),
                                        cookie.getDomain(),
                                        cookie.getVersion()));
        cookieRepository.save(sessionStorage);
        super.handleResponse(httpRequest, httpResponse);
    }
}
