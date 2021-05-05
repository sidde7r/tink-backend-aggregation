package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.Investments.KBC_INVESTMENTS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

@JsonObject
public class AssetsDetailGroupDto {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TypeValuePair total;
    private TypeValuePair label;
    private TypeValuePair groupId;
    private List<AssetsDetailSubGroupDto> subgroups;

    public Double toTotalVale() {
        return Optional.ofNullable(total)
                .map(TypeValuePair::getValue)
                .map(Double::parseDouble)
                .orElse(0.0);
    }

    public String toLabel() {
        return Optional.ofNullable(label).map(TypeValuePair::getValue).orElse("");
    }

    public String toGroupId() {
        return Optional.ofNullable(groupId).map(TypeValuePair::getValue).orElse("");
    }

    public List<PortfolioModule> toTinkPortfolioModules(
            Double cashValue, List<String> investmentProductNames) {
        if (!KBC_INVESTMENTS.equalsIgnoreCase(toLabel())) {
            return Collections.emptyList();
        }
        return Stream.of(toTinkPortfolio(cashValue, investmentProductNames))
                .collect(Collectors.toList());
    }

    private PortfolioModule toTinkPortfolio(Double cashValue, List<String> investmentProductNames) {
        List<InstrumentModule> instrumentModules =
                Optional.ofNullable(subgroups).orElse(Collections.emptyList()).stream()
                        .map(subgroup -> subgroup.toTinkInstruments(investmentProductNames))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        Double totalProfit =
                instrumentModules.stream()
                        .map(InstrumentModule::getProfit)
                        .mapToDouble(Double::doubleValue)
                        .sum();
        Double totalValue =
                instrumentModules.stream()
                        .map(InstrumentModule::getMarketValue)
                        .mapToDouble(Double::doubleValue)
                        .sum();

        if (Double.compare(toTotalVale(), totalValue) != 0) {
            logger.warn(
                    String.format(
                            "Total value from the bank is %f, but calculated total value is %f",
                            toTotalVale(), totalValue));
        }

        return PortfolioModule.builder()
                .withType(PortfolioModule.PortfolioType.OTHER)
                .withUniqueIdentifier("Group" + toGroupId())
                .withCashValue(cashValue)
                .withTotalProfit(totalProfit)
                .withTotalValue(totalValue)
                .withInstruments(instrumentModules)
                .build();
    }
}
