package se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.entities.UserExistEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserExistResponse extends HashMap<String, UserExistEntity> {

}
