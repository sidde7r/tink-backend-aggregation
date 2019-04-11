package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import static io.vavr.Predicates.not;

import io.vavr.control.Option;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private String application;
    private String tokenCredential;

    public String getApplication() {
        return application;
    }

    public boolean hasTokenCredential() {
        return getTokenCredential().filter(not(String::isEmpty)).isDefined();
    }

    public Option<String> getTokenCredential() {
        return Option.of(tokenCredential);
    }
}
