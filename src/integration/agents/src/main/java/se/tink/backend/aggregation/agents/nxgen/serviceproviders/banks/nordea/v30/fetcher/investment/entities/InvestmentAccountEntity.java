package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.entities;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.CURRENCY;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class InvestmentAccountEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("has_additional_info")
    private boolean hasMoreInfo;

    @JsonProperty("profit_loss")
    private double profitLoss;

    @JsonProperty("profit_loss_valid")
    private boolean profitLossValid;

    @JsonProperty("cash_amount")
    private double balance;

    @JsonProperty private String name;
    @JsonProperty private String id;
    @JsonProperty private String classification;

    @JsonProperty("market_value")
    private double value;

    @JsonProperty private List<HoldingEntity> holdings;

    public InvestmentAccount toTinkInvestmentAccount() {

        // TODO: create pension account builder
        // In some cases (e.g. some Pension accounts) the account doesn't have any holdings
        if (!hasInstruments()) {
            return InvestmentAccount.nxBuilder()
                    .withoutPortfolios()
                    .withZeroCashBalance(CURRENCY)
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier(id)
                                    .withAccountNumber(StringUtils.deleteWhitespace(accountNumber))
                                    .withAccountName(name)
                                    .addIdentifier(
                                            AccountIdentifier.create(
                                                    AccountIdentifierType.TINK, id))
                                    .build())
                    .build();
        }

        return InvestmentAccount.nxBuilder()
                .withPortfolios(Collections.singletonList(getTinkPortfolio()))
                .withZeroCashBalance(CURRENCY)
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(StringUtils.deleteWhitespace(accountNumber))
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifierType.TINK, id))
                                .build())
                .build();
    }

    private PortfolioModule getTinkPortfolio() {

        return PortfolioModule.builder()
                .withType(getTinkPortfolioType())
                .withUniqueIdentifier(id)
                .withCashValue(balance)
                .withTotalProfit(profitLoss)
                .withTotalValue(value)
                .withInstruments(getInstruments())
                .setRawType(getRawType())
                .build();
    }

    private List<InstrumentModule> getInstruments() {
        if (holdings == null) {
            return Collections.emptyList();
        }

        return holdings.stream()
                .filter(HoldingEntity::isInstrument)
                .map(HoldingEntity::toTinkInstrument)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean hasInstruments() {
        return !getInstruments().isEmpty() || classification.equalsIgnoreCase("PENSION");
    }

    private String getRawType() {

        // id format: TYPE:ACCOUNT
        return id.split(":")[0];
    }

    private PortfolioType getTinkPortfolioType() {

        final String type = id.substring(0, id.indexOf(":"));
        return NordeaBaseConstants.PORTFOLIO_TYPE_MAPPER
                .translate(type)
                .orElse(PortfolioType.OTHER);
    }

    public List<HoldingEntity> getHoldings() {
        return holdings;
    }
}
