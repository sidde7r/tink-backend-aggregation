package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.enums.MarketCode;

@Slf4j
public class NoAccountEntityMapper extends AccountEntityMapper {

    private static final int NO_ACCOUNT_NO_MIN_LENGTH = 11;

    public NoAccountEntityMapper() {
        super(MarketCode.NO.name());
    }

    @Override
    protected String getCreditCardUniqueIdentifier(AccountEntity accountEntity) {
        return getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoExt());
    }

    @Override
    protected IdModule buildIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(
                        getAccountNumberWithZerosIfIsTooShort(accountEntity.getAccountNoExt()))
                .withAccountNumber(accountEntity.getAccountNoExt())
                .withAccountName(accountEntity.getAccountName())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.BBAN, accountEntity.getAccountNoExt()))
                .build();
    }

    private String getAccountNumberWithZerosIfIsTooShort(String accountNumber) {
        if (accountNumber.length() < NO_ACCOUNT_NO_MIN_LENGTH) {
            log.error(
                    "Norwegian Danske Bank account number is shorter than expected ({}). Its length is [{}]",
                    NO_ACCOUNT_NO_MIN_LENGTH,
                    accountNumber.length());
        }
        return Strings.padStart(accountNumber, NO_ACCOUNT_NO_MIN_LENGTH, '0');
    }
}
