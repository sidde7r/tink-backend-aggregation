package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;

@Data
public class TransactionsAndLockedEventsResponse {

    private final boolean successful;

    private final TransactionsAndLockedEventsResponseDto transactionsAndLockedEventsResponseDto;

    private final OpenAmErrorResponseDto openAmErrorResponseDto;

    public TransactionsAndLockedEventsResponse(
            TransactionsAndLockedEventsResponseDto transactionsAndLockedEventsResponseDto) {
        this.successful = true;
        this.transactionsAndLockedEventsResponseDto = transactionsAndLockedEventsResponseDto;
        this.openAmErrorResponseDto = null;
    }

    public TransactionsAndLockedEventsResponse(OpenAmErrorResponseDto openAmErrorResponseDto) {
        this.successful = false;
        this.transactionsAndLockedEventsResponseDto = null;
        this.openAmErrorResponseDto = openAmErrorResponseDto;
    }
}
