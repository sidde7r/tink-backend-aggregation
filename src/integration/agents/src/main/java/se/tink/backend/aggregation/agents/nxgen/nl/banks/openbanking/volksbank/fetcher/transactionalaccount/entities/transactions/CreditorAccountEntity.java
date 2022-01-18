package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CreditorAccountEntity {

    private String currency;
    private String iban;
}
