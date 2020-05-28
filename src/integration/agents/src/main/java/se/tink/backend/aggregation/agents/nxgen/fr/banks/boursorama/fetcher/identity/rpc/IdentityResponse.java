package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.entity.MifidKeysEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Getter
public class IdentityResponse {
    private int activitySector;
    private int age;
    private String agencyName;
    private String alternativeNumber;
    private int annualIncome;
    private String birthCity;
    private String birthCountry;
    private String birthCountryValue;
    private String birthName;
    private String birthZipCode;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthdayDate;

    private String civilityCode;
    private String customerContactId;
    private String customerId;
    private String customerRange;
    private boolean declareIsf;
    private int dependentChildren;
    private String email;
    private String employeeSince;
    private String employer;
    private String faxNumber;
    private String firstName;
    private String fiscalAddress1;
    private String fiscalAddress2;
    private String fiscalAddress3;
    private String fiscalCity;
    private String fiscalCountry;
    private String fiscalCountryValue;
    private String fiscalZipCode;
    private String formerOccupation;
    private String gender;
    private int genderTitleCode;
    private boolean generalPublic;
    private int hasEmployedActivity;
    private String homeAddress1;
    private String homeAddress2;
    private String homeAddress3;
    private boolean homeAddressNPAI;
    private String homeCity;
    private String homeCountry;
    private String homeCountryValue;
    private String homeZipCode;
    private int housingSituation;
    private List<Object> incapables;
    private int incomes;
    private String lastName;
    private String maidenName;
    private boolean majorityTransitionStatus;
    private int maritalStatus;
    private int matrimonialPropertyRegime;
    private List<MifidKeysEntity> mifidKeys;
    private String mobilePhoneNumber;
    private String nationality;
    private String nationalityValue;
    private String nif;
    private String numTva;
    private String occupation;
    private List<Object> onBehalfOn;
    private int pep;
    private int pepCloseTo;
    private String pepFunction;
    private String pepRelative;
    private String phoneAuthentication;
    private String phoneNumber;
    private String professionalNumber;
    private String propertyAssetsEstimate;
    private List<Object> representedBy;
    private boolean retirement;
    private int socioProfessionalCategory;
    private String spouseFirstName;
    private boolean stockMarketListedCompany;
    private String title;
    private int typeContact;
    private boolean usaTaxPayer;
    private int usaTaxPayerNumerical;
}
