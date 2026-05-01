import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class RoadShieldGUI extends JFrame {

    JTable table;
    DefaultTableModel model;
    JPanel chartPanel;

    ArrayList<String[]> drivers = new ArrayList<>();
    ArrayList<String[]> accidents = new ArrayList<>();
    ArrayList<String[]> vehicles = new ArrayList<>();

    JLabel dCount, aCount, sCount, vCount;

    public RoadShieldGUI() {

        setTitle("RoadShield Dashboard");
        setSize(1300,750);
        setLayout(new BorderLayout());

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220,700));
        sidebar.setBackground(new Color(22,30,55));
        sidebar.setLayout(new GridLayout(14,1,8,8));

        JLabel logo = new JLabel("RoadShield", JLabel.CENTER);
        logo.setForeground(new Color(0,230,255));
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        sidebar.add(logo);

        sidebar.add(menuItem("Drivers", () -> showDrivers()));
        sidebar.add(menuItem("Add Driver", () -> addDriver()));
        sidebar.add(menuItem("Update Driver", () -> updateDriver()));
        sidebar.add(menuItem("Delete Driver", () -> deleteDriver()));

        sidebar.add(menuItem("Vehicles", () -> showVehicles()));
        sidebar.add(menuItem("Add Vehicle", () -> addVehicle()));

        sidebar.add(menuItem("Accidents", () -> showAccidents()));
        sidebar.add(menuItem("Add Accident", () -> addAccident()));

        // ⭐ NEW BUTTON
        sidebar.add(menuItem("DB Schema", () -> showSchema()));

        add(sidebar, BorderLayout.WEST);

        // ===== DASHBOARD =====
        JPanel top = new JPanel(new GridLayout(1,4,10,10));
        top.setBackground(new Color(30,45,75));

        dCount = createCard();
        aCount = createCard();
        sCount = createCard();
        vCount = createCard();

        top.add(cardPanel("Total Drivers", dCount));
        top.add(cardPanel("Total Accidents", aCount));
        top.add(cardPanel("Severity", sCount));
        top.add(cardPanel("Vehicles", vCount));

        add(top, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(table);

        // ===== CHART =====
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(new Color(30,45,75));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, chartPanel);
        split.setDividerLocation(750);

        add(split, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===== SIDEBAR ITEM =====
    JLabel menuItem(String text, Runnable action) {
        JLabel lbl = new JLabel("   " + text);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(35,50,85));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.run(); }
            public void mouseEntered(MouseEvent e) { lbl.setBackground(new Color(0,160,220)); }
            public void mouseExited(MouseEvent e) { lbl.setBackground(new Color(35,50,85)); }
        });

        return lbl;
    }

    JLabel createCard() {
        JLabel l = new JLabel("0", JLabel.CENTER);
        l.setForeground(Color.CYAN);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        return l;
    }

    JPanel cardPanel(String title, JLabel val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20,30,60));
        p.setBorder(BorderFactory.createLineBorder(Color.CYAN));

        JLabel t = new JLabel(title);
        t.setForeground(Color.LIGHT_GRAY);

        p.add(t, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);

        return p;
    }

    void updateDashboard() {
        dCount.setText(String.valueOf(drivers.size()));
        aCount.setText(String.valueOf(accidents.size()));
        vCount.setText(String.valueOf(vehicles.size()));
        sCount.setText(accidents.size()>0 ? "High" : "0");
        updateChart();
    }

    // ===== DRIVERS =====
    void showDrivers() {
        model.setDataVector(new Object[][]{}, new String[]{"ID","Name","License","Age"});
        for(String[] d: drivers) model.addRow(d);
    }

    void addDriver() {
        JTextField id=new JTextField(), name=new JTextField(), lic=new JTextField(), age=new JTextField();
        Object[] f={"ID",id,"Name",name,"License",lic,"Age",age};

        if(JOptionPane.showConfirmDialog(this,f,"Add Driver",0)==0){
            drivers.add(new String[]{id.getText(),name.getText(),lic.getText(),age.getText()});
            showDrivers(); updateDashboard();
        }
    }

    void updateDriver() {
        int r=table.getSelectedRow();
        if(r==-1){ JOptionPane.showMessageDialog(this,"Select row"); return; }

        drivers.get(r)[1]=JOptionPane.showInputDialog("Name");
        drivers.get(r)[3]=JOptionPane.showInputDialog("Age");
        showDrivers();
    }

    void deleteDriver() {
        int r=table.getSelectedRow();
        if(r==-1){ JOptionPane.showMessageDialog(this,"Select row"); return; }

        drivers.remove(r);
        showDrivers(); updateDashboard();
    }

    // ===== VEHICLES =====
    void showVehicles() {
        model.setDataVector(new Object[][]{}, new String[]{"Vehicle ID","Driver ID"});
        for(String[] v: vehicles) model.addRow(v);
    }

    void addVehicle() {
        JTextField vid=new JTextField(), did=new JTextField();
        Object[] f={"Vehicle ID",vid,"Driver ID",did};

        if(JOptionPane.showConfirmDialog(this,f,"Add Vehicle",0)==0){
            vehicles.add(new String[]{vid.getText(),did.getText()});
            showVehicles(); updateDashboard();
        }
    }

    // ===== ACCIDENTS =====
    void showAccidents() {
        model.setDataVector(new Object[][]{}, new String[]{"ID","Location","Severity","Injuries"});
        for(String[] a: accidents) model.addRow(a);
    }

    void addAccident() {
        JTextField id=new JTextField(), loc=new JTextField(), sev=new JTextField(), inj=new JTextField();
        Object[] f={"ID",id,"Location",loc,"Severity",sev,"Injuries",inj};

        if(JOptionPane.showConfirmDialog(this,f,"Add Accident",0)==0){
            accidents.add(new String[]{id.getText(),loc.getText(),sev.getText(),inj.getText()});
            showAccidents(); updateDashboard();
        }
    }

    // ===== CHART =====
    void updateChart() {
        DefaultCategoryDataset d=new DefaultCategoryDataset();
        int minor=0,major=0,critical=0;

        for(String[] a:accidents){
            if(a[2].equalsIgnoreCase("Minor")) minor++;
            else if(a[2].equalsIgnoreCase("Major")) major++;
            else critical++;
        }

        d.addValue(minor,"Accidents","Minor");
        d.addValue(major,"Accidents","Major");
        d.addValue(critical,"Accidents","Critical");

        JFreeChart chart=ChartFactory.createBarChart("Severity","Type","Count",d);

        CategoryPlot p=chart.getCategoryPlot();
        p.setBackgroundPaint(new Color(30,45,75));

        BarRenderer r=(BarRenderer)p.getRenderer();
        r.setSeriesPaint(0,new Color(0,200,255));

        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart),BorderLayout.CENTER);
        chartPanel.revalidate();
    }

    // ===== SCHEMA VIEW =====
    void showSchema() {

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setBackground(new Color(20,30,60));
        area.setForeground(Color.WHITE);

        area.setText(
            "===== DATABASE SCHEMA (3NF) =====\n\n" +
            "DRIVER(driver_id, name, license_no, age)\n" +
            "VEHICLE(vehicle_id, driver_id)\n" +
            "ACCIDENT(accident_id, location, severity)\n\n" +

            "===== RELATIONSHIPS =====\n" +
            "Driver 1 --- N Vehicle\n" +
            "Driver M --- N Accident\n\n" +

            "===== FUNCTIONAL DEPENDENCIES =====\n" +
            "driver_id -> name, license_no, age\n" +
            "vehicle_id -> driver_id\n" +
            "accident_id -> location, severity\n\n" +

            "===== NORMALIZATION =====\n" +
            "1NF: Atomic values\n" +
            "2NF: No partial dependency\n" +
            "3NF: No transitive dependency\n"
        );

        JFrame f = new JFrame("Schema View");
        f.setSize(600,500);
        f.add(new JScrollPane(area));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
