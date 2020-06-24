package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDtoBase;

public interface FrAispApiClient {

    Optional<? extends TrustedBeneficiariesResponseDtoBase> getTrustedBeneficiaries();

    Optional<? extends TrustedBeneficiariesResponseDtoBase> getTrustedBeneficiaries(String path);
}
