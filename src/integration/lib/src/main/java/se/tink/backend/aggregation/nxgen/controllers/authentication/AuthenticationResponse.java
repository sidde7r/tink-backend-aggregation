package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;
import se.tink.backend.agents.rpc.Field;

/**
 * In progressive authentication, carry the intermediate step and fields. Yet to see if we need to
 * carry Credential object or any data in it.
 */
public class AuthenticationResponse {

    private String step;
    private List<Field> fields;

    public AuthenticationResponse(String step, List<Field> fields) {
        this.step = step;
        this.fields = fields;
    }

    public String getStep() {
        return step;
    }

    public List<Field> getFields() {
        return fields;
    }
}
