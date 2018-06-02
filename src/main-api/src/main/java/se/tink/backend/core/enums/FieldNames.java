package se.tink.backend.core.enums;

public enum FieldNames {

    // Person 
    
    NAME("name"),
    ADDRESS("address"),
    ZIP_CODE("zipcode"),
    CITY("city"),
    EMPLOYMNET("employment"),
    EMPLOYMNET_TYPE("employmentType"),
    EMPLOYER("employer"),
    EMPLYED_OVER_1_YEAR("employedOverAYear"),
    NBR_CHILDREN("nbrChildren"),
    PERSON_NUMBER("personNumber"),
    PEP("pep"),
    
    // Other loans
    
    OTHER_LOANS("otherLoans"),
    OTHER_LOANS_NAME("otherLoansName"),
    OTHER_LOANS_AMOUNT("otherLoansAmount"),
    OTHER_LOANS_FEE("otherLoansFee"),
    
    // Other real estate
    
    OTHER_REAL_ESTATE("otherRealEstate"),
    OTHER_REAL_ESTATE_NAME("otherRealEstateName"),
    OTHER_REAL_ESTATE_VALUE("otherRealEstateValue"),
    OTHER_REAL_ESTATE_FEE("otherRealEstateFee"),
    OTHER_REAL_ESTATE_SHARE("otherRealEstateShare"),
    OTHER_REAL_ESTATE_LABEL("otherRealEstateLabel"),
    OTHER_REAL_ESTATE_COMMUNITY("otherRealEstateCommunity"),

    // Mortgage
    
    LOANS_ACCOUNTS("loanAccountIds"),
    LOANS_AMOUNT_TO_BORROW("amountToBorrow"),
    LOANS_RESIDENCE_NUMBER("residenceNumber"),
    LOANS_RESIDENCE_VALUE("apartmentValue"),
    LOANS_RESIDENCE_TYPE("residenceType"),
    LOANS_MONTHLY_FEE("monthlyFee"),
    LOANS_HOUSING_SOCIETY("housingSociety"),
    LOANS_NBR_ROOMS("nbrRooms"),
    LOANS_AREA("livingArea"),
    LOANS_SELF_APPLICANT("selfApplicant");
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    private FieldNames(String name) {
        this.name = name;
    }
}
