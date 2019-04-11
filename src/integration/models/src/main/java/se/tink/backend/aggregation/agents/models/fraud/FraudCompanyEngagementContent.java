package se.tink.backend.aggregation.agents.models.fraud;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FraudCompanyEngagementContent extends FraudDetailsContent {

    private Date inDate;
    private Date outDate;
    private String name;
    private String number;
    private List<String> roles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Date getInDate() {
        return inDate;
    }

    public void setInDate(Date inDate) {
        this.inDate = inDate;
    }

    public Date getOutDate() {
        return outDate;
    }

    public void setOutDate(Date outDate) {
        this.outDate = outDate;
    }

    @Override
    public String generateContentId() {
        if (roles == null) {
            return "null";
        }

        return String.valueOf(
                Objects.hash(itemType(), name, number, getRolesSortedLowercase(), outDate));
    }

    private List<String> getRolesSortedLowercase() {
        List<String> rolesSorted = Lists.newArrayList();

        for (String role : roles) {
            rolesSorted.add(role.toLowerCase());
        }

        Collections.sort(rolesSorted);

        return rolesSorted;
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.IDENTITY;
    }
}
