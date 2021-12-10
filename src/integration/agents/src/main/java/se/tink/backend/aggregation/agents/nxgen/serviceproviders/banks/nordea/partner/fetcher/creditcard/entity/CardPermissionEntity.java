package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Slf4j
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardPermissionEntity {

    private boolean transactions;

    private boolean transferFromCredit;

    private boolean viewPin;

    private boolean allowedToEnrollMobilePayments;
}
