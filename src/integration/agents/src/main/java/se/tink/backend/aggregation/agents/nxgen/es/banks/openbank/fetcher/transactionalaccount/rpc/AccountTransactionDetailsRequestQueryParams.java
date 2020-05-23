package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc;

public class AccountTransactionDetailsRequestQueryParams {
    private String productCodeOld;
    private String contractNumberOld;
    private String productCodeNew;
    private String contractNumberNew;
    private String movementOfTheDayIndex;
    private String dateNoted;

    public String getProductCodeOld() {
        return productCodeOld;
    }

    public String getContractNumberOld() {
        return contractNumberOld;
    }

    public String getProductCodeNew() {
        return productCodeNew;
    }

    public String getContractNumberNew() {
        return contractNumberNew;
    }

    public String getMovementOfTheDayIndex() {
        return movementOfTheDayIndex;
    }

    public String getDateNoted() {
        return dateNoted;
    }
}
