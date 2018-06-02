package se.tink.backend.common.merchants;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.core.Merchant;
import se.tink.backend.utils.LogUtils;

public class MerchantWhiteSpaceTrimmer {

    private static final LogUtils log = new LogUtils(MerchantWhiteSpaceTrimmer.class);

    public List<Merchant> TrimWhiteSpace(List<Merchant> merchants) {

        List<Merchant> result = new ArrayList<>();

        for (Merchant m : merchants) {

            boolean trimmed = false;

            String sniCode = m.getSniCode();
            String address = m.getAddress();
            String city = m.getCity();
            String country = m.getCountry();
            String name = m.getName();
            String organizationId = m.getOrganizationId();
            String postalCode = m.getPostalCode();
            String website = m.getWebsite();
            String phoneNumber = m.getPhoneNumber();
            String formattedAddress = m.getFormattedAddress();

            if (sniCode != null && !sniCode.equals(m.getSniCode().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", sniCode, sniCode.trim(), m.getId()));
                m.setSniCode(m.getSniCode().trim());
                trimmed = true;
            }

            if (address != null && !address.equals(m.getAddress().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", address, address.trim(), m.getId()));
                m.setAddress(m.getAddress().trim());
                trimmed = true;
            }

            if (city != null && !city.equals(m.getCity().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", city, city.trim(), m.getId()));
                m.setCity(m.getCity().trim());
                trimmed = true;
            }

            if (country != null && !country.equals(m.getCountry().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", country, country.trim(), m.getId()));
                m.setCountry(m.getCountry().trim());
                trimmed = true;
            }

            if (name != null && !name.equals(m.getName().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", name, name.trim(), m.getId()));
                m.setName(m.getName().trim());
                trimmed = true;
            }

            if (organizationId != null && !organizationId.equals(m.getOrganizationId().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", organizationId, organizationId.trim(),
                        m.getId()));
                m.setOrganizationId(m.getOrganizationId().trim());
                trimmed = true;
            }

            if (postalCode != null && !postalCode.equals(m.getPostalCode().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", postalCode, postalCode.trim(),
                        m.getId()));
                m.setPostalCode(m.getPostalCode().trim());
                trimmed = true;
            }

            if (website != null && !website.equals(m.getWebsite().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", website, website.trim(), m.getId()));
                m.setWebsite(m.getWebsite().trim());
                trimmed = true;
            }

            if (phoneNumber != null && !phoneNumber.equals(m.getPhoneNumber().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", phoneNumber, phoneNumber.trim(),
                        m.getId()));
                m.setPhoneNumber(m.getPhoneNumber().trim());
                trimmed = true;
            }

            if (formattedAddress != null && !formattedAddress.equals(m.getFormattedAddress().trim())) {
                log.info(String.format("Trimming [%s] to [%s] for merchant %s", formattedAddress,
                        formattedAddress.trim(), m.getId()));
                m.setFormattedAddress(m.getFormattedAddress().trim());
                trimmed = true;
            }

            if (trimmed) {
                result.add(m);
            }

        }

        return result;
    }

}
