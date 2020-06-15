package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TransactionsAndLockedEventsResponseDto {

    private AccountBaseInfoDto account;

    private String continuationKey;

    private List<LockedEventDto> lockedEvents;

    private List<TransactionInformationDto> transactions;
}
