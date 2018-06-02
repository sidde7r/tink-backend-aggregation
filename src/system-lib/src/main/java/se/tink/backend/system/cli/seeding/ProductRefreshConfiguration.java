package se.tink.backend.system.cli.seeding;

public class ProductRefreshConfiguration {
    private boolean isDryRun;
    private boolean isVerbose;
    private int ratePerSecond = 5;
    private final Scope scope;

    public ProductRefreshConfiguration(Scope scope) {
        this.scope = scope;
    }

    public int getRatePerSecond() {
        return ratePerSecond;
    }
    
    public boolean isDryRun() {
        return isDryRun;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public Scope getScope() {
        return scope;
    }

    public void setDryRun(boolean dryRun) {
        isDryRun = dryRun;
    }
    
    public void setRatePerSecond(int ratePerSecond) {
        this.ratePerSecond = ratePerSecond;
    }

    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    public enum Scope {
        REFRESH_PRODUCTS_COMMAND,
        SYSTEM_SERVICE
    }
}
