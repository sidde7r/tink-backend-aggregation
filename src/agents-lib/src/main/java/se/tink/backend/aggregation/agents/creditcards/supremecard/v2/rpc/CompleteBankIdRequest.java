package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteBankIdRequest {
    private String orderRef;

    public static CompleteBankIdRequest createRequestFromCollectBankIdResponse(OrderBankIdResponse orderBankIdResponse) {
        CompleteBankIdRequest completeBankIdRequest = new CompleteBankIdRequest();
        completeBankIdRequest.setOrderRef(orderBankIdResponse.getOrderRef());

        return completeBankIdRequest;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }
}
