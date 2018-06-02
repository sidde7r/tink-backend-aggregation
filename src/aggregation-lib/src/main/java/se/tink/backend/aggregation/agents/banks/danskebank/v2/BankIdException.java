package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdResponse;

public class BankIdException extends Exception {
    private final BankIdResponse response;

    public BankIdException(BankIdResponse response) {
        this.response = response;
    }

    public BankIdResponse getResponse() {
        return response;
    }
}
