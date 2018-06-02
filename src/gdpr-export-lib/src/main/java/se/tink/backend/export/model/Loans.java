package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportLoan;

public class Loans {

    private final List<ExportLoan> loans;

    public Loans(List<ExportLoan> loans) {
        this.loans = loans;
    }

    public List<ExportLoan> getLoans() {
        return loans;
    }
}
