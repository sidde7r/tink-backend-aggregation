package se.tink.backend.system.document.file.template;

public enum PdfDocumentTemplate {
    POA_AMORTIZATION_TEMPLATE("./data/templates/pdfs/POA_Amortization_empty.pdf"),
    POA_CERTIFICATE_OF_EMPLOYMENT_TEMPLATE("./data/templates/pdfs/POA_Certificate-of-Employment_empty.pdf"),
    POA_HOUSING_COOPERATIVE_TEMPLATE("./data/templates/pdfs/POA_Housing-Cooperative_empty.pdf"),
    SALARY_TEMPLATE("./data/templates/pdfs/Salary-Transactions_empty.pdf"),
    OTHER_INFORMATION_TEMPLATE("./data/templates/pdfs/Other-Information_empty.pdf");

    private final String filePath;

    PdfDocumentTemplate(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
