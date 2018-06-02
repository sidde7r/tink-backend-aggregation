package se.tink.backend.system.document.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.libraries.i18n.Catalog;

public class SwitchMortgageProvider {

    private final String name;
    private final String externalApplicationId;
    private final Applicant applicant;
    private final Optional<CoApplicant> coApplicant;
    private final Residence residence;
    private final Mortgage mortgage;
    private final List<AdditionalServiceInterest> additionalServiceInterests;

    public SwitchMortgageProvider(String name, String externalApplicationId, Applicant applicant,
            Optional<CoApplicant> coApplicant, Residence residence, Mortgage mortgage,
            List<AdditionalServiceInterest> additionalServiceInterests) {
        this.name = name;
        this.externalApplicationId = externalApplicationId;
        this.applicant = applicant;
        this.coApplicant = coApplicant;
        this.residence = residence;
        this.mortgage = mortgage;
        this.additionalServiceInterests = additionalServiceInterests;
    }

    public String getName() {
        return name;
    }

    public String getExternalApplicationId() {
        return externalApplicationId;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public Optional<CoApplicant> getCoApplicant() {
        return coApplicant;
    }

    public Residence getResidence() {
        return residence;
    }

    public List<AdditionalServiceInterest> getAdditionalServiceInterests() {
        return additionalServiceInterests;
    }

    public String getMessageBody(int monthsOfEmployment, double percent) {

        String coApplicant = "N/A";
        if (getCoApplicant().isPresent()) {
            coApplicant = getCoApplicant().get().getName();
        }

        String housingCoop = "N/A";
        if (getResidence().getHousingCooperative().isPresent()) {
            housingCoop = getResidence().getHousingCooperative().get();
        }

        String records = Catalog.format("<pre>{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}</pre>",
                getApplicant().getFullName(),
                getApplicant().getNationalId(),
                getExternalApplicationId(),
                getName(),
                getApplicant().getPhoneNumber(),
                getApplicant().getEmail(),
                getApplicant().getPoaDetails().getSigningDate(),
                coApplicant,
                housingCoop,
                getYesOrNo(isBankSeb() &&
                        (getApplicant().isEmployedLessThanXMonths(monthsOfEmployment) || getApplicant()
                                .monthlyDifferenceFromTotal(percent))),
                "SIGNED",
                getResidence().getMortgageProvider()
        );

        String body = records
                + "<br><br>"
                + "<br>Full Name: " + getApplicant().getFullName()
                + "<br>National id: " + getApplicant().getNationalId()
                + "<br>Email: " + getApplicant().getEmail()
                + "<br>Current Bank: " + getResidence().getMortgageProvider()
                + "<br>External application id: " + getExternalApplicationId()
                + "<br>Phone Number: " + getApplicant().getPhoneNumber()
                + "<br>Verify Salary (Certificate of Employment): <strong>" + getYesOrNo(
                isBankSeb() &&
                        (getApplicant().isEmployedLessThanXMonths(monthsOfEmployment) || getApplicant()
                                .monthlyDifferenceFromTotal(percent))) + "</strong>"
                + "<br>Signed date: " + getApplicant().getPoaDetails().getSigningDate();

        body += "<br>";

        body += "<br> Current Town: " + getApplicant().getTown();
        body += "<br> Current Address: " + getApplicant().getAddress();

        body += "<br>";

        if (getMortgage().getSecurity().isPresent()) {
            body += "<br> Mortgage security town: " + getMortgage().getSecurity().get().getTown();
            body += "<br> Mortgage security address: " + getMortgage().getSecurity().get().getAddress();
        }

        if (getResidence().getHousingCooperative().isPresent()) {
            body += "<br>";
            body += "<br> Housing cooperative: " + getResidence().getHousingCooperative().get();
        }

        if (getCoApplicant().isPresent()) {
            body += "<br>";
            body += "<br>Co Applicant name: " + getCoApplicant().get().getName();
            body += "<br>Co Applicant national id: " + getCoApplicant().get().getNationalId();
        }

        if (!getApplicant().getEmployment().isPresent()) {
            body += "<br>";
            body += "<br>Applicant's employment record not found.";
        } else {
            if (!getApplicant().getEmployment().get().getSince().isPresent()) {
                body += "<br>Applicant's employment start date record not found.";
            }
        }

        if (!getApplicant().getLatestTaxReport().isPresent()) {
            body += "<br>Tax report was not found.";
        }
        return body;
    }

    private String getYesOrNo(boolean var) {
        if (var) {
            return "YES";
        } else {
            return "NO";
        }
    }

    private boolean isBankSeb() {
        return Objects.equals(name, "seb-bankid");
    }

    public Mortgage getMortgage() {
        return mortgage;
    }

    public Map<String, String> getSebAdditionalInformation() {
        Map<String, String> map = Maps.newHashMap();

        if (!getMortgage().getLoanNumbers().isEmpty()) {
            map.put("Lånenummer hos nuvarande bank:", Joiner.on(", ").join(getMortgage().getLoanNumbers()));
        }

        map.put("Lånet omfattas av amorteringskravet:", getYesOrNo(getMortgage().getHasAmortizationRequirement()));
        if (!getAdditionalServiceInterests().isEmpty()) {
            for (AdditionalServiceInterest additionalServiceInterest : getAdditionalServiceInterests()) {
                map.put(additionalServiceInterest.getName() + ":",
                        setToString(additionalServiceInterest.getInterest(), "-", "."));
            }
        }

        if (getMortgage().getAccountNumber().isPresent()) {
            map.put("Konto för autogiro-betalning av ränta och amortering:", getMortgage().getAccountNumber().get());
        }

        if (!getApplicant().getAssets().isEmpty()) {
            map.put("Assets:", setToString(getApplicant().getAssetsAsStrings(), "-", "."));
        }
        return map;
    }

    private String setToString(Set<String> set, String prefix, String postfix) {
        String ret = "";
        for (String s : set) {
            ret += prefix + " " + s + postfix + " ";
        }
        return ret;
    }
}
