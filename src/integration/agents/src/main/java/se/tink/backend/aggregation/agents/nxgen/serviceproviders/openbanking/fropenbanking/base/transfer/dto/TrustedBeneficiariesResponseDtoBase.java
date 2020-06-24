package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto;

import java.util.List;

public interface TrustedBeneficiariesResponseDtoBase {

    List<? extends BeneficiaryDtoBase> getBeneficiaries();

    LinksDtoBase getLinks();
}
