package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CustomerProfileEntity {
    private String agentUserId;
    private String phoneNo;
    private String emailAddress;
    private String emailFormat;
    private boolean notificationsAgreement;
    private BigDecimal smsFee;
}
