package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BbvaErrorHandler {

    private static List<BbvaErrorResolver> resolvers =
            Lists.newArrayList(
                    new BbvaErrorResolver(
                            401, "168", SupplementalInfoError.NO_VALID_CODE.exception()),
                    new BbvaErrorResolver(
                            403, "362", SupplementalInfoError.WAIT_TIMEOUT.exception()));

    public static Optional<AgentException> handle(HttpResponseException responseException) {
        return resolvers.stream()
                .map(r -> r.resolve(responseException.getResponse()))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .findFirst();
    }
}
