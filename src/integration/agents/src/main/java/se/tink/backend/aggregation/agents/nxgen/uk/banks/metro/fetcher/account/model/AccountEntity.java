package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model.CategoryEntity.CategoryType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {
    private String accountId;

    private String accountNumber;

    private LocalDate creationDate;

    private AvailableBalanceEntity availableBalance;

    private String currency;

    private String iban;

    private CategoryEntity category;

    private int ordinal;

    private String sortCode;

    private String productType;

    private String nickname;

    @JsonIgnore
    public CategoryType getAccountType(){
        return category.getAccountCategory();
    }
}
