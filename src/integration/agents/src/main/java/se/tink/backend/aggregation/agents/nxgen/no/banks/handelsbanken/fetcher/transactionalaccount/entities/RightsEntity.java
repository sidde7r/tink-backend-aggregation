package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RightsEntity {
    private boolean accessPermission;
    private boolean paymentFrom;
    private boolean transferFrom;
    private boolean transferTo;
    private boolean createPaymentAgreement;
    private boolean createAutopaymentAgreement;
}
