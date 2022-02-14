package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;

public interface CbiScaMethodSelectable {

    String getSelectAuthenticationMethodLink();

    List<ScaMethodEntity> getScaMethods();
}
