package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class Account {
    private String relationType;
    private ConvertedBalance convertedBalance;
    private String productId;
    private String accountName;
    private String snum;
    private String accountType;
    private FlagsDTO flagsDTO;
    private String accountFamily;
    private String accountRelationship;
    private String refusalDate;
    private String contractNumber;
    private String accountTypeFullName;
    private Balance balance;
    private String iban;
    private String alias;
    private String currency;
    private String accountSubFamily;
}
