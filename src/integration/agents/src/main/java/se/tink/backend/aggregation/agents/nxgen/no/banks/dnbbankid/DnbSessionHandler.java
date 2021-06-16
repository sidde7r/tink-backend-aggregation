package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
@RequiredArgsConstructor
public class DnbSessionHandler implements SessionHandler {

    private final DnbApiClient apiClient;

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        HttpResponse response = apiClient.fetchAccountsRaw();
        if (isJsonResponse(response)) {
            return;
        }

        if (isHomePageRedirect(response)) {
            log.info("[DNB] Home page redirect - session expired");
        } else if (isRedirect(response)) {
            log.warn(
                    "[DNB] Unhandled redirect - session expired. Location: {}",
                    response.getLocation());
        } else {
            log.warn("[DNB] Unexpected non json response - session expired");
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }

    private boolean isJsonResponse(HttpResponse response) {
        String contentType = response.getHeaders().getFirst("Content-Type");
        return StringUtils.containsIgnoreCase(contentType, "application/json");
    }

    private boolean isRedirect(HttpResponse response) {
        return response.getStatus() == 302;
    }

    private boolean isHomePageRedirect(HttpResponse response) {
        String html = response.getBody(String.class);
        return DnbConstants.Messages.HOME_PAGE_REDIRECT.matcher(html).find();
    }
}
