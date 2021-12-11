package terrails.healthoverlay;

import terrails.healthoverlay.heart.HeartPiece;

import java.util.List;

public class ModConfiguration {

    public static boolean absorptionOverHealth;
    public static AbsorptionMode absorptionOverHealthMode;

    public static boolean healthVanilla;
    public static HeartPiece[] healthColors;
    public static HeartPiece[] healthPoisonColors;
    public static HeartPiece[] healthWitherColors;
    public static HeartPiece[] healthFrozenColors;

    public static boolean absorptionVanilla;
    public static HeartPiece[] absorptionColors;
    public static HeartPiece[] absorptionPoisonColors;
    public static HeartPiece[] absorptionWitherColors;
    public static HeartPiece[] absorptionFrozenColors;

    public enum AbsorptionMode {
        BEGINNING, AFTER_HEALTH
    }

    public static HeartPiece[] getColors(List<? extends String> stringValues, boolean absorption, boolean effect) {
        HeartPiece[] colors;
        int offset;
        if (absorption && effect && (stringValues.size() != 2 && stringValues.size() != 0)) {
            Constants.LOGGER.error("Absorption effect colors must be empty or have 2 values.");
            throw new IllegalArgumentException(stringValues.toString());
        } else if (absorption && !effect && absorptionVanilla) {
            colors = new HeartPiece[stringValues.size() + 1];
            colors[0] = HeartPiece.VANILLA_ABSORPTION;
            offset = 1;
        } else if (!absorption && effect && stringValues.size() == 1) {
            colors = new HeartPiece[2];
            colors[0] = HeartPiece.VANILLA_HEALTH;
            offset = 1;
        } else if (!absorption && !effect && healthVanilla) {
            colors = new HeartPiece[stringValues.size() + 1];
            colors[0] = HeartPiece.VANILLA_HEALTH;
            offset = 1;
        } else if (stringValues.isEmpty()) {
            return null;
        } else {
            colors = new HeartPiece[stringValues.size()];
            offset = 0;
        }

        for (int i = 0; i < stringValues.size(); i++) {
            colors[i + offset] = HeartPiece.custom(stringValues.get(i), absorption);
        }
        return colors;
    }
}

