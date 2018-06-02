package se.tink.backend.system.document.mapper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.joda.time.DateTime;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.system.document.core.AdditionalServiceInterest;
import se.tink.backend.system.document.core.Applicant;
import se.tink.backend.system.document.core.Asset;
import se.tink.backend.system.document.core.CoApplicant;
import se.tink.backend.system.document.core.Employment;
import se.tink.backend.system.document.core.Mortgage;
import se.tink.backend.system.document.core.PoaDetails;
import se.tink.backend.system.document.core.Residence;
import se.tink.backend.system.document.core.ResidenceType;
import se.tink.backend.system.document.core.Salary;
import se.tink.backend.system.document.core.Security;
import se.tink.backend.system.document.core.SwitchMortgageProvider;
import se.tink.backend.system.document.core.TaxReport;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.LogUtils;

public class GenericApplicationToDocumentUserMapper {

    private static final LogUtils log = new LogUtils(GenericApplicationToDocumentUserMapper.class);
    private static final int signatureScaleHeight = 150;
    private static final int signatureScaleFactor = 5;

    private static final ImmutableMap<String, String> otherBankingTranslations = new ImmutableMap.Builder<String, String>()
            .put("cards", "Kort")
            .put("pension-insurance", "Pensionsförsäkring")
            .put("pension", "Pension")
            .put("savings", "Sparande")
            .put("investing", "Investering")
            .put("capital-management", "Placering")
            .build();

    private static final ImmutableMap<String, String> transferSavingsTranslations = new ImmutableMap.Builder<String, String>()
            .put("gift-or-inheritance", "Arv/gåva")
            .put("property-sale", "Pensionsförsäkring")
            .put("pension", "Pension")
            .put("salary", "Lön")
            .put("insurance-payout", "Försäkringsutbetalning")
            .put("dividends-from-small-business", "Utdelning i fåmansbolag")
            .put("sale-of-company", "Företagsförsäljning")
            .put("other", "Annat")
            .build();

