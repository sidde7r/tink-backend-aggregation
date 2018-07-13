package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class SignBankIdRequest extends BankIdRequest<SignBankIdModuleInput> {
    public SignBankIdRequest(String orderReference) {
        super(new SignBankIdModuleInput(orderReference));
    }
}
