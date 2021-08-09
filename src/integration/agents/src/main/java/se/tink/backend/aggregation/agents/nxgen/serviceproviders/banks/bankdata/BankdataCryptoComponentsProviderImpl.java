package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

public class BankdataCryptoComponentsProviderImpl implements BankdataCryptoComponentsProvider {

    @Override
    public BankdataCryptoHelperStateGenerator provideGenerator() {
        return new BankdataCryptoHelperStateGenerator();
    }

    @Override
    public BankdataCryptoHelper provideHelper() {
        return new BankdataCryptoHelper();
    }
}
