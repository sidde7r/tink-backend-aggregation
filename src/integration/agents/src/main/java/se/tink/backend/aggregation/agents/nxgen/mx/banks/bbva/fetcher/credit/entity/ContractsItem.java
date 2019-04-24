package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.utils.CreditCardMasker;

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

    public Optional<CreditCardAccount> toCreditCardAccount(String holderName) {
        try {
            return Optional.of(
                    CreditCardAccount.builder(id, detail.getBalance(), detail.getAvailableCredit())
                            .setAccountNumber(CreditCardMasker.maskCardNumber(number))
                            .setName(alias)
                            .setHolderName(new HolderName(holderName))
                            .putInTemporaryStorage(BbvaMxConstants.STORAGE.ACCOUNT_ID, id)
                            .build());
        } catch (Exception e) {
            logger.error("{} {}", BbvaMxConstants.LOGGING.CREDIT_PARSING_ERROR, e.toString());
            return Optional.empty();
        }
    }
}
