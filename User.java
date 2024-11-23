import java.io.*;
import java.util.*;

public class User {
    private String username;
    private String password;
    private double totalMoney;
    private static final String USER_FILE = "users.txt";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.totalMoney = 0.0;
    }

    public String getUsername() {
        return username;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    // 新增：添加金钱并更新文件
    public double addMoney(double amount) {
        this.totalMoney += amount;
        updateMoney();
        return this.totalMoney;
    }

    public boolean register() {
        try {
            // 检查用户是否已存在
            if (userExists(username)) {
                return false;
            }

            // 写入新用户信息
            try (PrintWriter writer = new PrintWriter(new FileWriter(USER_FILE, true))) {
                writer.println(username + "," + password + "," + totalMoney);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean userExists(String username) {
        try {
            File file = new File(USER_FILE);
            if (!file.exists()) {
                return false;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 1 && parts[0].equals(username)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateMoney() {
        try {
            List<String> lines = new ArrayList<>();
            File file = new File(USER_FILE);
            
            // 读取所有行
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(username)) {
                        // 更新当前用户的金钱
                        lines.add(username + "," + password + "," + totalMoney);
                    } else {
                        lines.add(line);
                    }
                }
            }

            // 写回文件
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User login(String username, String password) {
        try {
            File file = new File(USER_FILE);
            if (!file.exists()) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3 && parts[0].equals(username) && parts[1].equals(password)) {
                        User user = new User(username, password);
                        user.totalMoney = Double.parseDouble(parts[2]);
                        return user;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
