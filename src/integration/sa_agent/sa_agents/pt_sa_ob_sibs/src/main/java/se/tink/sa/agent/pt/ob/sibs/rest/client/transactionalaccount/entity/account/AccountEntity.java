package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountEntity {

    private String id;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;
    private String name;
    private String accountType;
    private String cashAccountType;
    private String bic;
    private List<BalanceEntity> balances;
    private AccountLinksEntity links;
}
