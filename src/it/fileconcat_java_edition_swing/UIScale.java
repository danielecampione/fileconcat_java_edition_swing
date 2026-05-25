package it.fileconcat_java_edition_swing;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Rileva il fattore di scala del display e fornisce metodi helper per
 * scalare font e dimensioni pixel in modo da supportare schermi HiDPI/Retina.
 *
 * Strategia:
 *   - Usa GraphicsConfiguration.getDefaultTransform().getScaleX().
 *   - Se Java 9+ ha già attivato l'auto-scaling JVM, il transform è 1.0
 *     (pixel logici) e non scaleremo manualmente, evitando il doppio-scale.
 *   - Se il JVM NON gestisce HiDPI (tipico Java 8 su 4K), il transform
 *     restituisce il reale rapporto fisico (es. 2.0) e noi lo applichiamo.
 */
public final class UIScale {

    /** Fattore di scala rilevato: 1.0 su display standard, 2.0 su 4K/Retina ecc. */
    public static final float FACTOR;

    static {
        FACTOR = detectFactor();
    }

    private UIScale() {}

    // ── Rilevamento ────────────────────────────────────────────────────────────

    private static float detectFactor() {
        try {
            GraphicsDevice gd = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
            float scale = (float) tx.getScaleX();
            // Valore ragionevole: tra 1.0 e 4.0
            if (scale >= 1.0f && scale <= 4.0f) {
                return scale;
            }
        } catch (Exception ignored) {}

        // Fallback: usa i DPI di sistema (sicuro su tutte le versioni Java)
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        float fromDpi = dpi / 96.0f;
        return fromDpi >= 1.0f ? fromDpi : 1.0f;
    }

    // ── Helper pubblici ────────────────────────────────────────────────────────

    /**
     * Scala un valore intero (altezze, padding, gap, border width…).
     * Esempio: {@code scale(8)} → 16 a FACTOR 2.0.
     */
    public static int scale(int value) {
        return Math.round(value * FACTOR);
    }

    /**
     * Scala una dimensione font in punti.
     * Esempio: {@code fontSize(14)} → 28 a FACTOR 2.0.
     */
    public static int fontSize(int pts) {
        return Math.max(1, Math.round(pts * FACTOR));
    }

    /**
     * Crea un {@link Font} con dimensione già scalata per il display corrente.
     */
    public static Font font(String name, int style, int pts) {
        return new Font(name, style, fontSize(pts));
    }

    /**
     * Crea un {@link Dimension} con larghezza e altezza già scalate.
     */
    public static Dimension dim(int w, int h) {
        return new Dimension(scale(w), scale(h));
    }

    /**
     * Crea un border vuoto con insets già scalati.
     */
    public static javax.swing.border.Border emptyBorder(int top, int left, int bottom, int right) {
        return javax.swing.BorderFactory.createEmptyBorder(
                scale(top), scale(left), scale(bottom), scale(right));
    }
}