    /**
     * Map a generic application to a document (module) aware representation of the model.
     */
    public static SwitchMortgageProvider translate(GenericApplication genericApplication, Date signingDate) {

        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                genericApplication.getFieldGroups(), GenericApplicationFieldGroup::getName);
        Optional<GenericApplicationFieldGroup> genericApplicant = ApplicationUtils.getApplicant(fieldGroupByName);
        Optional<GenericApplicationFieldGroup> mortgageSecurity = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);
        Optional<GenericApplicationFieldGroup> currentMortgage = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);
        Optional<GenericApplicationFieldGroup> product = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.PRODUCT);
        Optional<GenericApplicationFieldGroup> genericCoApplicant = ApplicationUtils.getCoApplicant(fieldGroupByName);

        Optional<Boolean> hasAmortizationRequirement = currentMortgage.get()
                .tryGetFieldAsBool(ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT);

        Optional<String> directDebitAccountNumber = currentMortgage.get()
                .tryGetField(ApplicationFieldName.DIRECT_DEBIT_ACCOUNT);

        Optional<Employment> employment = mapGenericApplicantToEmployment(genericApplicant);
        Optional<TaxReport> latestTaxReport = mapGenericApplicantToTaxReport(genericApplicant);
        Optional<Security> security = mapGenericMortgageSecurityToSecurity(mortgageSecurity);
        Optional<CoApplicant> coApplicant = mapGenericCoApplicantToCoApplicant(genericCoApplicant);

        List<AdditionalServiceInterest> additionalServiceInterests = mapGenericApplicantToAdditionalServiceInterest(
                fieldGroupByName);

        DateTime signingDateTime = new DateTime(signingDate);

        PoaDetails applicantPoaDetails = new PoaDetails(
                genericApplicant.get().getField(ApplicationFieldName.TOWN),
                signingDateTime.toString("yyyy-MM-dd"),
                signingDateTime.plusDays(30).toString("yyyy-MM-dd"),
                getByteArrayImageFromPoints(
                        genericApplicant.get().getFieldAsListOfListOfPoints(ApplicationFieldName.SIGNATURE))
        );
        List<Salary> salaries = mapApplicationTransactionGroupToSalary(ApplicationUtils
                .getFirstSubGroup(genericApplicant, GenericApplicationFieldGroupNames.SALARY_TRANSACTIONS));

        Applicant applicant = new Applicant(
                genericApplicant.get().getField(ApplicationFieldName.NAME),
                genericApplicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER),
                genericApplicant.get().getField(ApplicationFieldName.STREET_ADDRESS),
                genericApplicant.get().getField(ApplicationFieldName.POSTAL_CODE),
                genericApplicant.get().getField(ApplicationFieldName.TOWN),
                genericApplicant.get().getField(ApplicationFieldName.EMAIL),
                genericApplicant.get().getField(ApplicationFieldName.PHONE_NUMBER),
                genericApplicant.get().getFieldAsInteger(ApplicationFieldName.MONTHLY_INCOME),
                employment,
                applicantPoaDetails,
                salaries,
                latestTaxReport,
                mapGenericApplicantToAsset(genericApplicant),
                cleanPoints(genericApplicant.get().getFieldAsListOfListOfPoints(ApplicationFieldName.SIGNATURE), signatureScaleHeight, signatureScaleFactor)
        );

        Residence residence = new Residence(
                currentMortgage.get().getField(ApplicationFieldName.LENDER),
                mortgageSecurity.get().tryGetField(ApplicationFieldName.HOUSING_COMMUNITY_NAME),
                mapPropertyTypeToResidenceType(mortgageSecurity.get().getField(ApplicationFieldName.PROPERTY_TYPE))
        );

        List<String> loanNumbers = Lists.newArrayList();
        currentMortgage.ifPresent(genericApplicationFieldGroup -> {
            for (GenericApplicationFieldGroup group : ApplicationUtils.getSubGroups(genericApplicationFieldGroup, GenericApplicationFieldGroupNames.LOAN)) {
                group.tryGetField(ApplicationFieldName.ACCOUNT_NUMBER).ifPresent(loanNumbers::add);
            }
        });

        SwitchMortgageProvider switchMortgageProvider = new SwitchMortgageProvider(
                product.get().getField(ApplicationFieldName.PROVIDER),
                product.get().getField(ApplicationFieldName.EXTERNAL_ID),
                applicant,
                coApplicant,
                residence,
                new Mortgage(security, hasAmortizationRequirement.orElse(true), directDebitAccountNumber, loanNumbers),
                additionalServiceInterests
        );

        return switchMortgageProvider;
    }

    private static Set<Asset> mapGenericApplicantToAsset(
            Optional<GenericApplicationFieldGroup> genericApplicant) {
        Set<Asset> assets = Sets.newHashSet();
        List<GenericApplicationFieldGroup> subGroups = ApplicationUtils
                .getSubGroups(genericApplicant, GenericApplicationFieldGroupNames.ASSET);

        if (subGroups == null || subGroups.isEmpty()) {
            return assets;
        }

        subGroups.forEach(asset ->
                assets.add(new Asset(asset.getField(ApplicationFieldName.NAME),
                        asset.getFieldAsInteger(ApplicationFieldName.VALUE))));
        return assets;
    }

    private static List<AdditionalServiceInterest> mapGenericApplicantToAdditionalServiceInterest(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        List<AdditionalServiceInterest> additionalServiceInterests = Lists.newArrayList();
        Optional<GenericApplicationFieldGroup> bankServices = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.BANK_SERVICES);

        Optional<GenericApplicationFieldGroup> otherBankServices = ApplicationUtils
                .getFirstSubGroup(bankServices, GenericApplicationFieldGroupNames.OTHER_BANK_SERVICES);
        if (otherBankServices.isPresent()) {
            Map<String, String> fields = otherBankServices.get().getFields();
            if (fields != null) {
                AdditionalServiceInterest additionalServiceInterest = new AdditionalServiceInterest(
                        "Kunden är intresserad av övriga tjänster",
                        translateKeysLanguage(fields.keySet(), otherBankingTranslations)
                );
                additionalServiceInterests.add(additionalServiceInterest);
            }
        }

        Optional<GenericApplicationFieldGroup> transferSavings = ApplicationUtils
                .getFirstSubGroup(bankServices, GenericApplicationFieldGroupNames.TRANSFER_SAVINGS);
        if (transferSavings.isPresent()) {
            Map<String, String> fields = transferSavings.get().getFields();
            if (fields != null) {
                AdditionalServiceInterest additionalServiceInterest = new AdditionalServiceInterest(
                        "Kunden har tillgångar att överföra till SEB, som härstammar från",
                        translateKeysLanguage(fields.keySet(), transferSavingsTranslations)
                );
                additionalServiceInterests.add(additionalServiceInterest);
            }

        }
        return additionalServiceInterests;
    }

    private static Set<String> translateKeysLanguage(Set<String> keys, Map<String, String> translationMap) {
        Set<String> translatedKeys = Sets.newHashSet();
        keys.forEach(k -> {
            if (translationMap.containsKey(k)) {
                translatedKeys.add(translationMap.get(k));
            } else {
                translatedKeys.add(k);
            }
        });
        return translatedKeys;
    }

    private static Optional<Security> mapGenericMortgageSecurityToSecurity(
            Optional<GenericApplicationFieldGroup> mortgageSecurity) {
        if (!mortgageSecurity.isPresent()) {
            return Optional.empty();
        }

        Optional<String> mortgageSecurityAddress = mortgageSecurity.get()
                .tryGetField(ApplicationFieldName.STREET_ADDRESS);
        Optional<String> mortgageSecurityTown = mortgageSecurity.get()
                .tryGetField(ApplicationFieldName.TOWN);

        return Optional.of(new Security(mortgageSecurityAddress.orElse(null), mortgageSecurityTown.orElse(null)));
    }

    private static Optional<CoApplicant> mapGenericCoApplicantToCoApplicant(
            Optional<GenericApplicationFieldGroup> genericCoApplicant) {
        if (!genericCoApplicant.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new CoApplicant(
                genericCoApplicant.get().getField(ApplicationFieldName.NAME),
                genericCoApplicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER)
        ));
    }

    private static Optional<TaxReport> mapGenericApplicantToTaxReport(
            Optional<GenericApplicationFieldGroup> genericApplicant) {
        Optional<Double> taxReportAmount = genericApplicant.get()
                .tryGetFieldAsDouble(ApplicationFieldName.TAX_REPORT_YEARLY_SALARY);

        if (!taxReportAmount.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new TaxReport(taxReportAmount.get()));
    }

    private static Optional<Employment> mapGenericApplicantToEmployment(
            Optional<GenericApplicationFieldGroup> genericApplicant) {
        Optional<String> employerName = genericApplicant.get()
                .tryGetField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
        Optional<String> employeeSince = genericApplicant.get().tryGetField(ApplicationFieldName.EMPLOYEE_SINCE);

        if (!employerName.isPresent()) {
            return Optional.empty();

        }

        return Optional.of(new Employment(employerName.get(), employeeSince));
    }

    private static List<Salary> mapApplicationTransactionGroupToSalary(
            Optional<GenericApplicationFieldGroup> transactionGroup) {
        if (!transactionGroup.isPresent()) {
            return Collections.emptyList();
        }

        return transactionGroup.get().getSubGroups().stream()
                .map(t -> new Salary(
                        t.getField(ApplicationFieldName.DATE),
                        String.valueOf((int) Double.parseDouble(t.getField(ApplicationFieldName.AMOUNT))),
                        t.getField(ApplicationFieldName.DESCRIPTION))).collect(
                        Collectors.toList());
    }

    private static ResidenceType mapPropertyTypeToResidenceType(String type) {
        switch (type) {
        case (ApplicationFieldOptionValues.HOUSE):
            return ResidenceType.HOUSE;
        case (ApplicationFieldOptionValues.APARTMENT):
            return ResidenceType.APARTMENT;
        default:
            return null;
        }
    }

    /**
     * Accepts a list of lists of points.
     * Each list represents a line.
     * Transforms the points in order to fit the box, connects the dots, and returns an in-memory png image.
     */
    public static byte[] getByteArrayImageFromPoints(List<List<Point>> list) {

        int margin = signatureScaleFactor;
        list = cleanPoints(list, signatureScaleHeight, signatureScaleFactor);
        if (list == null) {
            log.error("Signature is missing in PDF document generation process.");
            return null;
        }

        int maxWidth = 0;
        for (List<Point> line : list) {
            for (Point item : line) {
                if (item.x > maxWidth) {
                    maxWidth = item.x;
                }
            }
        }

        int width = ceilBasedOnMultiplier(maxWidth + 2 * margin, margin);
        int height = signatureScaleHeight;

        BufferedImage bi = getBufferedImageFromPoints(width, height, list, margin);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", outputStream);
        } catch (Exception e) {
            log.error("Could not transform list of points to png image.", e);
        }
        return outputStream.toByteArray();
    }

    /**
     * Create a buffered image, with specific width and height, from a list of points.
     */
    private static BufferedImage getBufferedImageFromPoints(int width, int height, List<List<Point>> points,
            int strokeWidth) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ig2.setColor(new Color(0, 0, 0));
        ig2.setStroke(new BasicStroke(strokeWidth));
        points.forEach(line ->
                {
                    if (line.size() == 1) {
                        ig2.fill(new Ellipse2D.Double(line.get(0).x, line.get(0).y, strokeWidth, strokeWidth));
                    } else {
                        for (int i = 1; i < line.size(); i++) {
                            ig2.drawLine(
                                    line.get(i).x,
                                    line.get(i).y,
                                    line.get(i - 1).x,
                                    line.get(i - 1).y
                            );
                        }
                    }
                }
        );
        ig2.dispose();
        return bi;
    }

    private static List<List<Point>> cleanPoints(List<List<Point>> points, int scaleHeight, int margin) {
         if (points == null) {
             log.error("Signature is missing in PDF document generation process.");
             return null;
         }
        points = removeConsecutiveDuplicatePoints(points);
        points = removeDuplicatesBetweenLines(points);
        points = removeOffset(points);
        return scalePoints(points, scaleHeight, margin);
    }

    private static List<List<Point>> removeOffset(List<List<Point>> points) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (List<Point> line : points) {
            for (Point point : line) {
                if (point.x < minX) {
                    minX = point.x;
                }
                if (point.y < minY) {
                    minY = point.y;
                }
            }
        }

        List newPoints = Lists.newArrayList();
        for (List<Point> line : points) {
            List newLine = Lists.newArrayList();
            for (Point point : line) {
                newLine.add(new Point(point.x - minX + 1, point.y - minY + 1));
            }
            newPoints.add(newLine);
        }
        return newPoints;
    }
    private static List<List<Point>> scalePoints(List<List<Point>> points, int scaledHeight, int margin) {
        int currentMaxHeight = 0;
        for (List<Point> line: points) {
            for (Point point: line) {
                if (point.y > currentMaxHeight) {
                    currentMaxHeight = point.y;
                }
            }
        }

        double resizeFactor = (double) (currentMaxHeight) / (scaledHeight - 2 * margin);

        List<List<Point>> newPoints = Lists.newArrayList();
        for (List<Point> line : points) {
            List<Point> newLine = Lists.newArrayList();
            for (Point point : line) {
                newLine.add(
                        new Point((int) (point.x / resizeFactor) + margin, (int) (point.y / resizeFactor) + margin));
            }
            newPoints.add(newLine);
        }

        return newPoints;
    }

    /**
     * Checks consecutive points and remove duplicates
     */
    public static List<List<Point>> removeConsecutiveDuplicatePoints(List<List<Point>> points) {
        List<List<Point>> newPoints = Lists.newArrayList();
        for (List<Point> line : points) {
            if (line.isEmpty()) {
                continue;
            }

            List<Point> newLine = Lists.newArrayList();
            int i = 0;
            Point prev = null;
            do {
                Point current = line.get(i);
                if (!Objects.equals(current, prev)) {
                    newLine.add(line.get(i));
                }

                prev = current;
                i++;
            } while (i < line.size());

            newPoints.add(newLine);
        }
        return newPoints;
    }

    /**
     * Removes duplicate points between different lines
     */
    private static List<List<Point>> removeDuplicatesBetweenLines(List<List<Point>> points) {
        if (points.size() == 1) {
            return points;
        }
        for (int i = points.size() - 1; i > 0; i--) {
            List<Point> prev = points.get(i - 1);
            List<Point> line = points.get(i);

            for (Point p : prev) {
                if (line.contains(p)) {
                    line.remove(p);
                }
            }
        }
        return points;
    }

    private static int ceilBasedOnMultiplier(int number, int multiple) {
        int result = number;

        if (number % multiple != 0) {
            int division = (number / multiple) + 1;
            result = division * multiple;
        }
        return result;
    }

}
