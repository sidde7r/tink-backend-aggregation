package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceEntity {

    private BalanceSingleEntity closingBooked;
    private BalanceSingleEntity authorised;
    private BalanceSingleEntity interimAvailable;
}
