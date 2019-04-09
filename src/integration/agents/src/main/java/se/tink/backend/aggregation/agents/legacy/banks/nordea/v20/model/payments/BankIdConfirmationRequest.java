package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

public class BankIdConfirmationRequest {
    private BankIdConfirmationIn bankIdConfirmationIn;

    public BankIdConfirmationIn getBankIdConfirmationIn() {
        return bankIdConfirmationIn;
    }

    public void setBankIdConfirmationIn(BankIdConfirmationIn bankIdConfirmationIn) {
        this.bankIdConfirmationIn = bankIdConfirmationIn;
    }

    public BankIdConfirmationRequest(String orderRef) {
        bankIdConfirmationIn = new BankIdConfirmationIn();
        bankIdConfirmationIn.setOrderRef(orderRef);
    }
}
