package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.mapper;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.enums.MarketCode;

@Slf4j
public class DkAccountEntityMapper extends AccountEntityMapper {

    private static final int ACCOUNT_NO_MIN_LENGTH = 10;

    public DkAccountEntityMapper() {
        super(MarketCode.DK.name());
    }

    @Override
    protected String getUniqueIdentifier(AccountEntity accountEntity) {
        return getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoExt());
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        if (accountNumber.length() < ACCOUNT_NO_MIN_LENGTH) {
            log.warn(
                    "Account number is shorter than expected({}). Its length is [{}]",
                    ACCOUNT_NO_MIN_LENGTH,
                    accountNumber.length());
        }
        return Strings.padStart(accountNumber, ACCOUNT_NO_MIN_LENGTH, '0');
    }
}
