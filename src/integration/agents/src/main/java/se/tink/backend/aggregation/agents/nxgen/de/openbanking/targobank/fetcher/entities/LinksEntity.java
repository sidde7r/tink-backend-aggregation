package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.HrefEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private HrefEntity balances;
    private HrefEntity transactions;
}
