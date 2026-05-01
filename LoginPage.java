import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame {

    JTextField user;
    JPasswordField pass;

    public LoginPage() {

        setTitle("RoadShield Login");
        setSize(350,250);
        setLayout(null);
        getContentPane().setBackground(new Color(28,36,58));

        JLabel title = new JLabel("Admin Login", JLabel.CENTER);
        title.setBounds(80,20,200,30);
        title.setForeground(new Color(0,220,255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel u = new JLabel("Username:");
        u.setBounds(40,80,100,25);
        u.setForeground(Color.WHITE);

        JLabel p = new JLabel("Password:");
        p.setBounds(40,120,100,25);
        p.setForeground(Color.WHITE);

        user = new JTextField();
        user.setBounds(140,80,150,25);

        pass = new JPasswordField();
        pass.setBounds(140,120,150,25);

        JButton login = new JButton("Login");
        login.setBounds(120,170,100,30);

        add(title); add(u); add(p);
        add(user); add(pass); add(login);

        login.addActionListener(e -> {
            if(user.getText().equals("admin") &&
               new String(pass.getPassword()).equals("admin123")) {
                new RoadShieldGUI();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,"Invalid Login");
            }
        });

        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
