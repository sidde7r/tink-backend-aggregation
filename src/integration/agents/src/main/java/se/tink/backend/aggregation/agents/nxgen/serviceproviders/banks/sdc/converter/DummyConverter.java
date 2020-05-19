package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter;

public class DummyConverter implements AccountNumberToIbanConverter {

    @Override
    public String convertToIban(String accountNumber) {
        return accountNumber;
    }
}
