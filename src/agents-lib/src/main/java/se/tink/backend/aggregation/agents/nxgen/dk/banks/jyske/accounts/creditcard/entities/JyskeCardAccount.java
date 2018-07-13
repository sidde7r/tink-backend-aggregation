package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JyskeCardAccount {
    private String accountName;
    private long regNo;
    private long accountNo;
}
