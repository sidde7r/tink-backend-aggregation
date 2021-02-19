package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardEntity {
    private String cardholderName;

    @JsonIgnore
    public boolean isHolderNameNotBlank() {
        return StringUtils.isNotBlank(cardholderName);
    }
}
