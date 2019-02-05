package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class VerifyBankIdRequest extends BankIdRequest<VerifyBankIdModuleInput> {
    public VerifyBankIdRequest(String orderReference) {
        super(new VerifyBankIdModuleInput(orderReference));
    }
}
