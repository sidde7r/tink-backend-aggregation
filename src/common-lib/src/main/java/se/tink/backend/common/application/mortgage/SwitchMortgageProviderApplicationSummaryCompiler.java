package se.tink.backend.common.application.mortgage;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.application.ApplicationSummaryCompiler;
import se.tink.backend.common.application.field.ApplicationFieldStrings;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.Currency;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.application.FieldData;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.utils.ApplicationUtils;

public class SwitchMortgageProviderApplicationSummaryCompiler implements ApplicationSummaryCompiler {

    private final CurrencyRepository currencyRepository;
    
    public SwitchMortgageProviderApplicationSummaryCompiler(final RepositoryFactory repositoryFactory) {
        this.currencyRepository = repositoryFactory.getRepository(CurrencyRepository.class);
    }
    
    @Override
    public List<ConfirmationFormListData> getSummary(GenericApplication genericApplication,
            User user, Optional<ProductArticle> productArticle) {

        List<ConfirmationFormListData> details = Lists.newArrayList();
        
        if (genericApplication == null) {
            return details;
        }

        Formatter formatter = new Formatter(user);
        
        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = FluentIterable
                .from(genericApplication.getFieldGroups())
                .index(GenericApplicationFieldGroup::getName);
        
        Optional<GenericApplicationFieldGroup> applicantSubGroup = ApplicationUtils.getApplicant(fieldGroupByName);
        Optional<GenericApplicationFieldGroup> coApplicantSubGroup = ApplicationUtils.getCoApplicant(fieldGroupByName);
        Optional<GenericApplicationFieldGroup> currentMortgageGroup = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);
        Optional<GenericApplicationFieldGroup> mortgageSecurityGroup = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);
        Optional<GenericApplicationFieldGroup> householdGroup = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.HOUSEHOLD);
        Optional<GenericApplicationFieldGroup> bankServicesGroup = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.BANK_SERVICES);

        // Mortgage
        if (currentMortgageGroup.isPresent()) {
            details.add(getCurrentMortgageforSummary(currentMortgageGroup.get(), formatter, productArticle));
        }

        // Mortgage security
        if (mortgageSecurityGroup.isPresent()) {
            details.add(getPropertyForSummary(mortgageSecurityGroup.get(), formatter));
        }

        // Applicant
        if (applicantSubGroup.isPresent()) {
            ConfirmationFormListData applicantData = getApplicantsForSummary(applicantSubGroup.get(), formatter);
            if (applicantData.isPopulated()) {
                details.add(applicantData);
            }

            // CSN loan
            ConfirmationFormListData studentLoanData = getCsnLoanForSummary(applicantSubGroup.get(), formatter);
            if (studentLoanData.isPopulated()) {
                details.add(studentLoanData);
            }

            // Other loans
            ConfirmationFormListData otherLoansData = getOtherLoansForSummary(applicantSubGroup.get(), formatter);
            if (otherLoansData.isPopulated()) {
                details.add(otherLoansData);
            }

            // Other assets
            ConfirmationFormListData otherAssetsData = getOtherAssetsForSummary(applicantSubGroup.get(), formatter);
            if (otherAssetsData.isPopulated()) {
                details.add(otherAssetsData);
            }

            // Deferred capital gain tax
            ConfirmationFormListData deferredCapitalGainsTaxData = getDeferredCapitalGainsTaxForSummary(
                    applicantSubGroup.get(), formatter);
            if (deferredCapitalGainsTaxData.isPopulated()) {
                details.add(deferredCapitalGainsTaxData);
            }

            // Other properties
            ConfirmationFormListData otherPropertiesData = getOtherPropertiesForSummary(applicantSubGroup.get(),
                    formatter);
            if (otherPropertiesData.isPopulated()) {
                details.add(otherPropertiesData);
            }

            // Alimony
            ConfirmationFormListData alimonyData = getAlimonyForSummary(applicantSubGroup.get(), formatter);
            if (alimonyData.isPopulated()) {
                details.add(alimonyData);
            }
        }

        // Co applicant
        if (coApplicantSubGroup.isPresent()) {
            details.add(getCoApplicantForSummary(coApplicantSubGroup.get(), formatter));
            
            // Other loans
            ConfirmationFormListData otherLoansData = getOtherLoansForSummary(coApplicantSubGroup.get(), formatter);
            if (otherLoansData.isPopulated()) {
                details.add(otherLoansData);
            }
            
            // Other properties
            ConfirmationFormListData otherPropertiesData = getOtherPropertiesForSummary(coApplicantSubGroup.get(),
                    formatter);
            if (otherPropertiesData.isPopulated()) {
                details.add(otherPropertiesData);
            }
        }

        // Household
        if (householdGroup.isPresent()) {
            details.add(getHouseholdForSummary(householdGroup.get(), formatter));
            
            // Bailment
            ConfirmationFormListData bailmentData = getBailmentForSummary(householdGroup.get(), formatter);
            if (bailmentData.isPopulated()) {
                details.add(bailmentData);
            }
        }

        // KYC
        if (applicantSubGroup.isPresent()) {
            details.add(getKycForSummary(applicantSubGroup.get(), bankServicesGroup, formatter));
        }

        return details;
    }

    private ConfirmationFormListData getOtherAssetsForSummary(GenericApplicationFieldGroup group,
            Formatter formatter) {
        ConfirmationFormListData currentAssetsData = new ConfirmationFormListData();
        currentAssetsData.setTitle("Övriga tillgångar");
        FieldData currentAssets = new FieldData();
        currentAssetsData.addField(currentAssets);

        List<GenericApplicationFieldGroup> otherLoans = ApplicationUtils.getSubGroups(group,
                GenericApplicationFieldGroupNames.ASSET);

        if (otherLoans.isEmpty()) {
            currentAssets.addRow("Inga övriga tillgångar");
        } else {

            currentAssets.setTitle("Tillgångar");

            double totalAssetValue = 0;

            for (GenericApplicationFieldGroup subgroup : otherLoans) {
                Optional<Double> value = subgroup.tryGetFieldAsDouble(ApplicationFieldName.VALUE);
                Optional<String> name = subgroup.tryGetField(ApplicationFieldName.NAME);
                if (value.isPresent() && name.isPresent()) {
                    currentAssets.addRow(name.get(), formatter.formatCurrency(value.get()));
                    totalAssetValue += value.get();
                }
            }

            FieldData totalAmount = new FieldData();
            totalAmount.setTitle("Totalt tillgångsvärde");
            totalAmount.addRow(formatter.formatCurrency(totalAssetValue));
            currentAssetsData.addField(totalAmount);
        }

        return currentAssetsData;

    }

    private ConfirmationFormListData getCurrentMortgageforSummary(GenericApplicationFieldGroup group,
            Formatter formatter, Optional<ProductArticle> productArticle) {

        ConfirmationFormListData mortgageBlock = new ConfirmationFormListData();
        mortgageBlock.setTitle("Bolån");

        // Current mortgages
        FieldData currentMortgageFieldData = new FieldData();
        currentMortgageFieldData.setTitle("Befintliga lån");
        currentMortgageFieldData.addRow(
                Catalog.format("{0} {1} ränta",
                        group.getField(ApplicationFieldName.LENDER),
                        formatter.formatPercent(group.getFieldAsDouble(ApplicationFieldName.INTEREST_RATE))),
                formatter.formatCurrency(group.getFieldAsDouble(ApplicationFieldName.AMOUNT)));

        Optional<Boolean> amortizationRequirement = group
                .tryGetFieldAsBool(ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT);

        if (amortizationRequirement.isPresent()) {
            if (amortizationRequirement.get()) {
                currentMortgageFieldData.addRow("Omfattas av amorteringskravet");
            } else {
                currentMortgageFieldData.addRow("Omfattas inte av amorteringskravet");
            }
        }

        mortgageBlock.addField(currentMortgageFieldData);

        // New mortgage
        FieldData newLoan = new FieldData();
        newLoan.setTitle("Nytt lån");
        if (productArticle.isPresent()) {
            Double rate = (Double) productArticle.get().getProperty(ProductPropertyKey.INTEREST_RATE);
            
            if (productArticle.get().hasProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)) {
                rate -= (Double) productArticle.get().getProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT);
            }
            
            newLoan.addRow(
                    Catalog.format("{0} {1} ränta",
                            productArticle.get().getName(),
                            formatter.formatPercent(rate)),
                    formatter.formatCurrency(group.getFieldAsDouble(ApplicationFieldName.AMOUNT)));
            mortgageBlock.addField(newLoan);
        }

        return mortgageBlock;
    }

    private ConfirmationFormListData getPropertyForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData propertyBlock = new ConfirmationFormListData();
        propertyBlock.setTitle("Bostad");

        FieldData streetAdress = new FieldData();
        streetAdress.setTitle("Gatuadress");
        streetAdress.addRow(group.getField(ApplicationFieldName.STREET_ADDRESS));
        propertyBlock.addField(streetAdress);

        FieldData postalCode = new FieldData();
        postalCode.setTitle("Postnummer");
        postalCode.addRow(group.getField(ApplicationFieldName.POSTAL_CODE));
        propertyBlock.addField(postalCode);

        FieldData town = new FieldData();
        town.setTitle("Ort");
        town.addRow(group.getField(ApplicationFieldName.TOWN));
        propertyBlock.addField(town);

        String propertyType = group.getField(ApplicationFieldName.PROPERTY_TYPE);
        
        switch (propertyType) {
        case ApplicationFieldOptionValues.APARTMENT:
            populateApartmentForSummary(propertyBlock, group, formatter);
            break;
        case ApplicationFieldOptionValues.HOUSE:
        case ApplicationFieldOptionValues.VACATION_HOUSE:
            populateHouseForSummary(propertyBlock, group, formatter);
            break;
        default:
            // Unknown property type.
        }

        FieldData marketValue = new FieldData();
        marketValue.setTitle("Uppskattat marknadsvärde");
        marketValue.addRow(formatter.formatCurrency(
                group.getFieldAsDouble(ApplicationFieldName.ESTIMATED_MARKET_VALUE)));
        propertyBlock.addField(marketValue);

        return propertyBlock;
    }
    
    private void populateApartmentForSummary(ConfirmationFormListData propertyBlock,
            GenericApplicationFieldGroup group, Formatter formatter) {
        
        FieldData propertyType = new FieldData();
        propertyType.setTitle("Typ av bostad");
        propertyType.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(group
                .getField(ApplicationFieldName.PROPERTY_TYPE))));
        propertyBlock.addField(propertyType);

        FieldData housingComunityName = new FieldData();
        housingComunityName.setTitle("Bostadsrättsföreningens namn");
        housingComunityName.addRow(group.getField(ApplicationFieldName.HOUSING_COMMUNITY_NAME));
        propertyBlock.addField(housingComunityName);

        FieldData numberOfRooms = new FieldData();
        numberOfRooms.setTitle("Antal rum");
        numberOfRooms.addRow(group.getField(ApplicationFieldName.NUMBER_OF_ROOMS));
        propertyBlock.addField(numberOfRooms);

        FieldData livingArea = new FieldData();
        livingArea.setTitle("Boyta");
        livingArea.addRow(group.getField(ApplicationFieldName.LIVING_AREA));
        propertyBlock.addField(livingArea);

        FieldData monthlyHousingComunityFee = new FieldData();
        monthlyHousingComunityFee.setTitle("Månadsavgift");
        monthlyHousingComunityFee.addRow(formatter.formatCurrency(
                group.getFieldAsDouble(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE)));
        propertyBlock.addField(monthlyHousingComunityFee);

        Optional<Double> monthlyAmoritization = group.tryGetFieldAsDouble(ApplicationFieldName.MONTHLY_AMORTIZATION);
        if (monthlyAmoritization.isPresent()) {
            FieldData ma = new FieldData();
            ma.setTitle("Amortering per månad");
            ma.addRow(formatter.formatCurrency(monthlyAmoritization.get()));
            propertyBlock.addField(ma);
        }
    }
    
    private void populateHouseForSummary(ConfirmationFormListData propertyBlock,
            GenericApplicationFieldGroup group, Formatter formatter) {
        
        Optional<String> municipalityCode = group.tryGetField(ApplicationFieldName.MUNICIPALITY);
        if (municipalityCode.isPresent()) {
            FieldData m = new FieldData();
            m.setTitle("Kommun");
            Optional<String> municipalityName = CountyCache.findMunicipalityName(municipalityCode.get());
            m.addRow(municipalityName.orElse(municipalityCode.get()));
            propertyBlock.addField(m);
        }
        
        FieldData propertyType = new FieldData();
        propertyType.setTitle("Typ av bostad");
        propertyType.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(group
                .getField(ApplicationFieldName.PROPERTY_TYPE))));
        propertyBlock.addField(propertyType);

        Optional<String> cadastral = group.tryGetField(ApplicationFieldName.CADASTRAL);
        if (cadastral.isPresent()) {
            FieldData c = new FieldData();
            c.setTitle("Fastighetsbeteckning");
            c.addRow(group.getField(ApplicationFieldName.CADASTRAL));
            propertyBlock.addField(c);
        }
        
        Optional<String> numberOfRooms = group.tryGetField(ApplicationFieldName.NUMBER_OF_ROOMS);
        if (numberOfRooms.isPresent()) {        
            FieldData nor = new FieldData();
            nor.setTitle("Antal rum");
            nor.addRow(numberOfRooms.get());
            propertyBlock.addField(nor);
        }

        Optional<String> livingArea = group.tryGetField(ApplicationFieldName.LIVING_AREA);
        if (livingArea.isPresent()) {
            FieldData livingAreaField = new FieldData();
            livingAreaField.setTitle("Boyta");
            livingAreaField.addRow(group.getField(ApplicationFieldName.LIVING_AREA));
            propertyBlock.addField(livingAreaField);
        }
        
        Optional<Double> operatingCost = group.tryGetFieldAsDouble(ApplicationFieldName.OPERATING_COST);
        if (operatingCost.isPresent()) {
            FieldData oc = new FieldData();
            oc.setTitle("Driftkostnad");
            oc.addRow(Catalog.format("{0}/mån", formatter.formatCurrency(operatingCost.get())));
            propertyBlock.addField(oc);
        }
        
        Optional<Double> purchasePrice = group.tryGetFieldAsDouble(ApplicationFieldName.PURCHASE_PRICE);
        if (purchasePrice.isPresent()) {
            FieldData pp = new FieldData();
            pp.setTitle("Köpeskilling");
            pp.addRow(formatter.formatCurrency(purchasePrice.get()));
            propertyBlock.addField(pp);
        }
        
        Optional<Double> assessedValue = group.tryGetFieldAsDouble(ApplicationFieldName.ASSESSED_VALUE);
        if (assessedValue.isPresent()) {
            FieldData av = new FieldData();
            av.setTitle("Taxeringsvärde");
            av.addRow(formatter.formatCurrency(assessedValue.get()));
            propertyBlock.addField(av);
        }
    }

    private ConfirmationFormListData getApplicantsForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData applicantsBlock = new ConfirmationFormListData();
        applicantsBlock.setTitle("Personuppgifter");

        Optional<String> fullName = group.tryGetField(ApplicationFieldName.NAME);
        if (fullName.isPresent()) {
            FieldData name = new FieldData();
            name.setTitle("Namn");
            name.addRow(fullName.get());
            applicantsBlock.addField(name);
        }

        Optional<String> firstName = group.tryGetField(ApplicationFieldName.FIRST_NAME);
        if (firstName.isPresent()) {
            FieldData fn = new FieldData();
            fn.setTitle("Förnamn");
            fn.addRow(firstName.get());
            applicantsBlock.addField(fn);
        }

        Optional<String> lastName = group.tryGetField(ApplicationFieldName.LAST_NAME);
        if (lastName.isPresent()) {
            FieldData ln = new FieldData();
            ln.setTitle("Efternamn");
            ln.addRow(lastName.get());
            applicantsBlock.addField(ln);
        }

        FieldData personalNumber = new FieldData();
        personalNumber.setTitle("Personnummer");
        personalNumber.addRow(group.getField(ApplicationFieldName.PERSONAL_NUMBER));
        applicantsBlock.addField(personalNumber);

        FieldData email = new FieldData();
        email.setTitle("E-post");
        email.addRow(group.getField("email"));
        applicantsBlock.addField(email);

        FieldData phoneNumber = new FieldData();
        phoneNumber.setTitle("Telefonnummer");
        phoneNumber.addRow(group.getField(ApplicationFieldName.PHONE_NUMBER));
        applicantsBlock.addField(phoneNumber);

        FieldData monthlyIncome = new FieldData();
        monthlyIncome.setTitle("Månadsinkomst");
        monthlyIncome.addRow(Catalog.format("{0}/mån",
                formatter.formatCurrency(group.getFieldAsDouble(ApplicationFieldName.MONTHLY_INCOME))));
        applicantsBlock.addField(monthlyIncome);

        FieldData employment = new FieldData();
        employment.setTitle("Huvudsaklig sysselsättning");
        employment.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(group
                .getField(ApplicationFieldName.EMPLOYMENT_TYPE))));
        applicantsBlock.addField(employment);

        Optional<String> employerOrCompanyName = group.tryGetField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
        if (employerOrCompanyName.isPresent()) {
            FieldData employerOrCompanyNameData = new FieldData();
            employerOrCompanyNameData.setTitle("Arbetsgivarens/företagets namn");
            employerOrCompanyNameData.addRow(employerOrCompanyName.get());
            applicantsBlock.addField(employerOrCompanyNameData);
        }

        Optional<String> employerSince = group.tryGetField(ApplicationFieldName.EMPLOYEE_SINCE);
        if (employerSince.isPresent()) {
            FieldData es = new FieldData();
            es.setTitle("Anställd sedan");
            es.addRow(employerSince.get());
            applicantsBlock.addField(es);
        }

        Optional<String> profession = group.tryGetField(ApplicationFieldName.PROFESSION);
        if (profession.isPresent()) {
            FieldData professionField = new FieldData();
            professionField.setTitle("Yrkesroll");
            professionField.addRow(profession.get());
            applicantsBlock.addField(professionField);
        }

        return applicantsBlock;
    }

    private ConfirmationFormListData getCsnLoanForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData csnLoanData = new ConfirmationFormListData();
        csnLoanData.setTitle("CSN-lån");

        Optional<Double> loanAmount = group.tryGetFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_AMOUNT);
        Optional<Double> loanMonthlyCost = group.tryGetFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST);
        
        if (loanAmount.isPresent() || loanMonthlyCost.isPresent()) {
        
            FieldData hasLoan = new FieldData();
            hasLoan.setTitle("Har du studielån hos CSN?");
            csnLoanData.addField(hasLoan);

            if (loanAmount.orElse(0d) > 0 || loanMonthlyCost.orElse(0d) > 0) {
                
                hasLoan.addRow("Ja");

                if (loanAmount.orElse(0d) > 0) {
                    FieldData la = new FieldData();
                    la.setTitle("Lånebelopp");
                    la.addRow(formatter.formatCurrency(loanAmount.get()));
                    csnLoanData.addField(la);
                }

                if (loanMonthlyCost.orElse(0d) > 0) {
                    FieldData mc = new FieldData();
                    mc.setTitle("Månadskostnad för studielån");
                    mc.addRow(formatter.formatCurrency(loanMonthlyCost.get()));
                    csnLoanData.addField(mc);
                }
            } else {
                hasLoan.addRow("Nej");
            }
        }

        return csnLoanData;
    }

    private ConfirmationFormListData getOtherLoansForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData currentLoansData = new ConfirmationFormListData();
        currentLoansData.setTitle("Övriga lån");
        FieldData currentLoans = new FieldData();
        currentLoansData.addField(currentLoans);

        List<GenericApplicationFieldGroup> otherLoans = ApplicationUtils.getSubGroups(group,
                GenericApplicationFieldGroupNames.LOAN);

        if (otherLoans.isEmpty()) {

            currentLoans.addRow("Inga övriga lån");
        } else {
        
            currentLoans.setTitle("Lån");
    
            double totalLoanAmount = 0;
    
            for (GenericApplicationFieldGroup subgroup : otherLoans) {
                Optional<Double> amount = subgroup.tryGetFieldAsDouble(ApplicationFieldName.AMOUNT);
                Optional<String> lender = subgroup.tryGetField(ApplicationFieldName.LENDER);
                if (amount.isPresent() && lender.isPresent()) {
                    currentLoans.addRow(lender.get(), formatter.formatCurrency(amount.get()));
                    totalLoanAmount += amount.get();
                }
            }
    
            FieldData totalAmount = new FieldData();
            totalAmount.setTitle("Totalt lånebelopp");
            totalAmount.addRow(formatter.formatCurrency(totalLoanAmount));
            currentLoansData.addField(totalAmount);
        }

        return currentLoansData;
    }

    private ConfirmationFormListData getBailmentForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData bailmentData = new ConfirmationFormListData();
        bailmentData.setTitle("Borgen");
        
        Optional<Double> bailmentAmount = group.tryGetFieldAsDouble(ApplicationFieldName.BAILMENT_AMOUNT);
        if (bailmentAmount.isPresent()) {
            
            FieldData field = new FieldData();
            field.setTitle("Har du gått i borgen för någon annans lån?");
            bailmentData.addField(field);
            
            if (bailmentAmount.get() > 0) {
                field.addRow("Ja");
                
                FieldData depField = new FieldData();
                depField.setTitle("Borgensbelopp");
                depField.addRow(formatter.formatCurrency(bailmentAmount.get()));
                bailmentData.addField(depField);
            } else {
                field.addRow("Nej");
            }
        }

        return bailmentData;
    }

    private ConfirmationFormListData getDeferredCapitalGainsTaxForSummary(GenericApplicationFieldGroup group,
            Formatter formatter) {
        
        ConfirmationFormListData taxData = new ConfirmationFormListData();
        taxData.setTitle("Uppskov för reavinstskatt");

        Optional<Double> deferredAmount = group
                .tryGetFieldAsDouble(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT);
        
        if (deferredAmount.isPresent()) {
            FieldData hasReferralCapitalGainTax = new FieldData();
            hasReferralCapitalGainTax.setTitle("Har du begärt uppskov för reavinstskatt?");
            taxData.addField(hasReferralCapitalGainTax);
            
            if (deferredAmount.get() > 0) {
                hasReferralCapitalGainTax.addRow("Ja");
                
                FieldData amount = new FieldData();
                amount.setTitle("Uppskovsbelopp");
                amount.addRow(formatter.formatCurrency(deferredAmount.get()));
                taxData.addField(amount);
            } else {
                hasReferralCapitalGainTax.addRow("Nej");
            }
        }

        return taxData;
    }

    private ConfirmationFormListData getOtherPropertiesForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData otherPropertiesData = new ConfirmationFormListData();
        otherPropertiesData.setTitle("Andra fastigheter");
        
        List<GenericApplicationFieldGroup> otherProperties = ApplicationUtils.getSubGroups(group,
                GenericApplicationFieldGroupNames.PROPERTY);
        
        if (otherProperties.isEmpty()) {
            FieldData type = new FieldData();
            type.addRow("Inga andra fastigheter");
            otherPropertiesData.addField(type);
        } else {
            for (GenericApplicationFieldGroup subgroup : otherProperties) {

                FieldData type = new FieldData();
                type.setTitle("Objektstyp");
                type.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(subgroup
                        .getField(ApplicationFieldName.TYPE))));
                otherPropertiesData.addField(type);
                
                Optional<String> houseLabel = subgroup.tryGetField(ApplicationFieldName.HOUSE_LABEL);
                if (houseLabel.isPresent()) {
                    FieldData field = new FieldData();
                    field.setTitle("Fastighetsbeteckning");
                    field.addRow(houseLabel.get());
                    otherPropertiesData.addField(field);
                }
                
                Optional<String> municipalityCode = subgroup.tryGetField(ApplicationFieldName.MUNICIPALITY);
                if (municipalityCode.isPresent()) {
                    FieldData field = new FieldData();
                    field.setTitle("Kommun");
                    Optional<String> municipalityName = CountyCache.findMunicipalityName(municipalityCode.get());
                    field.addRow(municipalityName.orElse(municipalityCode.get()));
                    otherPropertiesData.addField(field);
                }

                Optional<Double> marketValue = subgroup.tryGetFieldAsDouble(ApplicationFieldName.MARKET_VALUE);
                if (marketValue.isPresent()) {
                    FieldData mv = new FieldData();
                    mv.setTitle("Marknadsvärde");
                    mv.addRow(formatter.formatCurrency(marketValue.get()));
                    otherPropertiesData.addField(mv);
                }

                Optional<Double> assessedValue = subgroup.tryGetFieldAsDouble(ApplicationFieldName.ASSESSED_VALUE);
                if (assessedValue.isPresent()) {
                    FieldData av = new FieldData();
                    av.setTitle("Taxeringsvärde");
                    av.addRow(formatter.formatCurrency(assessedValue.get()));
                    otherPropertiesData.addField(av);
                }

                Optional<Double> loanAmount = subgroup.tryGetFieldAsDouble(ApplicationFieldName.LOAN_AMOUNT);
                if (loanAmount.isPresent()) {
                    FieldData la = new FieldData();
                    la.setTitle("Lånebelopp");
                    la.addRow(formatter.formatCurrency(loanAmount.get()));
                    otherPropertiesData.addField(la);
                }

                Optional<Double> groundRent = subgroup.tryGetFieldAsDouble(ApplicationFieldName.YEARLY_GROUND_RENT);
                if (groundRent.isPresent()) {
                    FieldData gr = new FieldData();
                    gr.setTitle("Tomträttsavgäld");
                    gr.addRow(formatter.formatCurrency(groundRent.get()));
                    otherPropertiesData.addField(gr);
                }

                Optional<Double> monthlyRent = subgroup.tryGetFieldAsDouble(ApplicationFieldName.MONTHLY_COST);
                if (monthlyRent.isPresent()) {
                    FieldData mr = new FieldData();
                    mr.setTitle("Månadsavgift");
                    mr.addRow(formatter.formatCurrency(monthlyRent.get()));
                    otherPropertiesData.addField(mr);
                }
            }
        }

        return otherPropertiesData;
    }

    private ConfirmationFormListData getAlimonyForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData alimonyData = new ConfirmationFormListData();
        alimonyData.setTitle("Underhållsbidrag");

        Optional<Double> amount = group.tryGetFieldAsDouble(ApplicationFieldName.PAYING_ALIMONY_AMOUNT);
        if (amount.isPresent()) {
            FieldData isPayingAlimony = new FieldData();
            isPayingAlimony.setTitle("Har du barn som du betalar underhåll för?");
            alimonyData.addField(isPayingAlimony);
            
            if (amount.get() > 0) {
                isPayingAlimony.addRow("Ja");
                
                FieldData alimonyAmount = new FieldData();
                alimonyAmount.setTitle("Belopp");
                alimonyAmount.addRow(formatter.formatCurrency(amount.get()));
                alimonyData.addField(alimonyAmount);
            } else {
                isPayingAlimony.addRow("Nej");
            }
        }

        return alimonyData;
    }

    private ConfirmationFormListData getCoApplicantForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData coApplicantData = new ConfirmationFormListData();
        coApplicantData.setTitle("Medsökande");

        Optional<String> fullName = group.tryGetField(ApplicationFieldName.NAME);
        if (fullName.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Namn");
            field.addRow(fullName.get());
            coApplicantData.addField(field);
        }

        Optional<String> firstName = group.tryGetField(ApplicationFieldName.FIRST_NAME);
        if (firstName.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Förnamn");
            field.addRow(firstName.get());
            coApplicantData.addField(field);
        }

        Optional<String> lastName = group.tryGetField(ApplicationFieldName.LAST_NAME);
        if (lastName.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Efternamn");
            field.addRow(lastName.get());
            coApplicantData.addField(field);
        }

        Optional<String> personalNumber = group.tryGetField(ApplicationFieldName.PERSONAL_NUMBER);
        if (personalNumber.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Personnummer");
            field.addRow(personalNumber.get());
            coApplicantData.addField(field);
        }

        Optional<String> emailAddress = group.tryGetField(ApplicationFieldName.EMAIL);
        if (emailAddress.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("E-post");
            field.addRow(emailAddress.get());
            coApplicantData.addField(field);
        }

        Optional<String> phoneNumber = group.tryGetField(ApplicationFieldName.PHONE_NUMBER);
        if (phoneNumber.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Telefonnummer");
            field.addRow(phoneNumber.get());
            coApplicantData.addField(field);
        }

        Optional<Double> monthlyIncome = group.tryGetFieldAsDouble(ApplicationFieldName.MONTHLY_INCOME);
        if (monthlyIncome.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Månadsinkomst");
            field.addRow(Catalog.format("{0}/mån", formatter.formatCurrency(monthlyIncome.get())));
            coApplicantData.addField(field);
        }

        Optional<String> relStat = group.tryGetField(ApplicationFieldName.RELATIONSHIP_STATUS);
        if (relStat.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Civilstatus");
            field.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(relStat.get())));
            coApplicantData.addField(field);
        }

        Optional<String> employmentType = group.tryGetField(ApplicationFieldName.EMPLOYMENT_TYPE);
        if (employmentType.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Huvudsaklig sysselsättning");
            field.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(employmentType.get())));
            coApplicantData.addField(field);
        }

        Optional<String> employerOrCompanyName = group.tryGetField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
        if (employerOrCompanyName.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Arbetsgivarens/företagets namn");
            field.addRow(employerOrCompanyName.get());
            coApplicantData.addField(field);
        }

        Optional<String> employedSince = group.tryGetField(ApplicationFieldName.EMPLOYEE_SINCE);
        if (employedSince.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Anställd sedan");
            field.addRow(employedSince.get());
            coApplicantData.addField(field);
        }

        Optional<String> profession = group
                .tryGetField(ApplicationFieldName.PROFESSION);
        if (profession.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Yrkesroll");
            field.addRow(profession.get());
            coApplicantData.addField(field);
        }

        Optional<String> sbabEmployeeSince = group
                .tryGetField(ApplicationFieldName.SBAB_EMPLOYEE_SINCE);
        if (sbabEmployeeSince.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Anställd sedan");
            field.addRow(sbabEmployeeSince.get());
            coApplicantData.addField(field);
        }
        
        Optional<String> streetAddress = group.tryGetField(ApplicationFieldName.STREET_ADDRESS);
        Optional<String> postalCode = group.tryGetField(ApplicationFieldName.POSTAL_CODE);
        Optional<String> town  = group.tryGetField(ApplicationFieldName.TOWN);
        
        if (streetAddress.isPresent() && postalCode.isPresent() && town.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Adress");
            field.addRow(streetAddress.get());
            field.addRow(Catalog.format("{0} {1}", postalCode.get(), town.get()));
            coApplicantData.addField(field);
        }

        Optional<Double> alimonyAmount = group.tryGetFieldAsDouble(ApplicationFieldName.PAYING_ALIMONY_AMOUNT);
        if (alimonyAmount.isPresent()) {
            
            FieldData isPayingAlimony = new FieldData();
            isPayingAlimony.setTitle("Har din medsöknande barn som han eller hon betalar underhåll för?");
            coApplicantData.addField(isPayingAlimony);
            
            if (alimonyAmount.get() > 0) {
                isPayingAlimony.addRow("Ja");
                
                FieldData field = new FieldData();
                field.setTitle("Belopp underhållsbidrag");
                field.addRow(formatter.formatCurrency(alimonyAmount.get()));
                coApplicantData.addField(field);
            } else {
                isPayingAlimony.addRow("Nej");
            }
        }

        Optional<Double> loanAmount = group.tryGetFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_AMOUNT);
        Optional<Double> loanMonthlyCost = group.tryGetFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST);
        
        if (loanAmount.isPresent() || loanMonthlyCost.isPresent()) {
            
            FieldData hasCsnLoan = new FieldData();
            hasCsnLoan.setTitle("Har din medsökande CSN-lån?");
            coApplicantData.addField(hasCsnLoan);

            if (loanAmount.orElse(0d) > 0 || loanMonthlyCost.orElse(0d) > 0) {
                
                hasCsnLoan.addRow("Ja");

                if (loanAmount.isPresent() && loanAmount.get() > 0) {
                    FieldData field = new FieldData();
                    field.setTitle("Lånebelopp");
                    field.addRow(formatter.formatCurrency(loanAmount.get()));
                    coApplicantData.addField(field);
                }

                if (loanMonthlyCost.isPresent() && loanMonthlyCost.get() > 0) {
                    FieldData field = new FieldData();
                    field.setTitle("Månadskostnad för studielån");
                    field.addRow(formatter.formatCurrency(loanMonthlyCost.get()));
                    coApplicantData.addField(field);
                }
            } else {
                hasCsnLoan.addRow("Nej");
            }
        }
        
        return coApplicantData;
    }

    private ConfirmationFormListData getHouseholdForSummary(GenericApplicationFieldGroup group, Formatter formatter) {
        ConfirmationFormListData householdData = new ConfirmationFormListData();
        householdData.setTitle("Om hushållet");

        Optional<Integer> numberOfChildren = group.tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN);
        Optional<Integer> numberOfChildrenReceivingBenefitsFrom = group
                .tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_CHILD_BENEFIT);
        Optional<Integer> numberOfChildrenReceivingAlimonyFor = group
                .tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY);
        Optional<Integer> numberOfChildrenPayingAlimonyFor = group
                .tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_PAYING_ALIMONY);

        if (numberOfChildren.isPresent() || numberOfChildrenReceivingBenefitsFrom.isPresent()
                || numberOfChildrenReceivingAlimonyFor.isPresent() || numberOfChildrenPayingAlimonyFor.isPresent()) {

            FieldData hasChildren = new FieldData();
            hasChildren.setTitle("Finns det barn i hushållet?");
            householdData.addField(hasChildren);

            if (numberOfChildren.orElse(0) > 0 || numberOfChildrenReceivingBenefitsFrom.orElse(0) > 0
                    || numberOfChildrenReceivingAlimonyFor.orElse(0) > 0
                    || numberOfChildrenPayingAlimonyFor.orElse(0) > 0) {

                hasChildren.addRow("Ja");
                
                if (numberOfChildren.isPresent() && numberOfChildren.get() > 0) {
                    FieldData field = new FieldData();
                    field.setTitle("Antal barn");
                    field.addRow(String.valueOf(numberOfChildren.orElse(0)));
                    householdData.addField(field);
                }
    
                if (numberOfChildrenReceivingBenefitsFrom.isPresent()) {
                    FieldData field = new FieldData();
                    field.setTitle("Antal barn ni får barnbidrag för");
                    field.addRow(String.valueOf(numberOfChildrenReceivingBenefitsFrom.orElse(0)));
                    householdData.addField(field);
                }
                
                if (numberOfChildrenReceivingAlimonyFor.isPresent()) {
                    FieldData field = new FieldData();
                    field.setTitle("Antal barn ni får underhållsbidrag för");
                    field.addRow(String.valueOf(numberOfChildrenReceivingAlimonyFor.orElse(0)));
                    householdData.addField(field);
                }
                
                if (numberOfChildrenPayingAlimonyFor.isPresent()) {
                    FieldData field = new FieldData();
                    field.setTitle("Antal barn ni betalar underhållsbidrag för");
                    field.addRow(String.valueOf(numberOfChildrenPayingAlimonyFor.orElse(0)));
                    householdData.addField(field);
                }
            } else {
                hasChildren.addRow("Nej");
            }
        }
        
        Optional<Integer> adultsInHousehold = group.tryGetFieldAsInteger(ApplicationFieldName.NUMBER_OF_ADULTS);
        if (adultsInHousehold.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Finns det en till vuxen i hushållet?");
            householdData.addField(field);

            if (adultsInHousehold.get() > 1) {
                field.addRow("Ja");

                FieldData depField = new FieldData();
                depField.setTitle("Antalet vuxna i hushållet");
                depField.addRow(String.valueOf(adultsInHousehold.get()));
                householdData.addField(depField);
            } else {
                field.addRow("Nej");
            }
        }

        Optional<Double> loanAmount = group.tryGetFieldAsDouble(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOAN_AMOUNT);
        if (loanAmount.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Har din medsökande andra lån?");
            householdData.addField(field);
            
            if (loanAmount.get() > 0) {
                field.addRow("Ja");
                
                FieldData depField = new FieldData();
                depField.setTitle("Total låneskuld");
                depField.addRow(formatter.formatCurrency(loanAmount.get()));
                householdData.addField(depField);
            } else {
                field.addRow("Nej");
            }
        }

        Optional<Double> bailmentAmount = group.tryGetFieldAsDouble(ApplicationFieldName.BAILMENT_AMOUNT);
        if (bailmentAmount.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Har din medsökande gått i borgen för någons lån?");
            householdData.addField(field);
            
            if (bailmentAmount.get() > 0) {
                field.addRow("Ja");
                
                FieldData depField = new FieldData();
                depField.setTitle("Totalt borgensbelopp");
                depField.addRow(formatter.formatCurrency(bailmentAmount.orElse(0d)));
                householdData.addField(depField);
            } else {
                field.addRow("Nej");
            }
        }

        Optional<Double> totalTaxAmount = group.tryGetFieldAsDouble(ApplicationFieldName.DEFERRED_AMOUNT);
        if (totalTaxAmount.isPresent()) {
            FieldData field = new FieldData();
            field.setTitle("Har din medsökande begärt uppskov för reavinstskatt?");
            householdData.addField(field);
            
            if (totalTaxAmount.get() > 0) {
                field.addRow("Ja");
    
                FieldData totalCapitalGainTaxAmount = new FieldData();
                totalCapitalGainTaxAmount.setTitle("Totalt uppskovsbelopp");
                totalCapitalGainTaxAmount.addRow(formatter.formatCurrency(totalTaxAmount.get()));
                householdData.addField(totalCapitalGainTaxAmount);
            } else {
                field.addRow("Nej");
            }
        }

        return householdData;
    }

    private ConfirmationFormListData getKycForSummary(GenericApplicationFieldGroup group, Optional<GenericApplicationFieldGroup> bankServicesGroup, Formatter formatter) {
        ConfirmationFormListData kycData = new ConfirmationFormListData();
        kycData.setTitle("Kundinformation");

        Optional<String> fullName = group.tryGetField(ApplicationFieldName.NAME);
        if (fullName.isPresent()) {
            FieldData name = new FieldData();
            name.setTitle("Namn");
            name.addRow(fullName.get());
            kycData.addField(name);
        }

        Optional<String> firstName = group.tryGetField(ApplicationFieldName.FIRST_NAME);
        if (firstName.isPresent()) {
            FieldData fn = new FieldData();
            fn.setTitle("Förnamn");
            fn.addRow(firstName.get());
            kycData.addField(fn);
        }

        Optional<String> lastName = group.tryGetField(ApplicationFieldName.LAST_NAME);
        if (lastName.isPresent()) {
            FieldData ln = new FieldData();
            ln.setTitle("Efternamn");
            ln.addRow(lastName.get());
            kycData.addField(ln);
        }

        FieldData personalNumber = new FieldData();
        personalNumber.setTitle("Personnummer");
        personalNumber.addRow(group.getField(ApplicationFieldName.PERSONAL_NUMBER));
        kycData.addField(personalNumber);

        String postalAddressCountryCode = group.getField(ApplicationFieldName.COUNTRY);
        Locale country = new Locale("en_US", postalAddressCountryCode);

        FieldData postalAddress = new FieldData();
        postalAddress.setTitle("Postadress");
        postalAddress.addRow(group.getField(ApplicationFieldName.STREET_ADDRESS));
        postalAddress.addRow(Catalog.format("{0} {1}", group.getField(ApplicationFieldName.POSTAL_CODE),
                group.getField(ApplicationFieldName.TOWN)));
        postalAddress.addRow(country.getDisplayCountry());
        kycData.addField(postalAddress);

        FieldData mobileNumber = new FieldData();
        mobileNumber.setTitle("Mobiltelefonnummer");
        mobileNumber.addRow(group.getField(ApplicationFieldName.PHONE_NUMBER));
        kycData.addField(mobileNumber);

        FieldData email = new FieldData();
        email.setTitle("E-postadress");
        email.addRow(group.getField(ApplicationFieldName.EMAIL));
        kycData.addField(email);

        FieldData residencesForTaxPurposes = new FieldData();
        residencesForTaxPurposes.setTitle("Skatterättsliga hemvister");
        for (GenericApplicationFieldGroup subgroup : ApplicationUtils.getSubGroups(group,
                GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES)) {

            Optional<String> countryCode = subgroup.tryGetField(ApplicationFieldName.COUNTRY);
            if (!countryCode.isPresent()) {
                continue;
            }

            Locale countryLocale = new Locale("en_US", countryCode.get());
            String countryName = countryLocale.getDisplayCountry();

            if ("SE".equalsIgnoreCase(countryCode.get())) {
                residencesForTaxPurposes.addRow(countryName);
            } else {
                Optional<String> tin = subgroup.tryGetField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER);

                if (tin.isPresent() && !Strings.isNullOrEmpty(tin.get())) {
                    residencesForTaxPurposes.addRow(Catalog.format("{0} ({1})",countryName, tin.get()));
                } else {
                    residencesForTaxPurposes.addRow(Catalog.format("{0} ({1})",countryName, "TIN saknas"));
                }
            }
        }
        kycData.addField(residencesForTaxPurposes);

        FieldData employment = new FieldData();
        employment.setTitle("Huvudsaklig sysselsättning");
        employment.addRow(formatter.getString(ApplicationFieldStrings.OPTION_LABELS.get(group
                .getField(ApplicationFieldName.EMPLOYMENT_TYPE))));
        kycData.addField(employment);

        Optional<String> employerOrCompanyName = group.tryGetField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
        if (employerOrCompanyName.isPresent()) {
            FieldData employerOrCompanyNameData = new FieldData();
            employerOrCompanyNameData.setTitle("Arbetsgivarens/företagets namn");
            employerOrCompanyNameData.addRow(employerOrCompanyName.get());
            kycData.addField(employerOrCompanyNameData);
        }

        Optional<String> employerSince = group.tryGetField(ApplicationFieldName.EMPLOYEE_SINCE);
        if (employerSince.isPresent()) {
            FieldData es = new FieldData();
            es.setTitle("Anställd sedan");
            es.addRow(employerSince.get());
            kycData.addField(es);
        }

        Optional<String> profession = group.tryGetField(ApplicationFieldName.PROFESSION);
        if (profession.isPresent()) {
            FieldData professionField = new FieldData();
            professionField.setTitle("Yrkesroll");
            professionField.addRow(profession.get());
            kycData.addField(professionField);
        }

        Optional<String> sbabEmployeeSince = group.tryGetField(ApplicationFieldName.SBAB_EMPLOYEE_SINCE);
        if (sbabEmployeeSince.isPresent()) {
            FieldData sbabEmployeeSinceField = new FieldData();
            sbabEmployeeSinceField.setTitle("Anställd sedan");
            sbabEmployeeSinceField.addRow(sbabEmployeeSince.get());
            kycData.addField(sbabEmployeeSinceField);
        }

        Optional<Boolean> isPep = group.tryGetFieldAsBool(ApplicationFieldName.IS_PEP);

        FieldData pep = new FieldData();
        pep.setTitle("Är du en person i politiskt utsatt ställning?");
        pep.addRow((isPep.isPresent() && isPep.get()) ? "Ja" : "Nej");
        kycData.addField(pep);

        Optional<Boolean> isActingOnOwnBehalf = group.tryGetFieldAsBool(ApplicationFieldName.ON_OWN_BEHALF);

        FieldData onOwnBehalf = new FieldData();
        onOwnBehalf.setTitle("Har du fyllt i ansökan för egen räkning?");
        onOwnBehalf.addRow((isActingOnOwnBehalf.isPresent() && isActingOnOwnBehalf.get()) ? "Ja" : "Nej");
        kycData.addField(onOwnBehalf);

        FieldData purposeOfBusiness = new FieldData();
        purposeOfBusiness.setTitle("Syftet med affärsförbindelsen");
        purposeOfBusiness.addRow("Vardagsekonomi och finansiering");
        kycData.addField(purposeOfBusiness);

        FieldData otherBankServices = new FieldData();
        otherBankServices.setTitle("Intresse för övriga tjänster");
        kycData.addField(otherBankServices);

        FieldData assetsBeingTransfered = new FieldData();
        assetsBeingTransfered.setTitle("Härkomst av tillgångar som överförs till banken");
        kycData.addField(assetsBeingTransfered);

        if (bankServicesGroup.isPresent()) {
            Optional<GenericApplicationFieldGroup> obs = ApplicationUtils
                    .getFirstSubGroup(bankServicesGroup, GenericApplicationFieldGroupNames.OTHER_BANK_SERVICES);
            if (obs.isPresent()) {
                Optional<Map<String, String>> fields = Optional.ofNullable(obs.get().getFields());
                if (fields.isPresent()) {
                    for (Map.Entry<String, String> entry : fields.get().entrySet()) {
                        if (Objects.equals(entry.getValue(), ApplicationFieldOptionValues.YES)) {
                            switch (entry.getKey()) {
                            case ApplicationFieldOptionValues.SAVINGS:
                                otherBankServices.addRow("Besparingar");
                                break;
                            case ApplicationFieldOptionValues.CAPITAL_MANAGEMENT:
                                otherBankServices.addRow("Placering");
                                break;
                            case ApplicationFieldOptionValues.CARDS:
                                otherBankServices.addRow("Kort");
                                break;
                            case ApplicationFieldOptionValues.PENSION:
                                otherBankServices.addRow("Pension");
                                break;
                            case ApplicationFieldOptionValues.PENSION_INSURANCE:
                                otherBankServices.addRow("Pensionsförsäkring");
                                break;
                            case ApplicationFieldOptionValues.INVESTING:
                                otherBankServices.addRow("Investering");
                                break;
                            }
                        }
                    }

                }
            }

            Optional<GenericApplicationFieldGroup> ts = ApplicationUtils
                    .getFirstSubGroup(bankServicesGroup, GenericApplicationFieldGroupNames.TRANSFER_SAVINGS);
            if (ts.isPresent()) {
                Optional<Map<String, String>> fields = Optional.ofNullable(ts.get().getFields());
                if (fields.isPresent()) {
                    for (Map.Entry<String, String> entry : fields.get().entrySet()) {
                        if (Objects.equals(entry.getValue(), ApplicationFieldOptionValues.YES)) {
                            switch (entry.getKey()) {
                            case ApplicationFieldOptionValues.SALARY:
                                assetsBeingTransfered.addRow("Lön");
                                break;
                            case ApplicationFieldOptionValues.PENSION:
                                assetsBeingTransfered.addRow("Pension");
                                break;
                            case ApplicationFieldOptionValues.INSURANCE_PAYOUT:
                                assetsBeingTransfered.addRow("Försäkringsutbetalning");
                                break;
                            case ApplicationFieldOptionValues.GIFT_OR_INHERITANCE:
                                assetsBeingTransfered.addRow("Gåva eller arv");
                                break;
                            case ApplicationFieldOptionValues.PROPERTY_SALE:
                                assetsBeingTransfered.addRow("Fastighetsförsäljning");
                                break;
                            case ApplicationFieldOptionValues.DIVIDENDS_FROM_SMALL_BUSINESS:
                                assetsBeingTransfered.addRow("Utdelning från fåmansbolag");
                                break;
                            case ApplicationFieldOptionValues.SALE_OF_COMPANY:
                                assetsBeingTransfered.addRow("Försäljning av företag");
                                break;
                            case ApplicationFieldOptionValues.OTHER:
                                assetsBeingTransfered.addRow("Annat");
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (otherBankServices.getValues().isEmpty()) {
            otherBankServices.addRow("Inget intresse");
        }

        if (assetsBeingTransfered.getValues().isEmpty()) {
            assetsBeingTransfered.addRow("Inga tillgångar kommer att överföras");
        }

        return kycData;
    }
    
    class Formatter {
        private final Catalog catalog;
        private final Currency currency;
        private final Locale locale;
        private final NumberFormat percentFormatter;
        
        public Formatter(User user) {
            this.catalog = Catalog.getCatalog(user.getProfile().getLocale());
            this.currency = currencyRepository.findOne("SEK");
            this.locale = Catalog.getLocale(user.getProfile().getLocale());
            this.percentFormatter = getPercentFormatter(locale);
        }
        
        public String formatPercent(Double decimalForm) {
            if (decimalForm == null) {
                return null;
            }
            
            return percentFormatter.format(decimalForm);
        }
        
        public String formatCurrency(Double amount) {
            if (amount == null) {
                return null;
            }
            
            return I18NUtils.formatCurrencyRound(amount, currency, locale);
        }
        
        public String getString(LocalizableKey localizableKey) {
            return catalog.getString(localizableKey);
        }
        
        private NumberFormat getPercentFormatter(Locale locale) {
            NumberFormat percentFormatter = NumberFormat.getPercentInstance(locale);
            percentFormatter.setMinimumIntegerDigits(1);
            percentFormatter.setMinimumFractionDigits(1);
            percentFormatter.setMaximumFractionDigits(2);
            return percentFormatter;
        }
    }
}
