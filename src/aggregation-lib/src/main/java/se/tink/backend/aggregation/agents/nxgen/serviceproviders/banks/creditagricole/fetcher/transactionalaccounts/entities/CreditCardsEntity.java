package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsEntity {
    private long id;
    private String cardNumber;
    private String name;
    private String type;
    private boolean isInBudget;
    private int customIndex;
    private String idelco;
    private String idelex;
    private String nomTypeCarte;
    private String typeCarte;
    private String couleur;
    private boolean mentionCB;
    private String logo;
    private boolean logoEKO;
}
