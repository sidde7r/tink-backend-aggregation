package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ViewDetailListEntity {
    // `viewDetailId` is null - cannot define it!
    private String accountType;
    private String accountSequenceNumber;
    private String accountNumber;
    private AccountEntity account;
}
