import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

/**
 *  2D Black Hole Simulator
 */
public class BlackHoleSimulator extends JPanel implements ActionListener {

    // --- CONFIGURATION CONSTANTS ---
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    
    // Physics: Stronger gravity makes the effect more obvious
    private static final double GRAVITY = 600.0;      
    private static final double EVENT_HORIZON = 40.0; 
    
    // Friction: 0.998 means they keep 99.8% of their speed (lose 0.2% per frame)
    private static final double FRICTION = 0.998;     
    
    // Trails enabled
    private static final boolean ENABLE_TRAILS = true; 

    // --- VARIABLES ---
    private ArrayList<Particle> particles;
    private Timer loop;
    private Random rand;
    private final Point center; 

    public  BlackHoleSimulator() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        particles = new ArrayList<>();
        rand = new Random();
        center = new Point(WIDTH / 2, HEIGHT / 2); 

        spawnGalaxy(); // Create the initial ring

        // Controls
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    spawnGalaxy(); 
                } else {
                    spawnCluster(e.getPoint()); 
                }
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) spawnGalaxy(); // Changed logic to respawn on space
                if(e.getKeyCode() == KeyEvent.VK_R) particles.clear(); // Clear on R
            }
        });

        loop = new Timer(16, this);
        loop.start();
    }

    /**
     * Creates the ring of particles.
     */
    private void spawnGalaxy() {
        particles.clear();
        double minDist = EVENT_HORIZON * 2.5;
        double maxDist = 350; 

        for (int i = 0; i < 1200; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double distance = minDist + (rand.nextDouble() * (maxDist - minDist));
            
            double x = center.x + Math.cos(angle) * distance;
            double y = center.y + Math.sin(angle) * distance;
            
            double speed = Math.sqrt(GRAVITY / distance);
            
            // Perpendicular velocity for orbit
            double vx = -Math.sin(angle) * speed;
            double vy = Math.cos(angle) * speed;

            particles.add(new Particle(x, y, vx, vy));
        }
    }

    private void spawnCluster(Point start) {
        for (int i = 0; i < 50; i++) { 
            double x = start.x + (rand.nextDouble() * 20 - 10);
            double y = start.y + (rand.nextDouble() * 20 - 10);
            
            double dx = center.x - x;
            double dy = center.y - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            double speed = Math.sqrt(GRAVITY / dist);

            double vx = (-dy/dist) * speed * 0.8; 
            double vy = (dx/dist) * speed * 0.8;

            particles.add(new Particle(x, y, vx, vy));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updatePhysics();
        repaint();
    }

    private void updatePhysics() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            
            double dx = center.x - p.x;
            double dy = center.y - p.y;
            double distance = Math.sqrt(dx*dx + dy*dy);

            // --- EVENT HORIZON (Delete if too close) ---
            if (distance < EVENT_HORIZON) {
                it.remove(); 
                continue;
            }

            double dirX = dx / distance;
            double dirY = dy / distance;

            double safeDist = Math.max(distance, 10.0);
            double force = GRAVITY / (safeDist * safeDist);

            p.vx += dirX * force;
            p.vy += dirY * force;

            p.vx *= FRICTION;
            p.vy *= FRICTION;

            p.x += p.vx;
            p.y += p.vy;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Background (Transparent for trails)
        if (ENABLE_TRAILS) {
            g2.setColor(new Color(0, 0, 0, 40)); 
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw Particles
        for (Particle p : particles) {
            // Calculate distance for color logic
            double dist = Math.sqrt(Math.pow(center.x - p.x, 2) + Math.pow(center.y - p.y, 2));
            
            // Color Logic
            if (dist < 100) g2.setColor(Color.RED);
            else if (dist < 250) g2.setColor(Color.ORANGE);
            else g2.setColor(Color.CYAN);

            g2.fillOval((int)p.x, (int)p.y, 3, 3);
        }

        // Draw Black Hole
        // 1. Glow
        g2.setColor(new Color(255, 255, 255, 30));
        double glowSize = EVENT_HORIZON * 2.2;
        g2.fillOval((int)(center.x - glowSize/2), (int)(center.y - glowSize/2), (int)glowSize, (int)glowSize);

        // 2. The Hole
        g2.setColor(Color.BLACK);
        double bhSize = EVENT_HORIZON * 2;
        g2.fillOval((int)(center.x - bhSize/2), (int)(center.y - bhSize/2), (int)bhSize, (int)bhSize);
        
        // 3. Outline
        g2.setColor(new Color(100, 100, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval((int)(center.x - bhSize/2), (int)(center.y - bhSize/2), (int)bhSize, (int)bhSize);

        // UI
        g2.setColor(Color.WHITE);
        g2.drawString("Particles: " + particles.size(), 10, 20);
        g2.drawString("[SpaceBar] Spawn Galaxy | [R] Clear | [Left Click] Spawn Cluster", 10, 35);
    }

    static class Particle {
        double x, y;   // Position
        double vx, vy; // Velocity

        public Particle(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
    // ------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Black Hole Simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new BlackHoleSimulator());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}