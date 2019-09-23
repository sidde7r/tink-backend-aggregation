package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PensionPlansEntity {
    private String id;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("productDescription")
    private String accountName;

    private String productFamilyCode;
    private String subfamilyCode;
    private BigDecimal liquidValue;
    private BigDecimal sharesNumber;
    private String subfamilyTypeCode;
    private String currency;
    private BigDecimal consolidatedRights;
    private TransactionsEntity transactions;
    private String branch;

    @JsonProperty("comertialClassifications")
    private List<Object> commercialClassifications;

    private List<ActionsEntity> actions;
    private String bocf;
    private String productId;
    private String bankId;

    @JsonObject
    public InvestmentAccount toTinkAccount(String holderName) {
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(getCashBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(id)
                                .withAccountName(accountName)
                                .addIdentifier(AccountIdentifier.create(Type.TINK, id))
                                .setProductName(productName)
                                .build())
                .addHolderName(holderName)
                .setApiIdentifier(id)
                .build();
    }

    private ExactCurrencyAmount getCashBalance() {
        return ExactCurrencyAmount.of(consolidatedRights, currency);
    }
}
