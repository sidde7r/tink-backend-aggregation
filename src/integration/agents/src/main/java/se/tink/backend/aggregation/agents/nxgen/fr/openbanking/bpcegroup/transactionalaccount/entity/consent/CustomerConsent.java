package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerConsent {

    private List<ConsentDataEntity> balances;

    private List<ConsentDataEntity> transactions;

    private boolean trustedBeneficiaries;

    private boolean psuIdentity;
}
