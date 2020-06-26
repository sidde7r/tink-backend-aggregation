package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation;

import java.util.Map;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public final class MockSupplementalInformationProvider implements SupplementalInformationProvider {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalInformationController supplementalInformationController;

    public MockSupplementalInformationProvider(final Map<String, String> mockCallbackData) {
        this.supplementalInformationHelper =
                new MockSupplementalInformationHelper(mockCallbackData);
        this.supplementalInformationController =
                new MockSupplementalInformationController(mockCallbackData);
    }

    @Override
    public SupplementalInformationHelper getSupplementalInformationHelper() {
        return supplementalInformationHelper;
    }

    @Override
    public SupplementalInformationController getSupplementalInformationController() {
        return supplementalInformationController;
    }
}
