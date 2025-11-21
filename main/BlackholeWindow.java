import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

public class BlackholeWindow extends JFrame {

    public BlackholeWindow() {
        setTitle("Blackhole Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new BlackholePanel());
        setLocationRelativeTo(null);
    }

    class BlackholePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = 50;

            g.setColor(Color.DARK_GRAY);
            g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            g.setColor(Color.GRAY);
            g.drawOval(centerX - 70, centerY - 70, 140, 140);
            g.drawOval(centerX - 85, centerY - 85, 170, 170);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            BlackholeWindow window = new BlackholeWindow();
            window.setVisible(true);
        });
    }
}
