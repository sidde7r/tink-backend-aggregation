package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.CmcicBaseFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountBaseConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CashAccountTypeEnumEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CmcicTransactionalAccountFetcher extends CmcicBaseFetcher<TransactionalAccount> {

    public CmcicTransactionalAccountFetcher(
            CmcicApiClient cmcicApiClient,
            CmcicAccountBaseConverter<TransactionalAccount> converter) {
        super(cmcicApiClient, converter);
    }

    @Override
    public boolean predicate(AccountResourceDto accountResourceDto) {
        return CashAccountTypeEnumEntity.CACC == accountResourceDto.getCashAccountType();
    }
}
