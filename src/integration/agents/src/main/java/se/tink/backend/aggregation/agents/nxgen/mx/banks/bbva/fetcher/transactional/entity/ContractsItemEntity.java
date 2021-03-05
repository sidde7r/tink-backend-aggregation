package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
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
            BbvaMxUtils.getTypeMapper(BbvaMxConstants.ACCOUNT_TYPES_MAP);

    public boolean isValid() {
        try {
            toTransactionalAccount("");
            return true;
        } catch (Exception e) {
            logger.error("{} {}", BbvaMxConstants.LOGGING.ACCOUNT_PARSING_ERROR, e.toString(), e);
            return false;
        }
    }

    private Optional<TransactionalAccount> toCheckingAccount(String holdername) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(detail.getCheckingBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(number)
                                .withAccountName(subProductType.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.TINK, number))
                                .build())
                .putInTemporaryStorage(BbvaMxConstants.STORAGE.ACCOUNT_ID, id)
                .addHolderName(holdername)
                .build();
    }

    private Optional<TransactionalAccount> toSavingsAccount(String holdername) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(detail.getCheckingBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(number)
                                .withAccountName(subProductType.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.TINK, number))
                                .build())
                .putInTemporaryStorage(BbvaMxConstants.STORAGE.ACCOUNT_ID, id)
                .addHolderName(holdername)
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
            return toCheckingAccount(holdername)
                    .orElseThrow(
                            () -> new IllegalStateException("Cannot create transactional account"));
        } else if (accountType == AccountTypes.SAVINGS) {
            return toSavingsAccount(holdername)
                    .orElseThrow(
                            () -> new IllegalStateException("Cannot create transactional account"));
        }

        throw new IllegalStateException(String.format("Unknown type: %s", getAccountType()));
    }
}
