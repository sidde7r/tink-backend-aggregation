package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard;

import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.CmcicBaseFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountBaseConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CmcicCreditCardFetcher extends CmcicBaseFetcher<CreditCardAccount> {

    public CmcicCreditCardFetcher(
            CmcicApiClient cmcicApiClient, CmcicAccountBaseConverter<CreditCardAccount> converter) {
        super(cmcicApiClient, converter);
    }

    @Override
    public boolean predicate(AccountResourceDto accountResourceDto) {
        return CashAccountType.CARD == accountResourceDto.getCashAccountType();
    }
}
