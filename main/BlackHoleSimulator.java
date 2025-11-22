import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;



public class BlackHoleSimulator extends JPanel implements ActionListener {

    // Global settings
    //I made the variable static to ensure the stability of the project 
    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    static final int BH_RADIUS = 25; // size of the black hole itself
    static final double GRAVITY = 1500.0; 
    
    // using an ArrayList because I don't know how many particles there will be
    ArrayList<Particle> particles = new ArrayList<>();
    Timer timer;
    Random rand = new Random();

    // Center of the screen
    int centerX = WIDTH / 2;
    int centerY = HEIGHT / 2;

    public BlackHoleSimulator() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true); // needed for keyboard input

        // Mouse click to add stuff
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                spawnParticles(e.getX(), e.getY());
            }
        });

        // Added spacebar to clear screen (Extra feature!!)
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    particles.clear();
                    System.out.println("Universe reset.");
                }
            }
        });

        // 60 FPS loop
        timer = new Timer(16, this);
        timer.start();
    }

    // Helper to make a bunch of particles at once
    void spawnParticles(int x, int y) {
        for (int i = 0; i < 30; i++) {
            // Math to make them orbit instead of falling straight in
            double dx = centerX - x;
            double dy = centerY - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            // Perpendicular velocity vector
            // Added some randomness so they don't all look the same
            double vx = -dy / dist * 6.0 + (rand.nextDouble() * 2 - 1); 
            double vy = dx / dist * 6.0 + (rand.nextDouble() * 2 - 1);

            particles.add(new Particle(x, y, vx, vy));
        }
        System.out.println("Added cluster. Total particles: " + particles.size());
    }

    // The main game loop
    public void actionPerformed(ActionEvent e) {
        // Using a standard for-loop with index so I can remove items easily
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            
            double dx = centerX - p.x;
            double dy = centerY - p.y;
            double dist = Math.sqrt(dx*dx + dy*dy);

            // Event Horizon: If it hits the middle, it's gone forever
            if (dist < BH_RADIUS + 5) {
                particles.remove(i);
                i--; // fix index since we removed one
                continue;
            }

            // F = G * m / r^2
            // This is the inverse square law we learned in Physics 101
            double force = GRAVITY / (dist * dist);
            
            // Acceleration
            double ax = (dx / dist) * force;
            double ay = (dy / dist) * force;

            // Update velocity
            p.vx += ax;
            p.vy += ay;

            // Update position
            p.x += p.vx;
            p.y += p.vy;
            
            // Update color based on speed/distance (Doppler effect ish?)
            if (dist < 150) p.c = new Color(100, 200, 255); // Blue-hot
            else if (dist < 300) p.c = new Color(255, 100, 100); // Red-shift
            else p.c = Color.WHITE;
        }
        repaint(); // Draw everything again
    }

    // Drawing code
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Turn on anti-aliasing to make circles look smooth
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- DRAW ACCRETION DISK ---
        // I made this glowing effect by drawing many circles with low opacity
        for (int i = 0; i < 60; i++) {
            int alpha = 255 - (i * 4); // Fade out as it gets bigger
            if (alpha < 0) alpha = 0;
            
            g2.setColor(new Color(255, 165, 0, alpha / 4)); // Orange glow
            int radius = BH_RADIUS + i * 2;
            int size = radius * 2;
            g2.drawOval(centerX - radius, centerY - radius, size, size);
        }

        // Draw the actual black hole
        g2.setColor(Color.BLACK);
        g2.fillOval(centerX - BH_RADIUS, centerY - BH_RADIUS, BH_RADIUS * 2, BH_RADIUS * 2);
        
        // White outline for event horizon
        g2.setColor(Color.WHITE);
        g2.drawOval(centerX - BH_RADIUS, centerY - BH_RADIUS, BH_RADIUS * 2, BH_RADIUS * 2);

        // Draw all particles
        for (Particle p : particles) {
            g2.setColor(p.c);
            // g2.fillRect((int)p.x, (int)p.y, 2, 2); // tried rect first, looked bad
            g2.fillOval((int)p.x, (int)p.y, 4, 4);
        }
        
        // UI text
        g2.setColor(Color.CYAN);
        g2.drawString("Particles: " + particles.size(), 10, 20);
        g2.drawString("Press SPACE to reset", 10, 40);
    }

    // Simple class to hold particle info
    class Particle {
        double x, y;
        double vx, vy;
        Color c;

        public Particle(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.c = Color.WHITE;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Gravity Sim Final Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BlackHoleSimulator sim = new BlackHoleSimulator();
        frame.add(sim);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}