package se.tink.sa.agent.pt.ob.sibs.rest.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonAccountTransactionsSibsRequest extends CommonAccountSibsRequestRequest {

    private String dateFromTransactionFetch;
    private String nextPageUri;
}
