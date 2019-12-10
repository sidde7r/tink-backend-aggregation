package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class AssetsDetailSubGroupDto {

    private TypeValuePair label;
    private TypeValuePair groupId;
    private List<AssetsDetailPositionDto> positions;

    public List<InstrumentModule> toTinkInstruments(List<String> investmentProductNames) {
        if (CollectionUtils.isEmpty(positions)) {
            return Collections.emptyList();
        }
        List<AssetsDetailPositionDto> filteredPositions =
                positions.stream()
                        .filter(
                                position ->
                                        investmentProductNames.contains(position.toProductName()))
                        .collect(Collectors.toList());

        return filteredPositions.stream()
                .map(position -> position.toTinkInstrument(toTinkInstrumentType()))
                .collect(Collectors.toList());
    }

    public InstrumentModule.InstrumentType toTinkInstrumentType() {
        String labelValue = Optional.ofNullable(label).map(TypeValuePair::getValue).orElse(null);
        if (StringUtils.isEmpty(labelValue)) {
            return InstrumentModule.InstrumentType.OTHER;
        } else if (labelValue.toLowerCase().contains("fund")) {
            return InstrumentModule.InstrumentType.FUND;
        } else if (labelValue.toLowerCase().contains("stock")) {
            return InstrumentModule.InstrumentType.STOCK;
        }
        return InstrumentModule.InstrumentType.OTHER;
    }
}
