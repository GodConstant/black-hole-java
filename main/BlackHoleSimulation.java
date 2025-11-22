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
    
    // Physics: Stronger gravity makes the effect more obvious
    private static final double GRAVITY = 2000.0;       
    private static final double EVENT_HORIZON = 40.0; 
    
    // Friction: 0.998 means they keep 99.8% of their speed (lose 0.2% per frame)
    // This small loss is what causes the spiral orbit.
    private static final double FRICTION = 0.998;     
    
    // Re-enabled trails to see particle paths
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
     * Math: Uses Polar Coordinates (Angle + Distance) to place dots in a circle.
     */
    private void spawnGalaxy() {
        particles.clear();
        double minDist = EVENT_HORIZON * 2.5;
        double maxDist = 350; 

        for (int i = 0; i < 1200; i++) {
            // 1. Pick a random angle (0 to 360 degrees)
            double angle = rand.nextDouble() * Math.PI * 2;
            
            // 2. Pick a random distance from center
            double distance = minDist + (rand.nextDouble() * (maxDist - minDist));
            
            // 3. Convert to X/Y Position (Polar -> Cartesian)
            // x = cos(angle) * r
            // y = sin(angle) * r
            double x = center.x + Math.cos(angle) * distance;
            double y = center.y + Math.sin(angle) * distance;
            
            // 4. Calculate Perfect Orbit Speed
            // Formula: Velocity = Sqrt( Gravity / Radius )
            double speed = Math.sqrt(GRAVITY / distance);
            
            // 5. Set Velocity Direction (Perpendicular to the black hole)
            // To orbit, you move 90 degrees to the pull of gravity.
            // If position is (cos, sin), perpendicular is (-sin, cos).
            double vx = -Math.sin(angle) * speed;
            double vy = Math.cos(angle) * speed;

            particles.add(new Particle(x, y, vx, vy));
        }
    }

    private void spawnCluster(Point start) {
        for (int i = 0; i < 50; i++) { 
            // Simple random spread
            double x = start.x + (rand.nextDouble() * 20 - 10);
            double y = start.y + (rand.nextDouble() * 20 - 10);
            
            // Calculate speed based on distance so they don't immediately fall in
            double dx = center.x - x;
            double dy = center.y - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            double speed = Math.sqrt(GRAVITY / dist);

            // Add some random velocity
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

    /**
     * The Main Physics Engine
     * Steps:
     * 1. Calculate Distance to center.
     * 2. Calculate Gravity Strength (Newton's Law).
     * 3. Move Particle.
     */
    private void updatePhysics() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            
            // 1. Calculate Vector to Center (dx, dy)
            double dx = center.x - p.x;
            double dy = center.y - p.y;
            
            // 2. Calculate Distance (Pythagorean Theorem: a^2 + b^2 = c^2)
            double distance = Math.sqrt(dx*dx + dy*dy);

            // --- EVENT HORIZON (Delete if too close) ---
            if (distance < EVENT_HORIZON) {
                it.remove(); 
                continue;
            }

            // 3. Normalization: Get the "Direction" alone (length 1)
            double dirX = dx / distance;
            double dirY = dy / distance;

            // 4. Calculate Gravity Force
            // Formula: F = G / (distance * distance)
            // We enforce a minimum distance (10.0) to avoid dividing by zero errors
            double safeDist = Math.max(distance, 10.0);
            double force = GRAVITY / (safeDist * safeDist);

            // 5. Apply Force to Velocity (Acceleration)
            p.vx += dirX * force;
            p.vy += dirY * force;

            // 6. Apply Friction (The Spiral Effect)
            p.vx *= FRICTION;
            p.vy *= FRICTION;

            // 7. Move Particle
            p.x += p.vx;
            p.y += p.vy;
            
            // Calculate speed for coloring later
            p.speed = Math.sqrt(p.vx*p.vx + p.vy*p.vy);
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
            // Color Logic: Faster = Blue, Slower = Red
            float speedRatio = (float) Math.min(p.speed / 10.0, 1.0f);
            float hue = 0.0f + (speedRatio * 0.6f); 
            
            g2.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
            // Increased size from 3 to 6
            g2.fill(new Ellipse2D.Double(p.x, p.y, 6, 6));
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
        g2.drawString("[R] Reset  |  [Click] Spawn", 10, 35);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simplified Black Hole");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new BlackHoleSimulation());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static class Particle {
        double x, y, vx, vy, speed;
        public Particle(double x, double y, double vx, double vy) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        }
    }
}