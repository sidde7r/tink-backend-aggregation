package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.LoginGridData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginGridResponse extends GenericResponse<LoginGridData> {}
