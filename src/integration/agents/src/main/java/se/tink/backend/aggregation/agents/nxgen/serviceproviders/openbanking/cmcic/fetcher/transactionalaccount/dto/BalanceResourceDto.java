package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto;

import java.util.Date;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BalanceStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BalanceResourceDto {

    private String name;

    private AmountTypeEntity balanceAmount;

    private BalanceStatusEntity balanceType;

    private Date lastChangeDateTime;

    private Date referenceDate;
}
