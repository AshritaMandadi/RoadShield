import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║ RoadShield — Accident Management System ║
 * ║ Upgraded UI · Java Swing · Oracle JDBC ║
 * ╚══════════════════════════════════════════════════════╝
 */
public class RoadShieldGUI extends JFrame {

    // ─── Colour Palette ──────────────────────────────────────────────────────
    static final Color BG_DEEP = new Color(10, 14, 26); // midnight navy
    static final Color BG_CARD = new Color(18, 24, 42); // card surface
    static final Color BG_STRIPE = new Color(22, 30, 52); // zebra stripe
    static final Color ACCENT_CYAN = new Color(0, 210, 190); // neon cyan
    static final Color ACCENT_CORAL = new Color(255, 90, 100); // coral red
    static final Color ACCENT_GOLD = new Color(255, 200, 60); // warning gold
    static final Color ACCENT_BLUE = new Color(90, 160, 255); // soft blue
    static final Color TEXT_PRIMARY = new Color(225, 230, 245);
    static final Color TEXT_MUTED = new Color(120, 135, 165);
    static final Color DIVIDER = new Color(35, 45, 72);
    static final Color SIDEBAR_BG = new Color(14, 19, 35);

    // ─── Fonts ───────────────────────────────────────────────────────────────
    static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 16);
    static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 16);
    static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FONT_KPI = new Font("Segoe UI", Font.BOLD, 36);

    // ─── State ────────────────────────────────────────────────────────────────
    JTable dataTable;
    DefaultTableModel tableModel;
    JPanel chartContainer;
    JLabel statusLabel;
    JLabel clockLabel;
    JPanel kpiPanel;
    JLabel[] kpiValues = new JLabel[4];
    JLabel[] kpiLabels = new JLabel[4];
    JButton activeBtn = null;
    JPanel sidebarPanel;

    // ─── Entry Point ─────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.5"); // GLOBAL SCALING
        // Use system look for HW acceleration, then override everything manually
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new RoadShieldGUI());
    }

    // ─── Constructor ─────────────────────────────────────────────────────────
    public RoadShieldGUI() {
        super("RoadShield — Accident Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1600, 900);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        buildUI();
        startClock();
        setVisible(true);

        // Auto-load KPIs after a short delay so window paints first
        Timer t = new Timer(400, e -> {
            loadKPIs();
            ((Timer) e.getSource()).stop();
        });
        t.start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ══════════════════════════════════════════════════════════════════════════
    void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ─── Top Bar ─────────────────────────────────────────────────────────────
    JPanel buildTopBar() {
        JPanel bar = new GradientPanel(BG_CARD, new Color(14, 20, 38), false);
        bar.setLayout(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));

        // Logo + Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        icon.setVerticalAlignment(SwingConstants.CENTER);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel appName = new JLabel("RoadShield");
        appName.setFont(FONT_TITLE);
        appName.setForeground(ACCENT_CYAN);

        JLabel appSub = new JLabel("Accident Intelligence System");
        appSub.setFont(FONT_SMALL);
        appSub.setForeground(TEXT_MUTED);

        titleStack.add(Box.createVerticalStrut(10));
        titleStack.add(appName);
        titleStack.add(appSub);

        left.add(icon);
        left.add(titleStack);

        // Right side: clock + connection badge
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel dbBadge = pillLabel("● ORACLE  XEPDB1", ACCENT_CYAN, new Color(0, 210, 190, 25));
        clockLabel = new JLabel();
        clockLabel.setFont(FONT_MONO);
        clockLabel.setForeground(TEXT_MUTED);

        right.add(dbBadge);
        right.add(clockLabel);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // ─── Sidebar ─────────────────────────────────────────────────────────────
    JPanel buildSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setBorder(new MatteBorder(0, 0, 0, 1, DIVIDER));

        sidebarPanel.add(Box.createVerticalStrut(16));
        sidebarPanel.add(sidebarSection("NAVIGATION"));
        sidebarPanel.add(Box.createVerticalStrut(4));

        // Nav buttons
        String[][] navItems = {
                { "", "Dashboard", "loadKPIs" },
                { "", "Drivers", "loadDrivers" },
                { "", "Accidents", "showAccidents" },
                { "", "Vehicles", "loadVehicles" },
                { "", "Injuries", "loadInjuries" },
                { "", "Locations", "loadLocations" },
        };

        for (String[] item : navItems) {
            JButton btn = sidebarButton(item[0] + "  " + item[1], item[2]);
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createVerticalStrut(2));
        }

        sidebarPanel.add(Box.createVerticalStrut(16));
        sidebarPanel.add(sidebarSection("ANALYTICS"));
        sidebarPanel.add(Box.createVerticalStrut(4));

        String[][] chartItems = {
                { "", "Severity Chart", "showSeverityChart" },
                { "", "Trend Chart", "showTrendChart" },
                { "", "Type Breakdown", "showTypeChart" },
        };

        for (String[] item : chartItems) {
            JButton btn = sidebarButton(item[0] + "  " + item[1], item[2]);
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createVerticalStrut(2));
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Bottom: version
        JLabel ver = new JLabel("v2.0  •  RoadShield");
        ver.setFont(FONT_SMALL);
        ver.setForeground(TEXT_MUTED);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(ver);
        sidebarPanel.add(Box.createVerticalStrut(12));

        return sidebarPanel;
    }

    JLabel sidebarSection(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        lbl.setMaximumSize(new Dimension(200, 22));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    JButton sidebarButton(String label, String action) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeBtn) {
                    g2.setColor(new Color(0, 210, 190, 35));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(ACCENT_CYAN);
                    g2.fillRoundRect(0, (getHeight() - 20) / 2, 3, 20, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(this == null || activeBtn != btn ? TEXT_PRIMARY : ACCENT_CYAN);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(260, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            activeBtn = btn;
            // update text colors
            for (Component c : sidebarPanel.getComponents()) {
                if (c instanceof JButton) {
                    ((JButton) c).setForeground(c == btn ? ACCENT_CYAN : TEXT_PRIMARY);
                    c.repaint();
                }
            }
            dispatchAction(action);
        });

        return btn;
    }

    void dispatchAction(String action) {
        switch (action) {
            case "loadKPIs": loadKPIs(); break;
            case "loadDrivers": loadDrivers(); break;
            case "showAccidents": showAccidents(); break;
            case "loadVehicles": loadVehicles(); break;
            case "loadInjuries": loadInjuries(); break;
            case "loadLocations": loadLocations(); break;
            case "showSeverityChart": showSeverityChart(); break;
            case "showTrendChart": showTrendChart(); break;
            case "showTypeChart": showTypeChart(); break;
        }
    }

    // ─── Main Area ───────────────────────────────────────────────────────────
    JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(BG_DEEP);

        // KPI strip at top
        kpiPanel = buildKPIStrip();
        main.add(kpiPanel, BorderLayout.NORTH);

        // Content: table + chart side by side
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildChartPanel());
        split.setDividerLocation(520);
        split.setDividerSize(6);
        split.setBackground(DIVIDER);
        split.setBorder(null);
        split.setContinuousLayout(true);

        main.add(split, BorderLayout.CENTER);
        return main;
    }

    // ─── KPI Strip ───────────────────────────────────────────────────────────
    JPanel buildKPIStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 1, 0));
        strip.setBackground(DIVIDER);
        strip.setPreferredSize(new Dimension(0, 120));

        String[] titles = { "Total Drivers", "Total Accidents", "Avg Severity", "Active Vehicles" };
        Color[] colors = { ACCENT_CYAN, ACCENT_CORAL, ACCENT_GOLD, ACCENT_BLUE };
        String[] icons = { "", "", "", "" };

        for (int i = 0; i < 4; i++) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(BG_CARD);
            card.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel ico = new JLabel(icons[i]);
            ico.setFont(new Font("Segoe UI", Font.PLAIN, 20));

            kpiValues[i] = new JLabel("—");
            kpiValues[i].setFont(FONT_KPI);
            kpiValues[i].setForeground(colors[i]);

            kpiLabels[i] = new JLabel(titles[i]);
            kpiLabels[i].setFont(FONT_SMALL);
            kpiLabels[i].setForeground(TEXT_MUTED);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            right.add(kpiValues[i]);
            right.add(kpiLabels[i]);

            card.add(ico, BorderLayout.WEST);
            card.add(right, BorderLayout.CENTER);

            // Thin bottom-accent line
            card.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 3, 0, colors[i]),
                    new EmptyBorder(12, 18, 9, 18)));

            strip.add(card);
        }
        return strip;
    }

    // ─── Table Panel ─────────────────────────────────────────────────────────
    JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Data Explorer");
        title.setFont(FONT_HEADING);
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Select a category from the sidebar");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_MUTED);

        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(title);
        stack.add(sub);

        header.add(stack, BorderLayout.WEST);

        // Refresh button
        JButton refresh = iconButton("↻", ACCENT_CYAN);
        refresh.setToolTipText("Refresh current view");
        header.add(refresh, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        dataTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_STRIPE);
                } else {
                    c.setBackground(new Color(0, 210, 190, 60));
                }
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        };
        styleTable(dataTable);

        JScrollPane scroll = new JScrollPane(dataTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBackground(BG_CARD);

        // Scrollbar style
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new DarkScrollBarUI());

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(sep, BorderLayout.CENTER); // will be replaced
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setGridColor(DIVIDER);
        t.setRowHeight(40);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        t.setSelectionBackground(new Color(0, 210, 190, 55));
        t.setSelectionForeground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = t.getTableHeader();
        header.setBackground(new Color(20, 28, 50));
        header.setForeground(ACCENT_CYAN);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
    }

    // ─── Chart Panel ─────────────────────────────────────────────────────────
    JPanel buildChartPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CARD);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Analytics");
        title.setFont(FONT_HEADING);
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(BG_CARD);

        // Placeholder
        JLabel ph = new JLabel("Select an analytics view from the sidebar", JLabel.CENTER);
        ph.setForeground(TEXT_MUTED);
        ph.setFont(FONT_BODY);
        chartContainer.add(ph, BorderLayout.CENTER);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(chartContainer, BorderLayout.CENTER);

        return wrapper;
    }

    // ─── Status Bar ──────────────────────────────────────────────────────────
    JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(8, 12, 22));
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, DIVIDER));

        statusLabel = new JLabel("  ● Ready  |  Connected to Oracle XEPDB1");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ACCENT_CYAN);

        JLabel credits = new JLabel("RoadShield v2.0  ");
        credits.setFont(FONT_SMALL);
        credits.setForeground(TEXT_MUTED);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(credits, BorderLayout.EAST);

        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DATA METHODS
    // ══════════════════════════════════════════════════════════════════════════

    void loadKPIs() {
        setStatus("Loading dashboard KPIs…");
        SwingWorker<int[], Void> w = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                try (Connection c = getConnection()) {
                    int[] vals = new int[4];
                    vals[0] = scalar(c, "SELECT COUNT(*) FROM DRIVER");
                    vals[1] = scalar(c, "SELECT COUNT(*) FROM ACCIDENT");
                    vals[2] = 0; // placeholder — adapt to your schema
                    vals[3] = scalar(c, "SELECT COUNT(DISTINCT V_ID) FROM VEHICLE");
                    return vals;
                }
            }

            @Override
            protected void done() {
                try {
                    int[] v = get();
                    kpiValues[0].setText(String.valueOf(v[0]));
                    kpiValues[1].setText(String.valueOf(v[1]));
                    kpiValues[2].setText("—");
                    kpiValues[3].setText(String.valueOf(v[3]));
                    setStatus("Dashboard loaded.");
                } catch (Exception ex) {
                    setStatus("Error loading KPIs: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    void loadDrivers() {
        runQuery(
                "SELECT DRIVER_ID, NAME, LICENSE_NO, CONTACT FROM DRIVER ORDER BY DRIVER_ID",
                new String[] { "Driver ID", "Name", "License No.", "Contact" });
    }

    void showAccidents() {
        runQuery(
                "SELECT A_ID, ACCIDENT_DATE, LOCATION, SEVERITY, DESCRIPTION FROM ACCIDENT ORDER BY A_ID",
                new String[] { "Acc. ID", "Date", "Location", "Severity", "Description" });
    }

    void loadVehicles() {
        runQuery(
                "SELECT V_ID, REG_NUMBER, MODEL, MAKE, YEAR FROM VEHICLE ORDER BY V_ID",
                new String[] { "Vehicle ID", "Reg. Number", "Model", "Make", "Year" });
    }

    void loadInjuries() {
        runQuery(
                "SELECT I_ID, A_ID, PERSON_NAME, INJURY_TYPE, HOSPITALIZED FROM INJURY ORDER BY I_ID",
                new String[] { "Injury ID", "Acc. ID", "Person", "Injury Type", "Hospitalized" });
    }

    void loadLocations() {
        runQuery(
                "SELECT L_ID, ROAD_NAME, CITY, STATE, PINCODE FROM LOCATION ORDER BY L_ID",
                new String[] { "Loc. ID", "Road Name", "City", "State", "Pincode" });
    }

    // Generic table loader
    void runQuery(String sql, String[] columns) {
        setStatus("Loading data…");
        SwingWorker<Object[][], Void> w = new SwingWorker<Object[][], Void>() {
            @Override
            protected Object[][] doInBackground() throws Exception {
                try (Connection con = getConnection();
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(sql)) {

                    java.util.List<Object[]> rows = new java.util.ArrayList<>();
                    int cols = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Object[] row = new Object[cols];
                        for (int i = 0; i < cols; i++)
                            row[i] = rs.getObject(i + 1);
                        rows.add(row);
                    }
                    return rows.toArray(new Object[0][]);
                }
            }

            @Override
            protected void done() {
                try {
                    Object[][] rows = get();
                    tableModel.setColumnIdentifiers(columns);
                    tableModel.setRowCount(0);
                    for (Object[] row : rows)
                        tableModel.addRow(row);
                    setStatus("Loaded " + rows.length + " records.");
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage());
                    showError(ex);
                }
            }
        };
        w.execute();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CHART METHODS
    // ══════════════════════════════════════════════════════════════════════════

    void showSeverityChart() {
        setStatus("Building severity chart…");
        SwingWorker<JFreeChart, Void> w = new SwingWorker<JFreeChart, Void>() {
            @Override
            protected JFreeChart doInBackground() throws Exception {
                try (Connection con = getConnection();
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(
                                "SELECT SEVERITY, COUNT(*) FROM ACCIDENT GROUP BY SEVERITY ORDER BY SEVERITY")) {

                    DefaultCategoryDataset ds = new DefaultCategoryDataset();
                    while (rs.next())
                        ds.setValue(rs.getInt(2), "Accidents", rs.getString(1));

                    JFreeChart chart = ChartFactory.createBarChart(
                            null, "Severity", "Count", ds,
                            PlotOrientation.VERTICAL, false, true, false);

                    applyDarkTheme(chart);
                    BarRenderer br = (BarRenderer) chart.getCategoryPlot().getRenderer();
                    br.setSeriesPaint(0, ACCENT_CORAL);
                    br.setMaximumBarWidth(0.2);
                    br.setShadowVisible(false);
                    return chart;
                }
            }

            @Override
            protected void done() {
                try {
                    putChart(get(), "Accidents by Severity");
                    setStatus("Severity chart loaded.");
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage());
                    showError(ex);
                }
            }
        };
        w.execute();
    }

    void showTrendChart() {
        setStatus("Building trend chart…");
        SwingWorker<JFreeChart, Void> w = new SwingWorker<JFreeChart, Void>() {
            @Override
            protected JFreeChart doInBackground() throws Exception {
                try (Connection con = getConnection();
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(
                                "SELECT TO_CHAR(ACCIDENT_DATE,'MON-YYYY') MON, COUNT(*) " +
                                        "FROM ACCIDENT GROUP BY TO_CHAR(ACCIDENT_DATE,'MON-YYYY') " +
                                        "ORDER BY MIN(ACCIDENT_DATE)")) {

                    DefaultCategoryDataset ds = new DefaultCategoryDataset();
                    while (rs.next())
                        ds.setValue(rs.getInt(2), "Incidents", rs.getString(1));

                    JFreeChart chart = ChartFactory.createLineChart(
                            null, "Month", "Count", ds,
                            PlotOrientation.VERTICAL, false, true, false);

                    applyDarkTheme(chart);
                    LineAndShapeRenderer lr = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();
                    lr.setSeriesPaint(0, ACCENT_CYAN);
                    lr.setSeriesStroke(0, new BasicStroke(2.5f));
                    lr.setSeriesShapesVisible(0, true);
                    return chart;
                }
            }

            @Override
            protected void done() {
                try {
                    putChart(get(), "Monthly Accident Trend");
                    setStatus("Trend chart loaded.");
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage());
                    showError(ex);
                }
            }
        };
        w.execute();
    }

    void showTypeChart() {
        setStatus("Building type breakdown…");
        SwingWorker<JFreeChart, Void> w = new SwingWorker<JFreeChart, Void>() {
            @Override
            protected JFreeChart doInBackground() throws Exception {
                try (Connection con = getConnection();
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(
                                "SELECT ACCIDENT_TYPE, COUNT(*) FROM ACCIDENT GROUP BY ACCIDENT_TYPE")) {

                    DefaultPieDataset<String> ds = new DefaultPieDataset<>();
                    while (rs.next())
                        ds.setValue(rs.getString(1), rs.getInt(2));

                    JFreeChart chart = ChartFactory.createPieChart(null, ds, true, true, false);

                    chart.setBackgroundPaint(BG_CARD);
                    chart.setBorderVisible(false);
                    PiePlot<?> pp = (PiePlot<?>) chart.getPlot();
                    pp.setBackgroundPaint(BG_CARD);
                    pp.setOutlineVisible(false);
                    pp.setLabelBackgroundPaint(BG_CARD);
                    pp.setLabelOutlinePaint(null);
                    pp.setLabelShadowPaint(null);
                    pp.setLabelPaint(TEXT_PRIMARY);
                    pp.setLabelFont(FONT_SMALL);

                    Color[] pieColors = { ACCENT_CYAN, ACCENT_CORAL, ACCENT_GOLD, ACCENT_BLUE,
                            new Color(170, 100, 255), new Color(255, 160, 80) };
                    int idx = 0;
                    for (Object key : ds.getKeys()) {
                        pp.setSectionPaint((String) key, pieColors[idx++ % pieColors.length]);
                    }

                    if (chart.getLegend() != null) {
                        chart.getLegend().setBackgroundPaint(BG_CARD);
                        chart.getLegend().setItemPaint(TEXT_PRIMARY);
                        chart.getLegend().setItemFont(FONT_SMALL);
                    }

                    return chart;
                }
            }

            @Override
            protected void done() {
                try {
                    putChart(get(), "Accidents by Type");
                    setStatus("Type chart loaded.");
                } catch (Exception ex) {
                    setStatus("Error: " + ex.getMessage());
                    showError(ex);
                }
            }
        };
        w.execute();
    }

    void putChart(JFreeChart chart, String heading) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(BG_CARD);
        cp.setBorder(null);
        cp.setPopupMenu(null);

        chartContainer.removeAll();
        chartContainer.add(cp, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    void applyDarkTheme(JFreeChart chart) {
        chart.setBackgroundPaint(BG_CARD);
        chart.setBorderVisible(false);
        if (chart.getTitle() != null)
            chart.getTitle().setPaint(TEXT_PRIMARY);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(22, 30, 52));
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(DIVIDER);
        plot.setRangeGridlineStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1, new float[] { 4, 4 }, 0));

        CategoryAxis domAxis = plot.getDomainAxis();
        domAxis.setLabelPaint(TEXT_MUTED);
        domAxis.setTickLabelPaint(TEXT_PRIMARY);
        domAxis.setTickLabelFont(FONT_SMALL);
        domAxis.setAxisLinePaint(DIVIDER);
        domAxis.setTickMarkPaint(DIVIDER);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelPaint(TEXT_MUTED);
        rangeAxis.setTickLabelPaint(TEXT_PRIMARY);
        rangeAxis.setTickLabelFont(FONT_SMALL);
        rangeAxis.setAxisLinePaint(DIVIDER);
        rangeAxis.setTickMarkPaint(DIVIDER);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(BG_CARD);
            chart.getLegend().setItemPaint(TEXT_PRIMARY);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@//localhost:1521/XEPDB1",
                "ASHRITA", "ash123");
    }

    int scalar(Connection c, String sql) throws Exception {
        try (Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("  ● " + msg));
    }

    void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    JLabel pillLabel(String text, Color fg, Color bg) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setFont(new Font("Consolas", Font.BOLD, 11));
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    JButton iconButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void startClock() {
        Timer t = new Timer(1000, e -> {
            String ts = new SimpleDateFormat("EEE  dd-MMM-yyyy  HH:mm:ss").format(new Date());
            clockLabel.setText(ts + "   ");
        });
        t.setInitialDelay(0);
        t.start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INNER HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Simple gradient panel */
    static class GradientPanel extends JPanel {
        Color c1, c2;
        boolean horizontal;

        GradientPanel(Color c1, Color c2, boolean horizontal) {
            this.c1 = c1;
            this.c2 = c2;
            this.horizontal = horizontal;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gp = horizontal
                    ? new GradientPaint(0, 0, c1, getWidth(), 0, c2)
                    : new GradientPaint(0, 0, c1, 0, getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** Dark scrollbar UI */
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(55, 65, 95);
            trackColor = new Color(18, 24, 42);
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return zeroButton();
        }

        JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}