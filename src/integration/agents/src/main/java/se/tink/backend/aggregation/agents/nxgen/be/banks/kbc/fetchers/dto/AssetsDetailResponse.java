package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetsDetailResponse extends HeaderResponse {

    private List<AssetsDetailGroupDto> groups;
    private TypeValuePair total;
    private TypeValuePair productSalesEntryLink;

    public List<AssetsDetailGroupDto> getGroups() {
        return groups;
    }
}
