package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OverviewEntity {

    private CardResponseEntity cardResponse;
    private AccountResponseEntity accountResponse;

    public CardResponseEntity getCardResponse() {
        return cardResponse;
    }

    public void setCardResponse(CardResponseEntity cardResponse) {
        this.cardResponse = cardResponse;
    }

    public AccountResponseEntity getAccountResponse() {
        return accountResponse;
    }

    public void setAccountResponse(AccountResponseEntity accountResponse) {
        this.accountResponse = accountResponse;
    }

    public List<AccountEntity> getAccountEntities() {
        if (accountResponse == null || accountResponse.getList() == null) {
            return Lists.newArrayList();
        }

        return accountResponse.getList();
    }

    public List<CardEntity> getCards() {
        if (cardResponse == null || cardResponse.getList() == null) {
            return Lists.newArrayList();
        }

        return cardResponse.getList();
    }
}
