package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation;

import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public interface SupplementalInformationProvider {

    SupplementalInformationHelper getSupplementalInformationHelper();

    SupplementalInformationController getSupplementalInformationController();
}
