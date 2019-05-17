package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.bankid;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.AlandsBankenSeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken.AlandsBankenSeConstants;

public class AlandsBankenBankidSeConfiguration extends AlandsBankenSeConfiguration {
    @Override
    protected Optional<String> getAppVersion() {
        return Optional.of(AlandsBankenSeConstants.APP_VERSION);
    }
}
