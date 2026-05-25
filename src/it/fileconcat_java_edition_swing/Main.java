package it.fileconcat_java_edition_swing;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        /*
         * Abilita il supporto HiDPI a livello JVM prima di qualsiasi
         * inizializzazione del toolkit Swing.
         *
         * - sun.java2d.uiScale.enabled: attiva lo scaling automatico su Java 9+
         *   (necessario su Windows; macOS e Linux di solito lo attivano già).
         * - sun.java2d.dpiaware: marca l'app come DPI-aware su Windows,
         *   evitando che il sistema operativo applichi un bitmap-scaling sfocato.
         * - swing.aatext / awt.useSystemAAFontSettings: antialiasing font,
         *   molto visibile su HiDPI.
         *
         * Devono essere impostate PRIMA che il GraphicsEnvironment venga
         * inizializzato, quindi prima di qualsiasi chiamata Swing/AWT.
         */
        System.setProperty("sun.java2d.uiScale.enabled",  "true");
        System.setProperty("sun.java2d.dpiaware",         "true");
        System.setProperty("swing.aatext",                "true");
        System.setProperty("awt.useSystemAAFontSettings", "lcd");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MergeGUI gui = new MergeGUI();
                gui.setVisible(true);
            }
        });
    }
}
