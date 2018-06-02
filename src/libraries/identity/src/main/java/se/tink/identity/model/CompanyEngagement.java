package se.tink.libraries.identity.model;

import java.util.Date;
import java.util.List;

public class CompanyEngagement {
    private Company company;
    private List<Role> roles;
    private Date inDate;

    public CompanyEngagement(Company company, List<Role> roles, Date inDate) {
        this.company = company;
        this.roles = roles;
        this.inDate = inDate;
    }

    public Company getCompany() {
        return company;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Date getInDate() {
        return inDate;
    }

    public static CompanyEngagement of(Company company, List<Role> roles, Date inDate) {
        return new CompanyEngagement(company, roles, inDate);
    }

}
