package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

public interface BankdataCryptoComponentsProvider {

    BankdataCryptoHelperStateGenerator provideGenerator();

    BankdataCryptoHelper provideHelper();
}
