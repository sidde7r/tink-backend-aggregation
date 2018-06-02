package se.tink.backend.grpc.v1.converter.identity;

import java.util.stream.Collectors;
import se.tink.libraries.identity.model.Address;
import se.tink.libraries.identity.model.CompanyEngagement;
import se.tink.libraries.identity.model.CreditScore;
import se.tink.libraries.identity.model.Identity;
import se.tink.libraries.identity.model.OutstandingDebt;
import se.tink.libraries.identity.model.Property;
import se.tink.libraries.identity.model.TaxDeclaration;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.IdentityState;
import se.tink.grpc.v1.models.IdentityStateAddress;
import se.tink.grpc.v1.models.IdentityStateCompany;
import se.tink.grpc.v1.models.IdentityStateCompanyEngagement;
import se.tink.grpc.v1.models.IdentityStateCreditScore;
import se.tink.grpc.v1.models.IdentityStateOutstandingDebt;
import se.tink.grpc.v1.models.IdentityStateProperty;
import se.tink.grpc.v1.models.IdentityStateRecordOfNonPayment;
import se.tink.grpc.v1.models.IdentityStateRole;
import se.tink.grpc.v1.models.IdentityStateTaxDeclaration;

public class IdentityToGrpcConverter implements Converter<Identity, IdentityState> {
    private final String currencyCode;

    public IdentityToGrpcConverter(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public IdentityState convertFrom(Identity input){

        IdentityState.Builder builder = IdentityState.newBuilder();

        ConverterUtils.setIfPresent(input::getFirstName, builder::setFirstName);
        ConverterUtils.setIfPresent(input::getLastName, builder::setLastName);
        ConverterUtils.setIfPresent(input::getNationalId, builder::setNationalId);

        if (input.getAddress() != null) {
            Address address = input.getAddress();
            IdentityStateAddress.Builder isaBuilder = IdentityStateAddress.newBuilder();
            ConverterUtils.setIfPresent(address::getName, isaBuilder::setName);
            ConverterUtils.setIfPresent(address::getCity, isaBuilder::setCity);
            ConverterUtils.setIfPresent(address::getCommunity, isaBuilder::setCommunity);
            ConverterUtils.setIfPresent(address::getPostalCode, isaBuilder::setPostalCode);

            builder.setAddress(isaBuilder);
        }


        if (input.getProperties() != null && input.getProperties().size() > 0) {
            for (Property p: input.getProperties()) {
                IdentityStateProperty.Builder ispBuilder = IdentityStateProperty.newBuilder();
                ConverterUtils.setIfPresent(p::getName, ispBuilder::setName);
                ConverterUtils.setIfPresent(p::getMunicipality, ispBuilder::setMunicipality);
                ConverterUtils.setIfPresent(p::getNumber, ispBuilder::setNumber);
                ConverterUtils.setIfPresent(p::getAcquisitionDate, ispBuilder::setAcquisitionDate, ProtobufModelUtils::toProtobufTimestamp);
                builder.addProperties(ispBuilder);
            }
        }

        if (input.getCompanies() != null && input.getCompanies().size() > 0) {
            for (CompanyEngagement companyEngagement : input.getCompanies()) {
                IdentityStateCompanyEngagement.Builder isceBuilder = IdentityStateCompanyEngagement.newBuilder();
                ConverterUtils.setIfPresent(companyEngagement::getCompany, isceBuilder::setCompany,
                        company -> IdentityStateCompany
                                .newBuilder()
                                .setName(companyEngagement.getCompany().getName())
                                .setNumber(companyEngagement.getCompany().getCompanyNumber())
                                .build());
                ConverterUtils.setIfPresent(companyEngagement::getRoles, isceBuilder::addAllRoles,
                        roles -> roles.stream()
                                .map(role -> IdentityStateRole.newBuilder().setName(role.getName()).build())
                                .collect(Collectors.toList()));
                ConverterUtils.setIfPresent(companyEngagement::getInDate, isceBuilder::setDateIn,
                        ProtobufModelUtils::toProtobufTimestamp);
                builder.addCompanyEngagements(isceBuilder.build());
            }
        }

        if (input.getCreditScore() != null) {
            CreditScore cs = input.getCreditScore();
            IdentityStateCreditScore.Builder iscsBuilder = IdentityStateCreditScore.newBuilder();
            ConverterUtils.setIfPresent(cs::getText, iscsBuilder::setText);
            ConverterUtils.setIfPresent(cs::getScore, iscsBuilder::setScore);
            ConverterUtils.setIfPresent(cs::getMaxScore, iscsBuilder::setMaxScore);
            builder.setCreditScore(iscsBuilder);
        }

        if (input.getMostRecentTaxDeclaration() != null) {
            TaxDeclaration td = input.getMostRecentTaxDeclaration();
            IdentityStateTaxDeclaration.Builder istdBuilder = IdentityStateTaxDeclaration.newBuilder();
            istdBuilder.setFinalTax(NumberUtils.toCurrencyDenominatedAmount(td.getFinalTax(), currencyCode));
            istdBuilder.setIncomeByService(NumberUtils.toCurrencyDenominatedAmount(td.getIncomeByService(), currencyCode));
            istdBuilder.setIncomeByCapital(NumberUtils.toCurrencyDenominatedAmount(td.getIncomeByCapital(), currencyCode));
            istdBuilder.setTotalIncome(NumberUtils.toCurrencyDenominatedAmount(td.getTotalIncome(), currencyCode));
            istdBuilder.setYear(td.getYear());
            ConverterUtils.setIfPresent(td::getRegisteredDate, istdBuilder::setRegisteredDate,
                    ProtobufModelUtils::toProtobufTimestamp);
            builder.setMostRecentTaxDeclaration(istdBuilder);
        }

        ConverterUtils.setIfPresent(input::getRecordsOfNonPayment, builder::addAllRecordsOfNonPayment,
                recordsOfNonPayment -> recordsOfNonPayment.stream().map(x -> IdentityStateRecordOfNonPayment.newBuilder()
                        .setName(x.getName())
                        .setAmount(NumberUtils.toCurrencyDenominatedAmount(x.getAmount(),currencyCode))
                        .setRegisteredDate(ProtobufModelUtils.toProtobufTimestamp(x.getRegisteredDate()))
                        .build()).collect(Collectors.toList()));

        if (input.getOutstandingDebt() != null) {
            OutstandingDebt od = input.getOutstandingDebt();
            IdentityStateOutstandingDebt.Builder isodBuilder = IdentityStateOutstandingDebt.newBuilder();
            ConverterUtils.setIfPresent(od::getAmount, isodBuilder::setAmount, amount -> NumberUtils.toCurrencyDenominatedAmount(amount, currencyCode));
            ConverterUtils.setIfPresent(od::getNumber, isodBuilder::setNumber);
            ConverterUtils.setIfPresent(od::getRegisteredDate, isodBuilder::setRegisteredDate,
                    ProtobufModelUtils::toProtobufTimestamp);
            builder.setOutstandingDebt(isodBuilder);
        }
        return builder.build();
    }
}
