package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.LinksDto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TransactionsResponseDto {

    private List<TransactionResourceDto> transactions;

    @JsonProperty("_links")
    private LinksDto links;
}
