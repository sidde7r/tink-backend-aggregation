package se.tink.backend.core;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "companies_engagements")
public class CompanyEngagement {

    private Date fromDate;
    @Id
    private String id;
    private String orgNumber;
    private String personNumber;
    private String roleName;
    private Date toDate;

    public CompanyEngagement() {
        id = StringUtils.generateUUID();
    }

    public Date getFromDate() {
        return fromDate;
    }

    public String getId() {
        return id;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public String getRoleName() {
        return roleName;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}
