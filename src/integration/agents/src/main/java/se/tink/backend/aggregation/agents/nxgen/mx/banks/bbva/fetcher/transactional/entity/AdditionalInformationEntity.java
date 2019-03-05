package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AdditionalInformationEntity {
    private String reference;
    private UpdatedBalanceEntity updatedBalance;
    private String additionalData;

    public String getAdditionalData() {
        return additionalData;
    }
}
