package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import io.vavr.CheckedPredicate;
import java.util.function.Predicate;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public final class BbvaPredicates {
    public static final CheckedPredicate<HttpResponse> IS_HTML_MEDIA_TYPE =
            response ->
                    MediaType.TEXT_HTML.equalsIgnoreCase(
                            response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

    public static final CheckedPredicate<BbvaResponse> IS_BANK_SERVICE_UNAVAILABLE =
            response -> response.hasError(BbvaConstants.Error.BANK_SERVICE_UNAVAILABLE);

    public static final CheckedPredicate<BbvaResponse> IS_RESPONSE_OK =
            response -> BbvaConstants.Message.OK.equalsIgnoreCase(response.getResult().getCode());

    public static final CheckedPredicate<BbvaResponse> RESPONSE_HAS_ERROR = BbvaResponse::hasError;

    public static final CheckedPredicate<String> IS_LOGIN_SUCCESS =
            response -> response.contains(BbvaConstants.Message.LOGIN_SUCCESS);

    public static final CheckedPredicate<String> IS_LOGIN_WRONG_CREDENTIALS =
            response -> response.contains(BbvaConstants.Message.LOGIN_WRONG_CREDENTIAL_CODE);

    public static final Predicate<AccountTypes> IS_TRANSACTIONAL_ACCOUNT =
            accountType ->
                    accountType.equals(AccountTypes.CHECKING)
                            || accountType.equals(AccountTypes.SAVINGS);
}
