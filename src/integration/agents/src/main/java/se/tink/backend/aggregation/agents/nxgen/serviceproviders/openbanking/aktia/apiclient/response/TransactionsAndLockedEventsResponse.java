package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.response;

import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;

@Value
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
