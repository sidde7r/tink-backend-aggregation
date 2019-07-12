package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

public abstract class Account {
    protected String iban;
    protected String bban;
    protected String pan;
    protected String maskedPan;
    protected String msisdn;
    protected String currency;
}
