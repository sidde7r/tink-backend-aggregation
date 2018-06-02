package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectBankIdRequest {
    private String orderRef;

    public static CollectBankIdRequest createRequestFromOrderBankIdResponse(OrderBankIdResponse orderBankIdResponse) {
        CollectBankIdRequest collectBankIdRequest = new CollectBankIdRequest();
        collectBankIdRequest.orderRef = orderBankIdResponse.getOrderRef();

        return collectBankIdRequest;
    }

    public String getOrderRef() {
        return orderRef;
    }
}
