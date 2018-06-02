package se.tink.backend.system.document.core;

import java.awt.Point;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;

public class Applicant {
    private final String fullName;
    private final String nationalId;
    private final String address;
    private final String postalCode;
    private final String town;
    private final String email;
    private final String phoneNumber;
    private final double monthlyIncome;
    private final Optional<Employment> employment;

    private final PoaDetails poaDetails;
    private final List<Salary> salaries;
    private final Optional<TaxReport> latestTaxReport;
    private final Set<Asset> assets;

    private final List<List<Point>> signature;

    public Applicant(String fullName, String nationalId, String address, String postalCode, String town,
            String email, String phoneNumber, double monthlyIncome,
            Optional<Employment> employment, PoaDetails poaDetails,
            List<Salary> salaries, Optional<TaxReport> latestTaxReport, Set<Asset> assets,
            List<List<Point>> signature) {
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.address = address;
        this.postalCode = postalCode;
        this.town = town;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.monthlyIncome = monthlyIncome;
        this.employment = employment;
        this.poaDetails = poaDetails;
        this.salaries = salaries;
        this.latestTaxReport = latestTaxReport;
        this.assets = assets;
        this.signature = signature;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getTown() {
        return town;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Optional<Employment> getEmployment() {
        return employment;
    }

    public PoaDetails getPoaDetails() {
        return poaDetails;
    }

    public List<Salary> getSalaries() {
        return salaries;
    }

    public Optional<TaxReport> getLatestTaxReport() {
        return latestTaxReport;
    }

    public Set<Asset> getAssets() {
        return assets;
    }

    public Set<String> getAssetsAsStrings() {
        return assets
                .stream()
                .map(asset -> asset.toString())
                .collect(Collectors.toSet());
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public boolean monthlyDifferenceFromTotal(double percentage) {
        if (!getLatestTaxReport().isPresent()) {
            return true;
        }

        double amount = getLatestTaxReport().get().getYearlySalary();
        return Math.abs((monthlyIncome * 12 - amount) / amount) > percentage;
    }

    public boolean isEmployedLessThanXMonths(int months) {
        if (!employment.isPresent()) {
            return false;
        }

        if (!employment.get().getSince().isPresent()) {
            return false;
        }

        DateTime employeeSince = new DateTime(employment.get().getSince().get());
        DateTime xMonthsAgo = new DateTime(getPoaDetails().getSigningDate()).minusMonths(months);

        if (xMonthsAgo.isAfter(employeeSince)) {
            return false;
        }

        return true;
    }

    public List<List<Point>> getSignature() {
        return signature;
    }


}
