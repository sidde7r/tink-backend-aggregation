package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account.IdModuleMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.AccountFlag;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;
import se.tink.sa.services.fetch.account.TransactionalAccount;
import se.tink.sa.services.fetch.account.TransactionalAccountType;
import se.tink.sa.services.fetch.account.BalanceModule;

import java.util.Map;

@Component
public class TransactionalAccountResponseMapper
        implements Mapper<TransactionalAccount, AccountEntity> {

    @Autowired
    private TransactionalAccountTypeResponseMapper transactionalAccountTypeResponseMapper;

    @Autowired
    private IdModuleMapper idModuleMapper;

    @Override
    public TransactionalAccount map(AccountEntity source, MappingContext mappingContext) {
        TransactionalAccount.Builder destBuilder = TransactionalAccount.newBuilder();

        Map<String, ExactCurrencyAmount> balances =
                mappingContext.get(SibsMappingContextKeys.ACCOUNTS_BALANCES);
        ExactCurrencyAmount balance = balances.get(source.getId());

        destBuilder.setType(TransactionalAccountType.CHECKING);
        destBuilder.addFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        destBuilder.setBalanceModule(buildBalanceMofule(balance));
        destBuilder.setApiId(source.getId());
        destBuilder.setId(idModuleMapper.map(source, mappingContext));

        return destBuilder.build();
    }

    private BalanceModule buildBalanceMofule(ExactCurrencyAmount balance){
        BalanceModule.Builder builder = BalanceModule.newBuilder();
        builder.setExactBalance(balance);
        return builder.build();
    }
}
