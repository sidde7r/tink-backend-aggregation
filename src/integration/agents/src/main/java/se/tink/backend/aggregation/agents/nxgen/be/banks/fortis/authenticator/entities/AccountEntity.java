package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String iban;
    private String currency;
    private String accountName;
    private String alias;
    private String accountType;
    private String accountTypeFullName;
    private String relationType;
    private String accountRelationship;
    private String productId;
    private String refusalDate;
    private String snum;
    private String accountFamily;
    private String accountSubFamily;
    private String contractNumber;
    private FlagsDTOEntity flagsDTO;
    private BalanceEntity  balance;
    private ConvertedBalanceEntity convertedBalance;
}
