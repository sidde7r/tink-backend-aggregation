package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private T data;
    private String[] message;
    private String internalCode;
}
