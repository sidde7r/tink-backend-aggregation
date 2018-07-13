package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class InitBankIdRequest extends BankIdRequest<InitBankIdModuleInput> {
    public InitBankIdRequest(String swedishLoginId) {
        super(new InitBankIdModuleInput(swedishLoginId));
    }
}