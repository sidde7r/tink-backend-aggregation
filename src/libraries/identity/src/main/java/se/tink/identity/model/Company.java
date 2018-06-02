package se.tink.libraries.identity.model;

import java.util.Objects;

public class Company {
    private String name;
    private String companyNumber;

    public Company(String name, String companyNumber) {
        this.name = name;
        this.companyNumber = companyNumber;
    }

    public String getName() {
        return name;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public static Company of(String name, String companyNumber) {
        return new Company(name, companyNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (this.getClass() != obj.getClass()) return false;
        Company c = (Company) obj;
        return Objects.equals(name, c.getName()) && Objects.equals(companyNumber, c.getCompanyNumber());
    }
}
