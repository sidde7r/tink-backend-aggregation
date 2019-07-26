package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class AmericanExpressV62Storage extends Storage {

    public AmericanExpressV62Storage() {
        super();
    }

    public void saveCreditCardList(List<CardEntity> cardList) {
        this.put(AmericanExpressV62Constants.Tags.CARD_LIST, cardList);
    }

    public List<CardEntity> getCreditCardList() {
        return this.get(
                        AmericanExpressV62Constants.Tags.CARD_LIST,
                        new TypeReference<List<CardEntity>>() {})
                .orElse(Collections.emptyList());
    }

    public void saveCompleteSubAccountsMap(
            Map<String, HashMap<String, String>> completeSubAccountsMap) {
        this.put(AmericanExpressV62Constants.Storage.ALL_SUB_ACCOUNTS, completeSubAccountsMap);
    }

    public Map<String, String> getSubAccountMap(final String accountNumber) {
        Map<String, String> subAccountMap = new HashMap<>();
        if (this.getCompleteSubAccountsMap().containsKey(accountNumber)) {
            subAccountMap = this.getCompleteSubAccountsMap().get(accountNumber);
        }
        return subAccountMap;
    }

    public Map<String, HashMap<String, String>> getCompleteSubAccountsMap() {
        return this.get(
                        AmericanExpressV62Constants.Storage.ALL_SUB_ACCOUNTS,
                        new TypeReference<Map<String, HashMap<String, String>>>() {})
                .orElse(new HashMap<>());
    }

    public void saveAccountTransactions(
            String accountNumber, Set<TransactionEntity> accountTransactions) {
        this.put(accountNumber, accountTransactions);
    }

    public Set<TransactionEntity> getAccountTransactions(String accountNumber) {
        return this.get(accountNumber, new TypeReference<Set<TransactionEntity>>() {})
                .orElse(new HashSet<>());
    }
}
