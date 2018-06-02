package se.tink.backend.grpc.v1.converter.category;

public class CategoryTree {
    private CategoryNode expenses;
    private CategoryNode income;
    private CategoryNode transfers;

    public CategoryNode getExpenses() {
        return expenses;
    }

    public void setExpenses(CategoryNode expenses) {
        this.expenses = expenses;
    }

    public CategoryNode getIncome() {
        return income;
    }

    public void setIncome(CategoryNode income) {
        this.income = income;
    }

    public CategoryNode getTransfers() {
        return transfers;
    }

    public void setTransfers(CategoryNode transfers) {
        this.transfers = transfers;
    }
}
