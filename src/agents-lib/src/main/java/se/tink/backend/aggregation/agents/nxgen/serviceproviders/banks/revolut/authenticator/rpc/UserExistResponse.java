package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.entities.UserExistEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.HashMap;

@JsonObject
public class UserExistResponse extends HashMap<String, UserExistEntity> {}
