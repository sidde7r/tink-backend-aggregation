package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class PensionPlanEntity extends AbstractContractDetailsEntity {
    private AmountEntity liquidValue;

    private double shares;
    private AmountEntity balance;

    public AmountEntity getLiquidValue() {
        return liquidValue;
    }

    public double getShares() {
        return shares;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public InvestmentAccount toInvestmentAccount(Double totalProfit) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(
                        PortfolioModule.builder()
                                .withType(PortfolioType.PENSION)
                                .withUniqueIdentifier(getId())
                                .withCashValue(0)
                                .withTotalProfit(totalProfit == null ? 0.00 : totalProfit)
                                .withTotalValue(getBalance().getAmountAsDouble())
                                .withoutInstruments()
                                .build())
                .withZeroCashBalance(getBalance().toTinkAmount().getCurrencyCode())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getId())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, getId()))
                                .setProductName(getProduct().getName())
                                .build())
                .setApiIdentifier(getId())
                .build();
    }

    @JsonIgnore
    @Override
    protected String getAccountName() {
        return Optional.ofNullable(getUserCustomization())
                .map(UserCustomizationEntity::getAlias)
                .orElse(getProduct().getDescription());
    }

    @JsonIgnore
    @Override
    protected String getAccountNumber() {
        return Optional.ofNullable(getFormats()).map(FormatsEntity::getBocf).orElse(getId());
    }
}
