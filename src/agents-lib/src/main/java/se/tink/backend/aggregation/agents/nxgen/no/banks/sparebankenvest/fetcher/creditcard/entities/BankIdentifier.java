package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

public class BankIdentifier {
    private String cardNumberGuid;
    private String kidGuid;

    public BankIdentifier(String cardNumberGuid, String kidGuid) {
        this.cardNumberGuid = cardNumberGuid;
        this.kidGuid = kidGuid;
    }

    public BankIdentifier(String bankIdentifier) {
        if (bankIdentifier == null) {
            throw new IllegalStateException("Missing bank identifier");
        }
        String[] bankIdentifierParts = bankIdentifier.split("\n");
        if (bankIdentifierParts.length != 2) {
            throw new IllegalStateException("Invalid bank identifier " + bankIdentifier);
        }

        this.cardNumberGuid = bankIdentifierParts[0];
        this.kidGuid = bankIdentifierParts[1];
    }

    public String getCardNumberGuid() {
        return cardNumberGuid;
    }

    public String getKidGuid() {
        return kidGuid;
    }

    public String getBankIdentifier() {
        return String.format("%s\n%s", cardNumberGuid, kidGuid);
    }
}
