package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private boolean avtale;
    private boolean betaling;
    private boolean tilgang;
    private boolean overfoereFra;
    private boolean overfoereTil;
}
