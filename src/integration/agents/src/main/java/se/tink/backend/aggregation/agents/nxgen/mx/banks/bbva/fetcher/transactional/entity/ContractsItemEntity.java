package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class ContractsItemEntity {
    private String number;
    private ProductEntity product;
    private NumberTypeEntity numberType;
    private String alias;
    private String id;
    private DetailEntity detail;
    private SingnedAuthorizationEntity singnedAuthorization;
    private String productType;
    private StatusEntity status;
    private SubProductTypeEntity subProductType;

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(ContractsItemEntity.class);

    @JsonIgnore
    private final TypeMapper<AccountTypes> accountTypeMapper =
            BBVAUtils.getTypeMapper(BBVAConstants.ACCOUNT_TYPES_MAP);

    public boolean isValid() {
        try {
            toTransactionalAccount("");
            return true;
        } catch (Exception e) {
            logger.error("{} {}", BBVAConstants.LOGGING.ACCOUNT_PARSING_ERROR, e.toString());
            return false;
        }
    }

    private CheckingAccount toCheckingAccount(String holdername) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(id)
                .setAccountNumber(number)
                .setBalance(detail.getCheckingBalance())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.TINK, number))
                .addHolderName(holdername)
                .setAlias(alias)
                .setProductName(subProductType.getName())
                .putInTemporaryStorage(BBVAConstants.STORAGE.ACCOUNT_ID, id)
                .build();
    }

    private SavingsAccount toSavingsAccount(String holdername) {
        return SavingsAccount.builder()
                .setUniqueIdentifier(id)
                .setAccountNumber(number)
                .setBalance(detail.getCheckingBalance())
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.TINK, number))
                .addHolderName(holdername)
                .setAlias(alias)
                .setProductName(subProductType.getName())
                .putInTemporaryStorage(BBVAConstants.STORAGE.ACCOUNT_ID, id)
                .build();
    }

    private String getAccountType() {
        return product.getId();
    }

    public TransactionalAccount toTransactionalAccount(String holdername) {
        AccountTypes accountType =
                accountTypeMapper
                        .translate(getAccountType())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Unknown type: %s", getAccountType())));
        if (accountType == AccountTypes.CHECKING) {
            return toCheckingAccount(holdername);
        } else if (accountType == AccountTypes.SAVINGS) {
            return toSavingsAccount(holdername);
        }

        throw new IllegalStateException(String.format("Unknown type: %s", getAccountType()));
    }
}
