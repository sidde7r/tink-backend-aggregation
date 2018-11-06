package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;

public final class BawagPskAccountTypeMappers {
    private AccountTypeMapper productCodeMapper;
    private AccountTypeMapper productTypeMapper;

    public AccountTypeMapper getProductCodeMapper() {
        if (productCodeMapper == null) {
            productCodeMapper = AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "B121", "B131", "B400")
                    .putRegex(AccountTypes.CHECKING, Pattern.compile("B\\w\\w\\w"))
                    .put(AccountTypes.SAVINGS, "D272", "D264")
                    .putRegex(AccountTypes.SAVINGS, Pattern.compile("D\\w\\w\\w"))
                    .put(AccountTypes.CREDIT_CARD, "00EC", "00ET", "00PD")
                    .putRegex(AccountTypes.CREDIT_CARD, Pattern.compile("00\\w\\w"))
                    .put(AccountTypes.LOAN, "S132")
                    .putRegex(AccountTypes.LOAN, Pattern.compile("S\\w\\w\\w"))
                    .build();
        }
        return productCodeMapper;
    }

    // Fallback mapper; more error-prone because the bank assigns a savings account to "CHECKING"
    // for some reason
    public AccountTypeMapper getProductTypeMapper() {
        if (productTypeMapper != null) {
            productTypeMapper = AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "CHECKING")
                    .put(AccountTypes.SAVINGS, "SAVINGS")
                    .put(AccountTypes.CREDIT_CARD, "CREDIT_CARD")
                    .put(AccountTypes.LOAN, "LOAN")
                    .build();
        }
        return productTypeMapper;
    }
}
