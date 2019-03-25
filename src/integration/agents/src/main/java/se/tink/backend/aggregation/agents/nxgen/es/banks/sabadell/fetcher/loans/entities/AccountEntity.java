package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String alias;
    private AmountEntity amount;
    private String availability;
    private String bic;
    private String contractCode;
    private String contractNumberFormatted;
    private String description;
    private String entityCode;
    private String hashIban;
    private String iban;
    private boolean isIberSecurities;
    private boolean isOwner;
    private boolean isSBPManaged;
    private String joint;
    private String mobileWarning;
    private int numOwners;
    private String number;
    private String owner;
    private String product;
    private String productType;
    private String value;
}
