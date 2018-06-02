package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportTransaction;

public class Transactions {

    private final List<ExportTransaction> transactions;

    public Transactions(
            List<ExportTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<ExportTransaction> getTransactions() {
        return transactions;
    }
}
