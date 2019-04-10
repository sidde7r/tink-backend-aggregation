package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {
    private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_sub_category")
    private String productSubCategory;

    private String nickname;

    @JsonProperty("display_number")
    private String displayNumber;

    @JsonUnwrapped private AmountEntity amount;

    public TransactionalAccount toTinkAccount() {

        // Format CCCC-CC-CCC-iban -> iban
        String iban = productId.split("-")[3];

        if (!NordeaFiConstants.ACCOUNT_TYPES.containsKey(productSubCategory))
            log.warn(String.format("Unmapped account type (%s)", productSubCategory));

        return TransactionalAccount.builder(
                        NordeaFiConstants.ACCOUNT_TYPES.getOrDefault(
                                productSubCategory, AccountTypes.OTHER),
                        iban)
                .setName(nickname)
                .setBalance(amount)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .setAccountNumber(displayNumber)
                .setBankIdentifier(productId)
                .build();
    }
}
