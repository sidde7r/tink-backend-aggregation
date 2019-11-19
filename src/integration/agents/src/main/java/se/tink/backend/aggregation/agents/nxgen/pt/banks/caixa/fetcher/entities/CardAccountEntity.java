package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountEntity {

    private List<CardEntity> cards;
    private String accountId;
    private String accountNumber;
    private String cardCodeType;
    private String controlCardAccount;
    private Boolean corporateCardAccount;
    private String currency;
    private String depositAccount;
    private String depositAccountDescription;
    private String description;
    private Date nextAnnuityDate;
    private String paymentOptionDescription;
    private String productDescription;
    private String productImageUrl;
    private String statementIssueDateDescription;
    private String statementPaymentLimitDescription;
    private String type;

    public List<CardEntity> getCards() {
        return cards;
    }

    public String getType() {
        return type;
    }
}
