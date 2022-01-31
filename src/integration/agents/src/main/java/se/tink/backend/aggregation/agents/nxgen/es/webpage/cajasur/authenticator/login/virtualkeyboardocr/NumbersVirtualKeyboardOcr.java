package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

class NumbersVirtualKeyboardOcr {
    private final NumberVirtualKeyboardOcrTravelingParams numberVirtualKeyboardOcrTravelingParams;
    private final VirtualKeyboardImageParameters virtualKeyboardImageParameters;
    private final Map<String, String> virtualKeyboardedNumbers = new HashMap<>();
    private final CRC32 checksum = new CRC32();

    NumbersVirtualKeyboardOcr(
            final VirtualKeyboardImageParameters virtualKeyboardImageParameters,
            final List<SubImageNumberCheckSumCorrelation> numberCheckSums,
            final BufferedImage virtualKeyboardImage) {
        this.virtualKeyboardImageParameters = virtualKeyboardImageParameters;
        this.numberVirtualKeyboardOcrTravelingParams =
                new NumberVirtualKeyboardOcrTravelingParams(virtualKeyboardImageParameters);

        doCcr(
                numberCheckSums.stream()
                        .collect(
                                Collectors.toMap(
                                        SubImageNumberCheckSumCorrelation::getCheckSum,
                                        SubImageNumberCheckSumCorrelation::getNumber)),
                virtualKeyboardImage);
    }

    public String getVirtualKeyboardValueForNumbersSequence(final String numbersSequence) {
        StringBuilder seq = new StringBuilder();
        for (int j = 0; j < numbersSequence.length(); j++) {
            seq.append(virtualKeyboardedNumbers.get(numbersSequence.substring(j, j + 1)));
        }
        return seq.toString();
    }

    private void doCcr(
            final Map<String, String> checkSumNumberCorrelation,
            final BufferedImage virtualKeyboardImage) {
        String key = "";

        for (int k = 0; k < 10; k++) {
            key = computeCheckSumForNextNumber(virtualKeyboardImage);
            if (!checkSumNumberCorrelation.containsKey(key)) {
                throw new IllegalStateException(
                        "Couldn't get sequence from image and password (images changed?)");
            }
            virtualKeyboardedNumbers.put(checkSumNumberCorrelation.get(key), "" + k);
        }
    }

    private String computeCheckSumForNextNumber(final BufferedImage virtualKeyboardImage) {
        byte[] arrayN =
                proccessImage(
                        virtualKeyboardImage.getSubimage(
                                numberVirtualKeyboardOcrTravelingParams.getHorizontalVal(),
                                numberVirtualKeyboardOcrTravelingParams.getVerticalVal(),
                                virtualKeyboardImageParameters.getImgWidth(),
                                virtualKeyboardImageParameters.getImgHeight()));
        checksum.reset();
        checksum.update(arrayN);
        numberVirtualKeyboardOcrTravelingParams.computeNextNumberPosition();
        return checksum.getValue() + "-" + arrayN.length;
    }

    private byte[] proccessImage(final BufferedImage image) {
        byte[] arrayN = new byte[image.getWidth() * image.getHeight()];
        int j3 = 0;
        int backColor = 110;
        int foreColor = 0;
        byte backColorB = 110;
        byte foreColorB = 0;

        int qpixel = 0xFFFFFFFF;
        int positiveThreshold = 100_000;
        for (int j = 0; j < image.getWidth(); j++) {
            for (int j2 = 0; j2 < image.getHeight(); j2++) {
                arrayN[j3] = (byte) image.getRGB(j, j2);
                int ipixel = qpixel - image.getRGB(j, j2);
                if (ipixel < positiveThreshold) {
                    arrayN[j3] = foreColorB;
                    image.setRGB(j, j2, foreColor);
                } else {
                    arrayN[j3] = backColorB;
                    image.setRGB(j, j2, backColor);
                }
                j3++;
            }
        }
        return arrayN;
    }
}
