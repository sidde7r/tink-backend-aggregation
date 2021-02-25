package se.tink.libraries.account.identifiers.se;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;

public class IbanCodes {

    // The padding properties below are hardcoded for Sweden.
    public static final Integer ACCOUNT_NUMBER_PADDING_LENGTH = 17;
    public static final char PADDING_CHAR = '0';

    // Bank codes shamelessly ripped (25/02/2021) from https://www.iban.se/ &
    // https://www.swedishbankers.se/media/1255/bank_id_iban_bic.pdf
    // The key correlates to the bank in ClearingNumber.java.
    private static final ImmutableMap<Bank, Integer> bankCodes =
            ImmutableMap.<ClearingNumber.Bank, Integer>builder()
                    .put(Bank.DANSKE_BANK, 120)
                    .put(Bank.ALANDSBANKEN, 230)
                    .put(Bank.NORDEA, 300)
                    .put(Bank.SEB, 500)
                    .put(Bank.HANDELSBANKEN, 600)
                    .put(Bank.SWEDBANK, 800)
                    .put(Bank.LANSFORSAKRINGAR_BANK, 902)
                    .put(Bank.NORDNET_BANK, 910)
                    .put(Bank.SKANDIABANKEN, 915)
                    .put(Bank.IKANO_BANK, 917)
                    .put(Bank.MARGINALEN_BANK, 923)
                    .put(Bank.ICA_BANKEN, 927)
                    .put(Bank.RESURS_BANK, 928)
                    .put(Bank.SPARBANKEN_SYD, 957)
                    .build();

    public static Integer getBankCode(Bank bank) {
        if (bankCodes.containsKey(bank)) {
            return bankCodes.get(bank);
        }
        throw new NotImplementedException(
                "The bank is not implemented for calculating IBAN. Please add it in the bank code map.");
    }
}
