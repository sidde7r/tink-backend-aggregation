package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JoseHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities.JwtPayloadEntity;

public interface Jwt {
    JoseHeaderEntity getJoseHeader();

    JwtPayloadEntity getPayload();
}
