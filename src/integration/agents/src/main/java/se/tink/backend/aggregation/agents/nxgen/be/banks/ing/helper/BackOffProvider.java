package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

public class BackOffProvider {

    private final long millisBase;

    public BackOffProvider(long millisBase) {
        this.millisBase = millisBase;
    }

    public long calculate(int retry) {
        return (long) Math.pow(2, retry) * millisBase;
    }
}
