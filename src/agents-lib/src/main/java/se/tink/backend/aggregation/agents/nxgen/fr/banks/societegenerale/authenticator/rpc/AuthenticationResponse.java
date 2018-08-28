package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.AuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse extends GenericResponse<AuthenticationData> {

}
