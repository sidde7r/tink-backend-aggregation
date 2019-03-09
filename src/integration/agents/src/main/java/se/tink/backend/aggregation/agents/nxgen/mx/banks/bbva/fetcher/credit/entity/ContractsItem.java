package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class ContractsItem {
    private String number;
    private Product product;
    private NumberType numberType;
    private List<RelatedContractsItem> relatedContracts;
    private String alias;
    private String id;
    private Detail detail;
    private String productType;
    private Status status;
    private SubProductType subProductType;

    @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(ContractsItem.class);

    public Optional<CreditCardAccount> toCreditCardAccount() {
        try { // TODO: holdername to all accounts?
            return Optional.of(
                    CreditCardAccount.builder(id, detail.getBalance(), detail.getAvailableCredit())
                            .setAccountNumber(number)
                            .setName(
                                    alias) // TODO: check if all Loan/Checking should have the alias
                            // name?
                            .putInTemporaryStorage(BBVAConstants.STORAGE.ACCOUNT_ID, id)
                            .build());
        } catch (Exception e) {
            logger.error("{} {}", BBVAConstants.LOGGING.CREDIT_PARSING_ERROR, e.toString());
            return Optional.empty();
        }
    }
}
