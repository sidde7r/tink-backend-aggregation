package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@JsonObject
public class ContractsItem {
    private String number;
    private Product product;
    private NumberType numberType;
    private String alias;
    private String id;
    private Detail detail;
    private String productType;
    private Status status;

    @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(ContractsItem.class);

    @JsonIgnore
    private final TypeMapper<AccountTypes> accounTypeMapper =
            BbvaMxUtils.getTypeMapper(BbvaMxConstants.ACCOUNT_TYPES_MAP);

    public Optional<LoanAccount> toLoanAccount() {
        final Optional<AccountTypes> accountType = accounTypeMapper.translate(product.getId());
        if (accountType.isPresent() && accountType.get().equals(AccountTypes.LOAN))
            try {
                return Optional.of(
                        LoanAccount.builder(id)
                                .setAccountNumber(number)
                                .setBankIdentifier(id)
                                .setName(alias)
                                .setBalance(detail.getBalance())
                                .build());
            } catch (Exception e) {
                logger.error("{} {}", BbvaMxConstants.LOGGING.LOAN_PARSING_ERROR, e.toString());
                return Optional.empty();
            }
        return Optional.empty();
    }
}
