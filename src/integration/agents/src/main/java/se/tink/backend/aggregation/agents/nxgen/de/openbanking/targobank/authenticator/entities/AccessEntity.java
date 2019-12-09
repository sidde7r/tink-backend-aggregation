package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities.ConsentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {
    private final List<ConsentAccountEntity> accounts = new ArrayList<>();
    private final List<ConsentAccountEntity> transactions = new ArrayList<>();
    private final List<ConsentAccountEntity> balances = new ArrayList<>();
    private String allPsd2;

    public AccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
