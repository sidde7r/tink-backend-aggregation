package se.tink.backend.utils;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import se.tink.libraries.account.identifiers.se.ClearingNumber;

public class ClearingNumberBankToProviderMapImpl implements ClearingNumberBankToProviderMap {
    private static final ImmutableMap<ClearingNumber.Bank, String> BANK_TO_PROVIDER = ImmutableMap.<ClearingNumber.Bank, String>builder()
            .put(ClearingNumber.Bank.ALANDSBANKEN, "alandsbanken")
            .put(ClearingNumber.Bank.AMFA_BANK, "amfabank")
            .put(ClearingNumber.Bank.AVANZA_BANK, "avanza")
            .put(ClearingNumber.Bank.BLUESTEP_FINANS, "bluestep")
            .put(ClearingNumber.Bank.CITIBANK, "citibank")
            .put(ClearingNumber.Bank.DANSKE_BANK, "danskebank")
            .put(ClearingNumber.Bank.DANSKE_BANK_SVERIGE, "danskebank")
            .put(ClearingNumber.Bank.DEN_NORSKE_BANK, "dnb")
            .put(ClearingNumber.Bank.DEN_NORSKE_BANK_SVERIGE, "dnb")
            .put(ClearingNumber.Bank.EKOBANKEN, "ekobanken")
            .put(ClearingNumber.Bank.ERIK_PENSER_BANKAKTIEBOLAG, "erikpenser")
            .put(ClearingNumber.Bank.FOREX_BANK, "forex")
            .put(ClearingNumber.Bank.FORTIS_BANK, "fortisbnpparibas")
            .put(ClearingNumber.Bank.HANDELSBANKEN, "handelsbanken")
            .put(ClearingNumber.Bank.ICA_BANKEN, "icabanken")
            .put(ClearingNumber.Bank.IKANO_BANK, "ikanokort")
            .put(ClearingNumber.Bank.JAKBANKEN, "jakbanken")
            .put(ClearingNumber.Bank.LANDSHYPOTEK, "landshypotek")
            .put(ClearingNumber.Bank.LANSFORSAKRINGAR_BANK, "lansforsakringar")
            //.put(ClearingNumber.Bank.LAN_O_SPAR_BANK, ) No provider/image name yet
            .put(ClearingNumber.Bank.MARGINALEN_BANK, "marginalen")
            .put(ClearingNumber.Bank.NORDAX_BANK, "nordax")
            .put(ClearingNumber.Bank.NORDEA, "nordea")
            .put(ClearingNumber.Bank.NORDEA_PERSONKONTO, "nordea")
            .put(ClearingNumber.Bank.NORDNET_BANK, "nordnet")
            .put(ClearingNumber.Bank.PLUSGIROT, "plusgirot")
            .put(ClearingNumber.Bank.PLUSGIROT_BANK, "plusgirot")
            .put(ClearingNumber.Bank.RESURS_BANK, "resursbank")
            //.put(ClearingNumber.Bank.RIKSGALDEN, ) No provider/image yet
            .put(ClearingNumber.Bank.ROYAL_BANK_OF_SCOTLAND, "royalbankofscotland")
            .put(ClearingNumber.Bank.SAMPO_BANK, "sampo")
            .put(ClearingNumber.Bank.SANTANDER_CONSUMER_BANK, "santander")
            .put(ClearingNumber.Bank.SBAB, "sbab")
            .put(ClearingNumber.Bank.SEB, "seb")
            .put(ClearingNumber.Bank.SKANDIABANKEN, "skandiabanken")
            .put(ClearingNumber.Bank.SPARBANKEN_ORESUND, "sparbankenoresund-bankid")
            //.put(ClearingNumber.Bank.SPARBANKEN_SYD, ) No provider/image name yet
            //.put(ClearingNumber.Bank.SVERIGES_RIKSBANK, ) No provider/image name yet
            .put(ClearingNumber.Bank.SWEDBANK, "swedbank")
            .build();

    @Override
    public Optional<String> getProviderForBank(ClearingNumber.Bank bank) {
        return Optional.ofNullable(BANK_TO_PROVIDER.get(bank));
    }
}
