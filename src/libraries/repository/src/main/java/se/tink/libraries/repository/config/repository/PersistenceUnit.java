package se.tink.libraries.repository.config.repository;

public enum PersistenceUnit {
    // !!!  Make sure that all PersistenceUnit names exist in `persistence.xml`  !!!
    AGGREGATION("tink-aggregation-api", AggregationRepositoryConfiguration.class, false),
    PROVIDER("tink-provider-api", ProviderRepositoryConfiguration.class, true);

    public static PersistenceUnit fromName(String name) {
        for (PersistenceUnit persistenceUnit : PersistenceUnit.values()) {
            if (persistenceUnit.getName().equals(name)) {
                return persistenceUnit;
            }
        }
        throw new IllegalArgumentException("Could not find persistence unit name: " + name);
    }

    private Class<? extends RepositoryConfigurator> configuratorKlass;

    private String name;

    private boolean canAccessTransactions;

    private PersistenceUnit(
            String name,
            Class<? extends RepositoryConfigurator> configuratorKlass,
            boolean canAccessTransactions) {
        this.name = name;
        this.configuratorKlass = configuratorKlass;
        this.canAccessTransactions = canAccessTransactions;
    }

    public Class<? extends RepositoryConfigurator> getConfiguratorKlass() {
        return configuratorKlass;
    }

    public String getName() {
        return name;
    }

    public boolean canAccessTransactions() {
        return canAccessTransactions;
    }
}
