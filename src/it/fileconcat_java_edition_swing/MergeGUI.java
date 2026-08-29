package it.fileconcat_java_edition_swing; 

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MergeGUI extends JFrame {

    private static final long serialVersionUID = 1319929220092210305L;
    
	// ── Colori ────────────────────────────────────────────────────────────────
    private static final Color C_BG      = new Color(0xf7f5f2);
    private static final Color C_SURFACE = Color.WHITE;
    private static final Color C_ACCENT  = new Color(0xe05c2a);
    private static final Color C_ACCH    = new Color(0xc94d1f);
    private static final Color C_DROP    = new Color(0xfff8f5);
    private static final Color C_DROP_A  = new Color(0xfde8df);
    private static final Color C_OK      = new Color(0x2d9e6b);
    private static final Color C_ERR     = new Color(0xd63b3b);
    private static final Color C_TEXT    = new Color(0x1a1a1a);
    private static final Color C_MUTED   = new Color(0x888077);
    private static final Color C_BORDER  = new Color(0xddd8d0);
    private static final Color C_SECBTN  = new Color(0xddd8d0);

    // ── Shorthand locali per UIScale (leggibilità) ─────────────────────────────
    private static int s(int n)           { return UIScale.scale(n);    }
    private static Font f(String n, int style, int pts) {
        return UIScale.font(n, style, pts);
    }

    // ── Stato ─────────────────────────────────────────────────────────────────
    private List<String>           sources  = new ArrayList<String>();
    private Map<String, JCheckBox> extBoxes = new LinkedHashMap<String, JCheckBox>();
    private JPanel                 extGrid;
    private boolean                running  = false;

    // ── Widget ────────────────────────────────────────────────────────────────
    private JLabel       dropLabel;
    private JPanel       dropPanel;
    private JTextField   outputField;
    private JProgressBar progressBar;
    private JLabel       statusLabel;
    private JButton      runButton;

    // ── Costruttore ───────────────────────────────────────────────────────────
    public MergeGUI() {
        setTitle("Unisci File");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(s(620), s(820));
        setMinimumSize(new Dimension(s(560), s(720)));
        setLocationRelativeTo(null);
        buildUI();
    }

    // ── Costruzione interfaccia ────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(s(20));
        scroll.setBackground(C_BG);
        root.add(scroll, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, s(24), s(14)));
        hdr.setBackground(C_SURFACE);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, s(1), 0, C_BORDER));

        JLabel title = new JLabel("\uD83D\uDCC4  Unisci File");
        title.setFont(f("Georgia", Font.BOLD, 28));
        title.setForeground(C_ACCENT);
        hdr.add(title);

        JLabel sub = new JLabel("Raccoglie i tuoi file di testo in uno solo");
        sub.setFont(f("SansSerif", Font.PLAIN, 15));
        sub.setForeground(C_MUTED);
        hdr.add(sub);

        return hdr;
    }

    // ── Body (scrollabile) ────────────────────────────────────────────────────
    private JPanel buildBody() {
        ScrollablePanel body = new ScrollablePanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_BG);
        body.setBorder(BorderFactory.createEmptyBorder(s(16), s(24), s(24), s(24)));

        body.add(sectionLabel("Trascina qui file, cartelle o archivi .zip"));
        body.add(buildDropZone());
        body.add(Box.createVerticalStrut(s(8)));
        body.add(buildBrowseRow());
        body.add(Box.createVerticalStrut(s(14)));

        body.add(sectionLabel("Nome del file da creare"));
        body.add(buildOutputCard());
        body.add(Box.createVerticalStrut(s(14)));

        body.add(sectionLabel("Che tipo di file vuoi raccogliere?"));
        body.add(buildExtCard());
        body.add(Box.createVerticalStrut(s(14)));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(8)));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(progressBar);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(f("SansSerif", Font.PLAIN, 15));
        statusLabel.setForeground(C_MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(s(6), 0, s(6), 0));
        body.add(statusLabel);

        runButton = buildRunButton();
        body.add(runButton);

        return body;
    }

    // ── Drop zone ─────────────────────────────────────────────────────────────
    private JPanel buildDropZone() {
        dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBackground(C_DROP);
        dropPanel.setBorder(dropBorder(C_BORDER));
        dropPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(110)));
        dropPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        dropLabel = new JLabel(
            "<html><center>\u2B07&nbsp; Trascina qui<br>"
            + "file &nbsp;&middot;&nbsp; cartelle &nbsp;&middot;&nbsp; .zip</center></html>",
            SwingConstants.CENTER);
        dropLabel.setFont(f("SansSerif", Font.PLAIN, 17));
        dropLabel.setForeground(C_MUTED);
        dropLabel.setBorder(BorderFactory.createEmptyBorder(s(16), s(16), s(16), s(16)));
        dropPanel.add(dropLabel, BorderLayout.CENTER);

        new DropTarget(dropPanel, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent e) {
                dropPanel.setBackground(C_DROP_A);
                dropPanel.setBorder(dropBorder(C_ACCENT));
            }
            @Override public void dragExit(DropTargetEvent e)  { resetDropBorder(); }
            @Override public void drop(DropTargetDropEvent e) {
                resetDropBorder();
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = e.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        List<String> paths = new ArrayList<String>();
                        for (File file : files) paths.add(file.getAbsolutePath());
                        addSources(paths);
                    }
                    e.dropComplete(true);
                } catch (Exception ex) { e.dropComplete(false); }
            }
        });

        return dropPanel;
    }

    private CompoundBorder dropBorder(Color lineColor) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(lineColor, s(2), true),
            BorderFactory.createEmptyBorder(s(8), s(8), s(8), s(8)));
    }

    private void resetDropBorder() {
        dropPanel.setBackground(C_DROP);
        dropPanel.setBorder(dropBorder(C_BORDER));
    }

    // ── Riga pulsanti sfoglia ─────────────────────────────────────────────────
    private JPanel buildBrowseRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(C_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(40)));

        JButton folderBtn = secondaryButton("Scegli cartella\u2026");
        folderBtn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { chooseFolder(); }
        });

        JButton filesBtn = secondaryButton("Scegli file\u2026");
        filesBtn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { chooseFiles(); }
        });

        JButton clearBtn = clearButton("\u00D7  Svuota elenco");
        clearBtn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { clearSources(); }
        });

        row.add(folderBtn);
        row.add(Box.createHorizontalStrut(s(8)));
        row.add(filesBtn);
        row.add(Box.createHorizontalGlue()); // spinge il bottone Svuota a destra
        row.add(clearBtn);
        return row;
    }

    // ── Card nome output ──────────────────────────────────────────────────────
    private JPanel buildOutputCard() {
        JPanel card = card(s(60));
        card.setLayout(new BorderLayout());

        outputField = new JTextField("output.txt");
        outputField.setFont(f("SansSerif", Font.PLAIN, 17));
        outputField.setForeground(C_TEXT);
        outputField.setBackground(C_SURFACE);
        outputField.setBorder(BorderFactory.createEmptyBorder(s(10), s(14), s(10), s(14)));
        card.add(outputField, BorderLayout.CENTER);
        return card;
    }

    // ── Card estensioni ───────────────────────────────────────────────────────
    private JPanel buildExtCard() {
        JPanel card = card(400); // altezza gestita dallo scroll interno
        card.setLayout(new BorderLayout());

        extGrid = new JPanel(new GridLayout(0, 4, s(4), s(4)));
        extGrid.setBackground(C_SURFACE);
        extGrid.setBorder(BorderFactory.createEmptyBorder(s(12), s(14), s(8), s(14)));

        JScrollPane scroll = new JScrollPane(extGrid);
        scroll.setPreferredSize(new Dimension(s(500), s(185)));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(s(16));
        card.add(scroll, BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, s(8), s(8)));
        ctrl.setBackground(C_SURFACE);
        ctrl.setBorder(BorderFactory.createMatteBorder(s(1), 0, 0, 0, C_BORDER));

        JButton selAll = secondaryButton("Seleziona tutti");
        selAll.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { setAll(true); }
        });
        JButton deselAll = secondaryButton("Deseleziona tutti");
        deselAll.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { setAll(false); }
        });

        ctrl.add(selAll);
        ctrl.add(deselAll);
        card.add(ctrl, BorderLayout.SOUTH);
        return card;
    }

    // ── Bottone principale ────────────────────────────────────────────────────
    private JButton buildRunButton() {
        JButton btn = new JButton("Unisci i file  \u2192");
        btn.setFont(f("SansSerif", Font.BOLD, 20));
        btn.setBackground(C_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, s(58)));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(58)));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(C_ACCH);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(C_ACCENT);
            }
        });
        btn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { runMerge(); }
        });
        return btn;
    }

    // ── Widget helpers ────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(f("Georgia", Font.PLAIN, 17));
        l.setForeground(C_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(s(6), 0, s(4), 0));
        return l;
    }

    /** Crea una card (pannello bianco con bordo arrotondato). */
    private JPanel card(int maxHeight) {
        JPanel p = new JPanel();
        p.setBackground(C_SURFACE);
        p.setBorder(BorderFactory.createLineBorder(C_BORDER, s(1), true));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(maxHeight)));
        return p;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(f("SansSerif", Font.PLAIN, 15));
        b.setBackground(C_SECBTN);
        b.setForeground(C_TEXT);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Pulsante distruttivo: sfondo rosso tenue, testo rosso. */
    private JButton clearButton(String text) {
        JButton b = new JButton(text);
        b.setFont(f("SansSerif", Font.PLAIN, 15));
        b.setBackground(new Color(0xf5e0dc));
        b.setForeground(C_ERR);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0xeecbc5));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(0xf5e0dc));
            }
        });
        return b;
    }

    // ── Logica selezione sorgenti ─────────────────────────────────────────────

    /**
     * Aggiunge i percorsi alla lista esistente (senza sostituirla),
     * deduplicando i duplicati. Poi rescansiona TUTTE le sorgenti accumulate.
     */
    private void addSources(final List<String> newPaths) {
        // Deduplicazione: inserisco in un LinkedHashSet per preservare ordine
        LinkedHashSet<String> merged = new LinkedHashSet<String>(sources);
        merged.addAll(newPaths);
        sources = new ArrayList<String>(merged);

        updateDropLabel();
        setStatus("Scansione estensioni\u2026", C_MUTED);

        final List<String> allCopy = new ArrayList<String>(sources);
        new Thread(new Runnable() {
            @Override public void run() {
                final List<String> found = BusinessLogic.scanExtensions(allCopy);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() { applyFoundExtensions(found); }
                });
            }
        }).start();
    }

    /** Aggiorna il testo della drop zone in base al numero di sorgenti accumulate. */
    private void updateDropLabel() {
        if (sources.isEmpty()) {
            dropLabel.setText(
                "<html><center>\u2B07&nbsp; Trascina qui<br>"
                + "file &nbsp;&middot;&nbsp; cartelle &nbsp;&middot;&nbsp; .zip</center></html>");
            dropLabel.setForeground(C_MUTED);
        } else if (sources.size() == 1) {
            String name = new File(sources.get(0)).getName();
            if (name.isEmpty()) name = sources.get(0);
            dropLabel.setText("<html><center>\u2713&nbsp; " + escapeHtml(name) + "</center></html>");
            dropLabel.setForeground(C_OK);
        } else {
            dropLabel.setText(
                "<html><center>\u2713&nbsp; " + sources.size() + " elementi selezionati</center></html>");
            dropLabel.setForeground(C_OK);
        }
    }

    /** Svuota l'elenco sorgenti e riporta la UI allo stato iniziale. */
    private void clearSources() {
        if (sources.isEmpty()) return;
        sources.clear();
        updateDropLabel();
        extBoxes.clear();
        extGrid.removeAll();
        extGrid.revalidate();
        extGrid.repaint();
        setStatus(" ", C_MUTED);
    }

    private void applyFoundExtensions(List<String> found) {
        extBoxes.clear();
        extGrid.removeAll();

        for (String ext : found) {
            JCheckBox cb = new JCheckBox("." + ext);
            cb.setSelected(true);
            cb.setFont(f("SansSerif", Font.PLAIN, 16));
            cb.setBackground(C_SURFACE);
            cb.setForeground(C_TEXT);
            cb.setFocusPainted(false);
            extBoxes.put(ext, cb);
            extGrid.add(cb);
        }

        extGrid.revalidate();
        extGrid.repaint();

        if (!found.isEmpty()) {
            StringBuilder sb = new StringBuilder("Trovate: ");
            for (int i = 0; i < found.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(".").append(found.get(i));
            }
            setStatus(sb.toString(), C_OK);
        } else {
            setStatus("Nessun file testuale con estensione trovato.", C_MUTED);
        }
    }

    // ── Dialog sfoglia ────────────────────────────────────────────────────────
    private void chooseFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Scegli una cartella");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            addSources(Collections.singletonList(fc.getSelectedFile().getAbsolutePath()));
        }
    }

    private void chooseFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("Scegli uno o più file");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            List<String> paths = new ArrayList<String>();
            for (File file : fc.getSelectedFiles()) paths.add(file.getAbsolutePath());
            addSources(paths);
        }
    }

    // ── Estensioni ────────────────────────────────────────────────────────────
    private void setAll(boolean selected) {
        for (JCheckBox cb : extBoxes.values()) cb.setSelected(selected);
    }

    // ── Status ────────────────────────────────────────────────────────────────
    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    // ── Esecuzione merge ──────────────────────────────────────────────────────
    private void runMerge() {
        if (running) return;

        if (sources.isEmpty()) {
            setStatus("\u26A0  Scegli o trascina almeno un file o cartella.", C_ERR);
            return;
        }

        final List<String> selected = new ArrayList<String>();
        for (Map.Entry<String, JCheckBox> e : extBoxes.entrySet()) {
            if (e.getValue().isSelected()) selected.add(e.getKey());
        }
        if (selected.isEmpty()) {
            setStatus("\u26A0  Scegli almeno un tipo di file.", C_ERR);
            return;
        }

        String outName = outputField.getText().trim();
        if (outName.isEmpty()) outName = "output.txt";
        final String finalOutName = outName;

        running = true;
        runButton.setEnabled(false);
        runButton.setBackground(C_ACCH);
        runButton.setText("\u231B  Un momento\u2026");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        setStatus("Raccolta file in corso\u2026", C_MUTED);

        final List<String> srcCopy = new ArrayList<String>(sources);

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    final String path = BusinessLogic.mergeFiles(srcCopy, selected, finalOutName);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() { onSuccess(path); }
                    });
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() { onError(ex.getMessage()); }
                    });
                }
            }
        }).start();
    }

    private void onSuccess(String path) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        double sizeKb = new File(path).length() / 1024.0;
        setStatus(String.format("\u2713  Fatto! Salvato: %s  (%.1f KB)", path, sizeKb), C_OK);
        resetRunButton();
    }

    private void onError(String msg) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        setStatus("\u2717  Errore: " + msg, C_ERR);
        resetRunButton();
    }

    private void resetRunButton() {
        running = false;
        runButton.setEnabled(true);
        runButton.setBackground(C_ACCENT);
        runButton.setText("Unisci i file  \u2192");
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── Inner class: body scrollabile senza espansione orizzontale ─────────────
    private static class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
    	
        private static final long serialVersionUID = -2149605122900544407L;
		
        ScrollablePanel() { super(); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return UIScale.scale(20); }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return UIScale.scale(60); }
        @Override public boolean getScrollableTracksViewportWidth()  { return true;  }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
