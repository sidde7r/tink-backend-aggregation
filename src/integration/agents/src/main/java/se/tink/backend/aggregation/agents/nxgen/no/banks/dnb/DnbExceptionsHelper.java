package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DnbExceptionsHelper {

    public static boolean customerDoesNotHaveAccessToResource(HttpResponseException e) {
        if (e.getResponse().getStatus() == 403
                && e.getResponse().hasBody()
                && e.getResponse()
                        .getBody(String.class)
                        .contains(DnbConstants.Messages.NO_ACCESS)) {
            log.warn("[DNB] Customer does not have access to the source.", e);
            return true;
        }
        return false;
    }

    public static boolean noResourceFoundForTheCustomer(HttpResponseException e) {
        if (e.getResponse().getStatus() == 404
                && e.getResponse().hasBody()
                && e.getResponse()
                        .getBody(String.class)
                        .contains(DnbConstants.Messages.NO_ACCOUNT_SUFFIX)) {
            log.warn(
                    "[DNB] Resource for customer not found {}.",
                    e.getResponse().getBody(String.class));
            return true;
        }
        return false;
    }
}
