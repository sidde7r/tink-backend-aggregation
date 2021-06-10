package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.CardType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class ProductDto {

    private DigitalInfoDto digitalInfo;

    private AccountTypesDto accountTypes;

    private List<String> accountEligibilities;

    @JsonIgnore
    AccountHolderType getHolderType() {
        return Optional.ofNullable(accountTypes)
                .map(AccountTypesDto::getLineOfBusinessType)
                .map(this::mapLineOfBusinessToHolderType)
                .orElse(AccountHolderType.UNKNOWN);
    }

    private AccountHolderType mapLineOfBusinessToHolderType(String lineOfBusinessType) {
        switch (lineOfBusinessType) {
            case CardType.CONSUMER_CARD:
                return AccountHolderType.PERSONAL;
            case CardType.COMPANY_CARD:
                return AccountHolderType.BUSINESS;
            case CardType.CORPORATE_CARD:
                return AccountHolderType.CORPORATE;
            default:
                return AccountHolderType.UNKNOWN;
        }
    }
}
