package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;

@Data
public class ConsentDataEntity {
    private final String iban;
    private final String currency;
    private final GenericIdentificationEntity other;

    public ConsentDataEntity(String iban, String resourceId) {
        this.iban = iban;
        this.currency = BredBanquePopulaireConstants.ConsentValues.CURRENCY;
        this.other =
                GenericIdentificationEntity.builder()
                        .schemeName(BredBanquePopulaireConstants.ConsentValues.SCHEME_NAME)
                        .identification(resourceId)
                        .issuer(BredBanquePopulaireConstants.ConsentValues.ISSUER)
                        .build();
    }
}
