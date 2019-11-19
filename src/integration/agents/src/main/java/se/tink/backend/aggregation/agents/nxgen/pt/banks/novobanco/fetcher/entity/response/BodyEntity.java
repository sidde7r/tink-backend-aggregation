package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BodyEntity {

    @JsonProperty("Movimentos")
    private List<MovementsEntity> movements;

    @JsonProperty("Saldo")
    private BalanceEntity balance;

    @JsonProperty("Moeda")
    private String currency;

    @JsonProperty("TokenPaginacao")
    private String tokenPaging;

    public BalanceEntity getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTokenPaging() {
        return tokenPaging;
    }

    public List<MovementsEntity> getMovements() {
        return movements;
    }
}
