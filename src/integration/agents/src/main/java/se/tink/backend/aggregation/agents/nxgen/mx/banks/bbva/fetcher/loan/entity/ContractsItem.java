package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
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

    public Optional<LoanAccount> toLoanAccount() {
        if (BBVAConstants.ACCOUNT_TYPE_MAPPER.translate(product.getName()).isPresent()
                && BBVAConstants.ACCOUNT_TYPE_MAPPER
                        .translate(product.getName())
                        .get()
                        .equals(AccountTypes.LOAN))
            try {
                return Optional.of(
                        LoanAccount.builder(id)
                                .setAccountNumber(number)
                                .setBankIdentifier(id)
                                .setName(product.getName())
                                .setBalance(detail.getBalance())
                                .build());
            } catch (Exception e) {
                return Optional.empty();
            }
        return Optional.empty();
    }
}
