import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class RoadShieldGUI extends JFrame {

    JTable table;
    DefaultTableModel model;
    JPanel chartPanel;

    public RoadShieldGUI() {

        setTitle("🚗 RoadShield Dashboard");
        setSize(950, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Color bg = new Color(30,30,30);
        Color panel = new Color(45,45,45);
        Color accent = new Color(0,150,136);

        getContentPane().setBackground(bg);

        // 🔹 Title
        JLabel title = new JLabel("RoadShield System", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setOpaque(true);
        title.setBackground(accent);
        title.setPreferredSize(new Dimension(100,40));
        add(title, BorderLayout.NORTH);

        // 🔹 Table
        model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Data");

        table = new JTable(model);
        table.setBackground(panel);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);
        table.setRowHeight(25);

        JScrollPane tableScroll = new JScrollPane(table);

        // 🔹 Chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(panel);

        // 🔹 Split layout
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, chartPanel);
        split.setDividerLocation(450);
        add(split, BorderLayout.CENTER);

        // 🔹 Buttons
        JPanel bottom = new JPanel();
        bottom.setBackground(bg);

        JButton load = createButton("Load Drivers");
        JButton accidents = createButton("Show Accidents");
        JButton chart = createButton("Show Chart");

        bottom.add(load);
        bottom.add(accidents);
        bottom.add(chart);

        add(bottom, BorderLayout.SOUTH);

        // 🔹 Actions
        load.addActionListener(e -> loadDrivers());
        accidents.addActionListener(e -> showAccidents());
        chart.addActionListener(e -> showChart());

        setVisible(true);
    }

    // 🔹 Button Styling (FIXED)
    JButton createButton(String text){
        JButton btn = new JButton(text);

        btn.setBackground(new Color(0,150,136)); // teal
        btn.setForeground(Color.WHITE); // ✅ visible text
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        return btn;
    }

    // 🔹 Load Drivers
    void loadDrivers() {
        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT DRIVER_ID, NAME FROM DRIVER");

            model.setColumnIdentifiers(new String[]{"Driver ID","Name"});
            model.setRowCount(0);

            while(rs.next()){
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,e);
        }
    }

    // 🔹 Show Accidents
    void showAccidents() {
        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT A_ID, SEVERITY FROM ACCIDENT");

            model.setColumnIdentifiers(new String[]{"Accident ID","Severity"});
            model.setRowCount(0);

            while(rs.next()){
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,e);
        }
    }

    // 🔹 Chart (FULL FIX)
    void showChart() {
        try {
            Connection con = getConnection();
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(
                "SELECT SEVERITY, COUNT(*) FROM ACCIDENT GROUP BY SEVERITY");

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            while(rs.next()){
                dataset.setValue(rs.getInt(2), "Accidents", rs.getString(1));
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Accident Severity Analysis",
                "Severity",
                "Count",
                dataset
            );

            // 🎨 Dark Theme Fix
            chart.setBackgroundPaint(new Color(30,30,30));

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(50,50,50));
            plot.setRangeGridlinePaint(Color.WHITE);

            plot.getDomainAxis().setLabelPaint(Color.WHITE);
            plot.getDomainAxis().setTickLabelPaint(Color.WHITE);

            plot.getRangeAxis().setLabelPaint(Color.WHITE);
            plot.getRangeAxis().setTickLabelPaint(Color.WHITE);

            chart.getTitle().setPaint(Color.WHITE);

            plot.getRenderer().setSeriesPaint(0, new Color(255, 99, 132));

            ChartPanel cp = new ChartPanel(chart);

            chartPanel.removeAll();
            chartPanel.add(cp, BorderLayout.CENTER);
            chartPanel.revalidate();

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,e);
        }
    }

    // 🔹 DB Connection
    Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(
            "jdbc:oracle:thin:@localhost:1521/XEPDB1",
            "ASHRITA",
            "ash123"
        );
    }

    public static void main(String[] args) {
        new RoadShieldGUI();
    }
}
