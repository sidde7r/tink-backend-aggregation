package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response;

import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountsSummaryResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;

@Value
public class AccountsSummaryResponse {

    private final boolean successful;

    private final AccountsSummaryResponseDto accountsSummaryResponseDto;

    private final OpenAmErrorResponseDto openAmErrorResponseDto;

    public AccountsSummaryResponse(AccountsSummaryResponseDto accountsSummaryResponseDto) {
        this.successful = true;
        this.accountsSummaryResponseDto = accountsSummaryResponseDto;
        this.openAmErrorResponseDto = null;
    }

    public AccountsSummaryResponse(OpenAmErrorResponseDto openAmErrorResponseDto) {
        this.successful = false;
        this.accountsSummaryResponseDto = null;
        this.openAmErrorResponseDto = openAmErrorResponseDto;
    }
}
