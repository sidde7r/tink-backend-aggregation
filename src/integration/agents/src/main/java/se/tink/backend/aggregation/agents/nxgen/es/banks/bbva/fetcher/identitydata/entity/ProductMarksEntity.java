package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductMarksEntity {
    private TypeEntity hasPreGrantedLoan;
    private boolean hasPendingTasks;
    private boolean hasIICs;
    private boolean hasCRMCampaigns;
    private boolean hasFinancedTransactions;
}
