package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class CustomerConsent {
    private List<ConsentDataEntity> balances;
    private List<ConsentDataEntity> transactions;
    private boolean trustedBeneficiaries;
    private boolean psuIdentity;
}
