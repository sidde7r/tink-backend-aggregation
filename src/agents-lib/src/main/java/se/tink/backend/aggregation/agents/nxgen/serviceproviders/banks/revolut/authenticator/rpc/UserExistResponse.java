package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.entities.UserExistEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserExistResponse extends HashMap<String, UserExistEntity> {}
