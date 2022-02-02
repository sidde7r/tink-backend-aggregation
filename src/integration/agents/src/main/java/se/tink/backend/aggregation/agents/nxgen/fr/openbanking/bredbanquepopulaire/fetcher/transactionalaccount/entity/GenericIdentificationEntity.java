package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GenericIdentificationEntity {
    private final String identification;
    private final String schemeName;
    private final String issuer;
}
