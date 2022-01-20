package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class VirtualKeyboardOcr {

    public String getNumbersSequenceFromImage(
            BufferedImage image, String password, boolean type1, boolean isEnterprise) {
        // Umbrales para los niveles de claro-oscuro

        int imgWidth = type1 ? 9 : 11;
        int imgHeight = 13;
        int firstRowHorizontalInit = type1 ? 7 : 11;
        int firstRowVerticalInit = type1 ? 4 : 9;
        int horizontalStep = 34;
        int verticalStep = 34;

        if (isEnterprise) {
            imgWidth = 8;
            imgHeight = 12;
            firstRowHorizontalInit = 8;
            firstRowVerticalInit = 5;
            horizontalStep = 28;
            verticalStep = 28;
        }
        int hcount = 0;
        String key = "";

        String imageNumber = "";
        try {
            BufferedImage imageN = null;
            // Relleno el Map de Numeros en byte[]
            Map<String, String> seqMap = new HashMap<>(10);
            int horizontalVal = firstRowHorizontalInit;
            int verticalVal = firstRowVerticalInit;
            for (int k = 0; k < 10; k++) {
                imageN = image.getSubimage(horizontalVal, verticalVal, imgWidth, imgHeight);
                byte[] arrayN = new byte[imageN.getWidth() * imageN.getHeight()];

                proccessImage(arrayN, imageN);

                CRC32 checksum = new CRC32();
                checksum.reset();
                checksum.update(arrayN);
                key = checksum.getValue() + "-" + arrayN.length;
                imageNumber = getImageNumber(key);
                seqMap.put(imageNumber, String.valueOf(k));
                horizontalVal += horizontalStep;
                if (hcount == 4) {
                    horizontalVal = firstRowHorizontalInit;
                    verticalVal += verticalStep;
                    hcount = 0;
                } else {
                    hcount++;
                }
            }
            StringBuilder seq = new StringBuilder();
            // Recorro el Map para devolver la secuencia
            for (int j = 0; j < password.length(); j++) {
                if (seqMap.containsKey(password.substring(j, j + 1))) {
                    seq.append(seqMap.get(password.substring(j, j + 1)));
                } else {
                    throw new IllegalStateException(
                            "Couldn't get sequence from image and password (images changed?)");
                }
            }
            return seq.toString();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error in the images in order to get the password sequence", e);
        }
    }

    private void proccessImage(byte[] arrayN, BufferedImage imageN) {
        int j3 = 0;
        int backColor = 110;
        int foreColor = 0;
        byte backColorB = 110;
        byte foreColorB = 0;

        int qpixel = 0xFFFFFFFF;
        int positiveThreshold = 100_000;
        for (int j = 0; j < imageN.getWidth(); j++) {
            for (int j2 = 0; j2 < imageN.getHeight(); j2++) {
                arrayN[j3] = (byte) imageN.getRGB(j, j2);
                int ipixel = qpixel - imageN.getRGB(j, j2);
                // Elimino el ruido de la imagen con umbral
                // Los claros van a (Blanco)
                // Los oscuros a (Negro)
                if (ipixel < positiveThreshold) {
                    arrayN[j3] = foreColorB;
                    imageN.setRGB(j, j2, foreColor);
                } else {
                    arrayN[j3] = backColorB;
                    imageN.setRGB(j, j2, backColor);
                }
                j3++;
            }
        }
    }

    private String getImageNumber(String key) {
        if (KEYBOARD_NUMBERS.containsKey(key)) {
            return KEYBOARD_NUMBERS.get(key);
        } else {
            throw new IllegalStateException(
                    "getImageNumberBBK - Couldn't get sequence from image and password (images changed?)");
        }
    }

    public static final Map<String, String> KEYBOARD_NUMBERS = new HashMap<>(30);

    static {
        // Sca movs
        KEYBOARD_NUMBERS.put("3654922649-143", "0");
        KEYBOARD_NUMBERS.put("2130343582-143", "1");
        KEYBOARD_NUMBERS.put("291315566-143", "2");
        KEYBOARD_NUMBERS.put("1410499081-143", "3");
        KEYBOARD_NUMBERS.put("2777058048-143", "4");
        KEYBOARD_NUMBERS.put("3231203627-143", "5");
        KEYBOARD_NUMBERS.put("2890145617-143", "6");
        KEYBOARD_NUMBERS.put("3822793684-143", "7");
        KEYBOARD_NUMBERS.put("386154722-143", "8");
        KEYBOARD_NUMBERS.put("2947001144-143", "9");

        // Login
        KEYBOARD_NUMBERS.put("561125109-117", "0");
        KEYBOARD_NUMBERS.put("800784789-117", "1");
        KEYBOARD_NUMBERS.put("3960919832-117", "2");
        KEYBOARD_NUMBERS.put("2591862343-117", "3");
        KEYBOARD_NUMBERS.put("3878562455-117", "4");
        KEYBOARD_NUMBERS.put("2542234190-117", "5");
        KEYBOARD_NUMBERS.put("2494823981-117", "6");
        KEYBOARD_NUMBERS.put("1450603399-117", "7");
        KEYBOARD_NUMBERS.put("1178992991-117", "8");
        KEYBOARD_NUMBERS.put("3043009662-117", "9");

        // New image enterprise
        KEYBOARD_NUMBERS.put("1627713458-96", "0");
        KEYBOARD_NUMBERS.put("1752858395-96", "1");
        KEYBOARD_NUMBERS.put("3522085418-96", "2");
        KEYBOARD_NUMBERS.put("4049401402-96", "3");
        KEYBOARD_NUMBERS.put("1405771207-96", "4");
        KEYBOARD_NUMBERS.put("2140092309-96", "5");
        KEYBOARD_NUMBERS.put("1661481153-96", "6");
        KEYBOARD_NUMBERS.put("1428844876-96", "7");
        KEYBOARD_NUMBERS.put("3545991190-96", "8");
        KEYBOARD_NUMBERS.put("2516460739-96", "9");
    }
}
