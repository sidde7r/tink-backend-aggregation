package se.tink.backend.core;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

    private String address;
    private String community;
    private String county;
    private String email;
    private boolean fskattRegistered;
    private String name;
    @Id
    private String orgNumber;
    private String phone;
    private Date registered;
    private String snicode;
    private String status;
    private Date statusUpdated;
    private String town;
    private String url;
    private String zipcode;

    public String getAddress() {
        return address;
    }

    public String getCommunity() {
        return community;
    }

    public String getCounty() {
        return county;
    }

    public String getEmail() {
        return email;
    }

    public boolean getFskattRegistered() {
        return fskattRegistered;
    }

    public String getName() {
        return name;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public String getPhone() {
        return phone;
    }

    public Date getRegistered() {
        return registered;
    }

    public String getSnicode() {
        return snicode;
    }

    public String getStatus() {
        return status;
    }

    public Date getStatusUpdated() {
        return statusUpdated;
    }

    public String getTown() {
        return town;
    }

    public String getUrl() {
        return url;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFskattRegistered(boolean fskattRegistered) {
        this.fskattRegistered = fskattRegistered;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    public void setSnicode(String snicode) {
        this.snicode = snicode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusUpdated(Date statusUpdated) {
        this.statusUpdated = statusUpdated;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
