import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class PlaneGame {
    private JFrame frame;
    private User currentUser;
    private GamePanel gamePanel;
    private LoginPanel loginPanel;
    private MenuPanel menuPanel;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public PlaneGame() {
        frame = new JFrame("飞机大战");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        // 使用CardLayout来管理不同的面板
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        frame.add(mainPanel);

        // 初始化各个面板
        loginPanel = new LoginPanel();
        mainPanel.add(loginPanel, "login");
        
        // 显示登录面板
        cardLayout.show(mainPanel, "login");
        frame.setVisible(true);
    }

    // 新增：主菜单面板
    private class MenuPanel extends JPanel {
        private Image backgroundImage;

        public MenuPanel() {
            setLayout(new GridBagLayout());
            createBackgroundImage();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            // 欢迎文本
            JLabel welcomeLabel = new JLabel("欢迎回来, " + currentUser.getUsername());
            welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
            welcomeLabel.setForeground(Color.WHITE);
            gbc.gridy = 0;
            add(welcomeLabel, gbc);

            // 总金额显示
            JLabel moneyLabel = new JLabel(String.format("总金额: %.2f", currentUser.getTotalMoney()));
            moneyLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
            moneyLabel.setForeground(Color.YELLOW);
            gbc.gridy = 1;
            add(moneyLabel, gbc);

            // 开始游戏按钮
            JButton startButton = new JButton("开始游戏");
            startButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
            startButton.setPreferredSize(new Dimension(200, 50));
            startButton.addActionListener(e -> {
                gamePanel = new GamePanel();
                mainPanel.add(gamePanel, "game");
                cardLayout.show(mainPanel, "game");
                gamePanel.requestFocusInWindow();
                gamePanel.startGame();
            });
            gbc.gridy = 2;
            add(startButton, gbc);

            // 退出按钮
            JButton exitButton = new JButton("退出登录");
            exitButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
            exitButton.setPreferredSize(new Dimension(200, 50));
            exitButton.addActionListener(e -> {
                cardLayout.show(mainPanel, "login");
                loginPanel = new LoginPanel();
                mainPanel.add(loginPanel, "login");
                cardLayout.show(mainPanel, "login");
            });
            gbc.gridy = 3;
            add(exitButton, gbc);
        }

        private void createBackgroundImage() {
            backgroundImage = createGradientBackground(getWidth(), getHeight());
        }

        private Image createGradientBackground(int width, int height) {
            BufferedImage bg = new BufferedImage(
                Math.max(1, width),
                Math.max(1, height),
                BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g2d = bg.createGraphics();
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(70, 130, 180),
                0, height, new Color(25, 25, 112)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();
            return bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 如果大小改变，重新创建背景
            if (backgroundImage == null || 
                backgroundImage.getWidth(null) != getWidth() || 
                backgroundImage.getHeight(null) != getHeight()) {
                createBackgroundImage();
            }
            g.drawImage(backgroundImage, 0, 0, this);
        }
    }

    private void showMenu() {
        menuPanel = new MenuPanel();
        mainPanel.add(menuPanel, "menu");
        cardLayout.show(mainPanel, "menu");
    }

    private class GamePanel extends JPanel {
        private static final int PLANE_SPEED = 8; // 提高飞机速度
        private static final int BULLET_SPEED = 10;
        private static final int REDPACKET_SPEED = 3;
        private static final int GAME_DURATION = 60;
        private static final int DEFAULT_WIDTH = 800;
        private static final int DEFAULT_HEIGHT = 600;

        private Image planeImage;
        private Image bulletImage;
        private Image redPacketImage;
        private Image backgroundImage;
        private int planeX = 400;
        private int planeY = 500;
        private List<Point> bullets = new ArrayList<>();
        private List<RedPacket> redPackets = new ArrayList<>();
        private double currentGameMoney = 0;
        private int timeLeft = GAME_DURATION;
        private javax.swing.Timer gameTimer;
        private javax.swing.Timer redPacketTimer;
        private javax.swing.Timer countdownTimer;
        private Random random = new Random();
        private boolean isPaused = false;

        private class RedPacket {
            int x, y;
            double value;
            RedPacket(int x, int y, double value) {
                this.x = x;
                this.y = y;
                this.value = value;
            }
        }

        public GamePanel() {
            setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            setBackground(Color.WHITE);
            loadImages();
            setupKeyListener();
        }

        private void loadImages() {
            try {
                planeImage = ImageIO.read(new File("images/plane.png"));
                bulletImage = ImageIO.read(new File("images/bullet.png"));
                redPacketImage = ImageIO.read(new File("images/redpacket.png"));
                backgroundImage = createGradientBackground();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "无法加载图片资源！");
                System.exit(1);
            }
        }

        private Image createGradientBackground() {
            int width = Math.max(DEFAULT_WIDTH, getWidth());
            int height = Math.max(DEFAULT_HEIGHT, getHeight());
            
            BufferedImage bg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bg.createGraphics();
            
            // 创建更丰富的渐变背景
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(135, 206, 235), // 天蓝色
                0, height, new Color(25, 25, 112) // 深蓝色
            );
            
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            
            // 添加一些装饰性的元素
            g2d.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 50; i++) {
                int starX = random.nextInt(width);
                int starY = random.nextInt(height);
                int starSize = random.nextInt(3) + 1;
                g2d.fillOval(starX, starY, starSize, starSize);
            }
            
            g2d.dispose();
            return bg;
        }

        private void setupKeyListener() {
            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (!isPaused) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_LEFT:
                                if (planeX > 0) planeX -= PLANE_SPEED;
                                break;
                            case KeyEvent.VK_RIGHT:
                                if (planeX < getWidth() - 50) planeX += PLANE_SPEED;
                                break;
                            case KeyEvent.VK_UP:
                                if (planeY > 0) planeY -= PLANE_SPEED;
                                break;
                            case KeyEvent.VK_DOWN:
                                if (planeY < getHeight() - 30) planeY += PLANE_SPEED;
                                break;
                            case KeyEvent.VK_X:
                                bullets.add(new Point(planeX + 25, planeY));
                                break;
                            case KeyEvent.VK_C:
                                pauseGame();
                                break;
                        }
                        repaint();
                    } else if (e.getKeyCode() == KeyEvent.VK_C) {
                        resumeGame();
                    }
                }
            });
        }

        private void pauseGame() {
            isPaused = true;
            gameTimer.stop();
            redPacketTimer.stop();
            countdownTimer.stop();
            
            int choice = JOptionPane.showConfirmDialog(
                this,
                "游戏已暂停\n是否继续游戏？",
                "暂停",
                JOptionPane.YES_NO_OPTION
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                resumeGame();
            } else {
                endGame();
            }
        }

        private void resumeGame() {
            isPaused = false;
            gameTimer.start();
            redPacketTimer.start();
            countdownTimer.start();
        }

        public void startGame() {
            gameTimer = new javax.swing.Timer(50, e -> {
                updateGame();
                repaint();
            });
            gameTimer.start();

            redPacketTimer = new javax.swing.Timer(2000, e -> spawnRedPacket());
            redPacketTimer.start();

            countdownTimer = new javax.swing.Timer(1000, e -> {
                timeLeft--;
                if (timeLeft <= 0) {
                    endGame();
                }
            });
            countdownTimer.start();
        }

        private void updateGame() {
            // Update bullets
            Iterator<Point> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Point bullet = bulletIterator.next();
                bullet.y -= BULLET_SPEED;
                if (bullet.y < 0) {
                    bulletIterator.remove();
                }
            }

            // Update red packets
            Iterator<RedPacket> packetIterator = redPackets.iterator();
            while (packetIterator.hasNext()) {
                RedPacket packet = packetIterator.next();
                packet.y += REDPACKET_SPEED;
                if (packet.y > getHeight()) {
                    packetIterator.remove();
                    continue;
                }

                // Check collision with bullets
                for (Point bullet : bullets) {
                    if (Math.abs(bullet.x - packet.x) < 30 && Math.abs(bullet.y - packet.y) < 30) {
                        currentGameMoney += packet.value;
                        packetIterator.remove();
                        break;
                    }
                }
            }
        }

        private void spawnRedPacket() {
            int x = random.nextInt(getWidth() - 50);
            double value = random.nextInt(90) + 10;
            redPackets.add(new RedPacket(x, 0, value));
        }

        private void endGame() {
            gameTimer.stop();
            redPacketTimer.stop();
            countdownTimer.stop();

            double newTotal = currentUser.addMoney(currentGameMoney);

            JOptionPane.showMessageDialog(this, 
                String.format("游戏结束!\n本局获得: %.2f\n总金额: %.2f", currentGameMoney, newTotal));

            showMenu();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // 如果背景图片大小与面板不匹配，重新创建背景
            if (backgroundImage == null || 
                backgroundImage.getWidth(null) != getWidth() || 
                backgroundImage.getHeight(null) != getHeight()) {
                backgroundImage = createGradientBackground();
            }
            
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw plane with shadow
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.drawImage(planeImage, planeX + 2, planeY + 2, 50, 30, null);
            g2d.drawImage(planeImage, planeX, planeY, 50, 30, null);

            // Draw bullets with glow effect
            g2d.setColor(new Color(255, 255, 0, 50));
            for (Point bullet : bullets) {
                g2d.fillOval(bullet.x - 2, bullet.y - 2, 9, 14);
                g2d.drawImage(bulletImage, bullet.x, bullet.y, 5, 10, null);
            }

            // Draw red packets with glow and value
            for (RedPacket packet : redPackets) {
                // Draw glow effect
                g2d.setColor(new Color(255, 0, 0, 30));
                g2d.fillOval(packet.x - 5, packet.y - 5, 40, 40);
                
                // Draw red packet
                g2d.drawImage(redPacketImage, packet.x, packet.y, 30, 30, null);
                
                // Draw value with shadow
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String value = String.format("%.0f", packet.value);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.drawString(value, packet.x + 1, packet.y - 4);
                
                // Draw text
                g2d.setColor(Color.YELLOW);
                g2d.drawString(value, packet.x, packet.y - 5);
            }

            // Draw HUD with shadow effect
            drawHUD(g2d);

            // Draw pause overlay if needed
            if (isPaused) {
                drawPauseOverlay(g2d);
            }
        }

        private void drawHUD(Graphics2D g2d) {
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
            
            String[] texts = {
                String.format("时间: %d", timeLeft),
                String.format("当前获得: %.2f", currentGameMoney),
                String.format("总金额: %.2f", currentUser.getTotalMoney())
            };
            
            int y = 30;
            for (String text : texts) {
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.drawString(text, 11, y + 1);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, 10, y);
                
                y += 30;
            }
        }

        private void drawPauseOverlay(Graphics2D g2d) {
            // Draw semi-transparent overlay
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw pause text
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
            String pauseText = "游戏暂停";
            FontMetrics fm = g2d.getFontMetrics();
            
            // Calculate center position
            int textX = (getWidth() - fm.stringWidth(pauseText)) / 2;
            int textY = getHeight() / 2;
            
            // Draw text shadow
            g2d.setColor(new Color(0, 0, 0, 128));
            g2d.drawString(pauseText, textX + 2, textY + 2);
            
            // Draw text
            g2d.setColor(Color.WHITE);
            g2d.drawString(pauseText, textX, textY);
            
            // Draw instruction
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
            String instruction = "按 C 继续游戏";
            fm = g2d.getFontMetrics();
            textX = (getWidth() - fm.stringWidth(instruction)) / 2;
            textY += 40;
            
            g2d.drawString(instruction, textX, textY);
        }
    }

    private class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            // Username
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("用户名:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            usernameField = new JTextField(15);
            add(usernameField, gbc);

            // Password
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(new JLabel("密码:"), gbc);

            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            add(passwordField, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel();
            JButton loginButton = new JButton("登录");
            JButton registerButton = new JButton("注册");

            loginButton.addActionListener(e -> handleLogin());
            registerButton.addActionListener(e -> handleRegister());

            buttonPanel.add(loginButton);
            buttonPanel.add(registerButton);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(buttonPanel, gbc);
        }

        private void handleLogin() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入用户名和密码");
                return;
            }

            User user = User.login(username, password);
            if (user != null) {
                currentUser = user;
                showMenu();
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误");
            }
        }

        private void handleRegister() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入用户名和密码");
                return;
            }

            User newUser = new User(username, password);
            if (newUser.register()) {
                JOptionPane.showMessageDialog(this, "注册成功，请登录");
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "注册失败，用户名可能已存在");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlaneGame());
    }
}
