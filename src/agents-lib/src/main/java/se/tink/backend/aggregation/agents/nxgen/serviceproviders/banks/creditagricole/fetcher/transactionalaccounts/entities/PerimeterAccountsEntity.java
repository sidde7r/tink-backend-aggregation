package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PerimeterAccountsEntity {
    private String perimeterId;
    private String id;
    private String accountNumber;
    private String label;
    private String holder;
    private double balance;
    private boolean selected;
    private List<CreditCardsEntity> creditCards;
}
