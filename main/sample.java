import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Advanced 2D Black Hole Simulator
 * IMPORTANT: Save this file as "BlackHoleSimulation.java"
 */
public class BlackHoleSimulation extends JPanel implements ActionListener {

    // --- CONFIGURATION CONSTANTS ---
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    
    // Physics: Reduced from 2000.0 to 600.0 to slow down the movement
    private static final double GRAVITY = 2000.0;       
    private static final double EVENT_HORIZON = 40.0; 
    
    // Friction: 0.998 means they keep 99.8% of their speed (lose 0.2% per frame)
    // This small loss is what causes the spiral orbit.
    private static final double FRICTION = 0.998;     
    
    // Trails enabled
    private static final boolean ENABLE_TRAILS = true; 

    // --- VARIABLES ---
    private ArrayList<Particle> particles;
    private Timer loop;
    private Random rand;
    private final Point center; 

    public BlackHoleSimulation() {
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
                if(e.getKeyCode() == KeyEvent.VK_SPACE) particles.clear();
                if(e.getKeyCode() == KeyEvent.VK_R) spawnGalaxy();
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
        // Increased maxDist from 350 to 450 to spread particles out across the screen
        double maxDist = 450; 

        // Increased particle count from 1200 to 3500
        for (int i = 0; i < 5000; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double distance = minDist + (rand.nextDouble() * (maxDist - minDist));
            
            double x = center.x + Math.cos(angle) * distance;
            double y = center.y + Math.sin(angle) * distance;
            
            double speed = Math.sqrt(GRAVITY / distance);
            double vx = -Math.sin(angle) * speed;
            double vy = Math.cos(angle) * speed;

            particles.add(new Particle(x, y, vx, vy));
        }
    }

    private void spawnCluster(Point start) {
        for (int i = 0; i < 50; i++) { 
            // Increased spread from 20 to 80 so clicked particles aren't so clumped
            double x = start.x + (rand.nextDouble() * 80 - 40);
            double y = start.y + (rand.nextDouble() * 80 - 40);
            
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

        // Draw Particles using the logic from your provided code
        for (Particle p : particles) {
            // Calculate distance for color logic
            double dist = Math.sqrt(Math.pow(center.x - p.x, 2) + Math.pow(center.y - p.y, 2));
            
            // Color Logic copied from your snippet:
            // Red = Close, Orange = Medium, Cyan = Far
            if (dist < 100) g2.setColor(Color.RED);
            else if (dist < 250) g2.setColor(Color.ORANGE);
            else g2.setColor(Color.CYAN);

            // Changed size from 6 back to 3
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
        g2.drawString("[R] Reset  |  [SpaceBar] Spawn", 10, 35);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Black Hole Simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new BlackHoleSimulation());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Simple class to store particle data (Copied from your snippet)
    private static class Particle {
        double x, y;   // Position
        double vx, vy; // Velocity

        Particle(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
}