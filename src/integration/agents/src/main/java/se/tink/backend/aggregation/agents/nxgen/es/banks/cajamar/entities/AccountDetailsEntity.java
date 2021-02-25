package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsEntity {
    private String alias;
    private String iban;
    private String bic;
    private String openingDate;
    private String lastOperationDate;
    private String language;
    private String type;
    private String settlement;
    private String currency;
    private BigDecimal averageBalance;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal dailyLimit;
    private BigDecimal availableToday;

    @JsonProperty("holders")
    private List<ParticipantAccountEntity> accountParticipants;

    public List<ParticipantAccountEntity> getAccountParticipants() {
        return accountParticipants;
    }
}
