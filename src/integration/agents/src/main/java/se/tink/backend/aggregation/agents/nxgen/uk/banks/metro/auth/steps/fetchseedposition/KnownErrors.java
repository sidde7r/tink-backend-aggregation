package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition;

import static io.vavr.API.$;

import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import io.vavr.API;
import io.vavr.API.Match.Case;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error.UnknownError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.ErrorResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.libraries.serialization.utils.SerializationUtils;

enum KnownErrors {
    INVALID_CREDENTIALS(
            API.Case(
                    $(
                            res ->
                                    !API.Option(res)
                                            .filter(ResponseEntity::hasBody)
                                            .map(
                                                    r ->
                                                            SerializationUtils
                                                                    .deserializeFromString(
                                                                            r.getBody(),
                                                                            ErrorResponse.class))
                                            .filter(
                                                    errorResponse ->
                                                            errorResponse.hasCode(
                                                                    "INVALID_USER_DETAILS"))
                                            .isEmpty()),
                    new InvalidCredentialsError())),
    UNKNOWN_ERROR(
            API.Case(
                    $(res -> res.getStatusCode().isError()),
                    res -> new UnknownError(res.getStatusCode(), res.getBody())));
    private final Case<ResponseEntity<String>, AgentBankApiError> definitionOfCase;

    KnownErrors(Case<ResponseEntity<String>, AgentBankApiError> definitionOfCase) {
        this.definitionOfCase = definitionOfCase;
    }

    private Case<ResponseEntity<String>, AgentBankApiError> getCase() {
        return definitionOfCase;
    }

    static Case<ResponseEntity<String>, AgentBankApiError>[] getCases() {
        return Stream.of(values()).map(KnownErrors::getCase).toArray(Case[]::new);
    }
}
