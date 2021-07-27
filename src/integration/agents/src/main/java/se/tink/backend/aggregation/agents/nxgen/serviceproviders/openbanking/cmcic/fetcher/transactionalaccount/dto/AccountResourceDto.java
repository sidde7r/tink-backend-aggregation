package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PsuAccountIdentificationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountResourceDto {

    private String resourceId;

    private PsuAccountIdentificationEntity accountId;

    private String name;

    private String linkedAccount;

    private CashAccountType cashAccountType;

    private List<BalanceResourceDto> balances;

    private String psuStatus;

    @JsonProperty("_links")
    private AccountLinksEntity links;
}
