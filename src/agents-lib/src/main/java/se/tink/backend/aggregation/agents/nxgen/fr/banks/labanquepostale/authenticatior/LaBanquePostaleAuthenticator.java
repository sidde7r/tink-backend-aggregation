package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.authenticatior;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class LaBanquePostaleAuthenticator implements Authenticator {

    private final LaBanquePostaleApiClient apiClient;

    public LaBanquePostaleAuthenticator(
            LaBanquePostaleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String numpadUrlExt = apiClient.initLogin();
        byte[] numpadImg = apiClient.getLoginNumpad(numpadUrlExt);
        Map<Integer, String> numpad = parseNumpad(numpadImg);
        String password = buildPassword(credentials.getField(Field.Key.PASSWORD), numpad);

        Optional<String> errorCode = apiClient.submitLogin(
                credentials.getField(Field.Key.USERNAME),
                password);

        if (errorCode.isPresent()) {
            switch (errorCode.get()) {

            case LaBanquePostaleConstants.ErrorCodes.INCORRECT_CREDENTIALS:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            default:
                throw new IllegalStateException(
                        String.format(LaBanquePostaleConstants.ErrorMessages.UNKNOWN_ERROR,
                                errorCode.get()));
            }
        }
    }

    /**
     * Simulates entering the passcode on the numpad, returning the result.
     */
    private String buildPassword(String passCode, Map<Integer, String> numpad) {

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < passCode.length(); i++) {

            result.append(numpad.get(Character.getNumericValue(passCode.charAt(i))));
        }

        return result.toString();
    }

    /**
     * Splits the numpad into an array of images where each image represent one key.
     */
    private BufferedImage[] splitNumpad(BufferedImage source) {

        final int width = LaBanquePostaleConstants.AuthConfig.NUMPAD_WIDTH;
        final int height = LaBanquePostaleConstants.AuthConfig.NUMPAD_HEIGHT;
        final int padding = LaBanquePostaleConstants.AuthConfig.NUMPAD_KEY_PADDING;

        final int chunkWidth = source.getWidth() / width;
        final int chunkHeight = source.getHeight() / height;

        BufferedImage keys[] = new BufferedImage[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int index = x + y * width;
                keys[index] = source.getSubimage(
                        x * chunkWidth + padding,
                        y * chunkHeight + padding,
                        chunkWidth - 2 * padding,
                        chunkHeight - 2 * padding);
            }
        }

        return keys;
    }

    /**
     * Convert numpad byte[] image to a map of (keyIndex -> keyValue)
     *
     * @throws IllegalStateException - If an error in image recognition is found. (Duplicate or
     *                               missing keys) And if byte[] cannot be converted into a
     *                               BufferedImage.
     */
    private Map<Integer, String> parseNumpad(byte[] numpadBytes) {

        try {

            BufferedImage crypticNumpad = ImageIO.read(new ByteArrayInputStream(numpadBytes));
            return parseNumpad(splitNumpad(crypticNumpad));

        } catch (IOException e) {

            throw new IllegalStateException(LaBanquePostaleConstants.ErrorMessages.NO_NUMPAD_IMAGE);
        }
    }

    /**
     * Uses image recognition to read one number from each image and mapping them to their value
     * decided by their location on the numpad. Empty keys are discaded.
     *
     * @throws IllegalStateException - If an error in image recognition is found. (Duplicate or
     *                               missing keys)
     */
    private Map<Integer, String> parseNumpad(BufferedImage[] images) {

        Map<Integer, String> numpad = new HashMap<>(
                LaBanquePostaleConstants.AuthConfig.PASSWORD_LENGTH);

        for (int keyIndex = 0; keyIndex < images.length; keyIndex++) {
            String parsedDigit = ImageRecognizer.ocr(images[keyIndex], Color.BLUE);

            if (!parsedDigit.isEmpty()) {

                int digit = Integer.parseInt(parsedDigit);
                if (numpad.containsKey(digit)) {

                    throw new IllegalStateException(
                            LaBanquePostaleConstants.ErrorMessages.DUPLICATE_DIGITS);
                }

                // Value format: 01, 02, ... 14, 15
                numpad.put(digit, String.format("%02d", keyIndex));
            }
        }

        if (numpad.size() != LaBanquePostaleConstants.AuthConfig.PASSWORD_LENGTH) {

            throw new IllegalStateException(
                    LaBanquePostaleConstants.ErrorMessages.WRONG_DIGIT_COUNT);
        }

        return numpad;
    }
}
