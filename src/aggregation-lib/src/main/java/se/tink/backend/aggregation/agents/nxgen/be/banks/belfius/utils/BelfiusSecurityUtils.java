package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils;

import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public class BelfiusSecurityUtils {

    public static String createSignature(
            String challenge, String deviceToken, String panNumber, String contractNumber, String password) {
        String challengeUpperCase = challenge.toUpperCase();
        String panNumberStripped = panNumber.replace(" ", "");
        String contractPasswordHash = hash(String.format("%s%s", contractNumber, password));
        return hash(String.format("%s%s%s%s%s",
                challengeUpperCase, challengeUpperCase, deviceToken, panNumberStripped, contractPasswordHash));
    }

    //challenge|iban(85)|iban(to/94)|amount(0,01) EUR
    public static String createTransferSignature(String challenge, String sourceIban, String destinationIban, String amount, String currency){
        String toSha = challenge + sourceIban + destinationIban + amount + " " + currency;
        return hash(toSha);
    }

    public static String hash(String data) {
        return Hash.sha1AsHex(data).toUpperCase();
    }

    public static String generateTransactionId() {
        return "signIWSAuthentication"
                + System.currentTimeMillis()/1000 + "." + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateTransferId() {
        return "submitTransfer"
                + System.currentTimeMillis()/1000 + "." + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateBeneficiaryId() {
        return "signBeneficiary"
                + System.currentTimeMillis()/1000 + "." + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateSignTransferId() {
        return "signTransfer"
                + System.currentTimeMillis()/1000 + "." + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateDeviceToken() {
        return RandomStringUtils.random(53, "0123456789ABCDEF");
    }
}
