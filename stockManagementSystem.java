  import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.Scanner;

// Product class with Encapsulation
class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private int quantity;
    private double price;

    public Product(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;	
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Quantity: " + quantity + ", Price: $" + price;
    }
}

// Main Stock Management System
public class StockManagementSystem extends Frame {
    private Product[] inventory;
    private int productCount = 0;
    private final int maxProducts;
    private static final String FILE_NAME = "inventory.dat";

    public StockManagementSystem(int maxProducts) {
        this.maxProducts = maxProducts;
        inventory = new Product[maxProducts];
        loadInventory();
        setupGUI();
        startAutoSave();
    }

    // Add Product
    public synchronized void addProduct(int id, String name, int quantity, double price) {
        if (productCount >= inventory.length) {
            System.out.println("Inventory is full. Cannot add more products.");
            return;
        }
        inventory[productCount++] = new Product(id, name, quantity, price);
        System.out.println("Product added successfully!");
    }

    // Update Stock
    public synchronized void updateStock(int productId, int newQuantity) {
        for (int i = 0; i < productCount; i++) {
            if (inventory[i].getId() == productId) {
                inventory[i].setQuantity(newQuantity);
                System.out.println("Stock updated successfully for: " + inventory[i].getName());
                return;
            }
        }
        System.out.println("Product not found.");
    }

    // Place Order
    public synchronized void placeOrder(int productId, int orderQuantity) {
        for (int i = 0; i < productCount; i++) {
            if (inventory[i].getId() == productId) {
                if (inventory[i].getQuantity() >= orderQuantity) {
                    inventory[i].setQuantity(inventory[i].getQuantity() - orderQuantity);
                    System.out.println("Order placed successfully for: " + inventory[i].getName());
                } else {
                    System.out.println("Insufficient stock for: " + inventory[i].getName());
                }
                return;
            }
        }
        System.out.println("Product not found.");
    }

    // Display Inventory
    public void displayInventory() {
        if (productCount == 0) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("Inventory Status:");
        for (int i = 0; i < productCount; i++) {
            System.out.println(inventory[i]);
        }
    }

    // File Handling: Save Inventory
    private synchronized void saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(inventory);
            oos.writeInt(productCount);
            System.out.println("Inventory saved to file.");
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }

    // File Handling: Load Inventory
    private void loadInventory() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            inventory = (Product[]) ois.readObject();
            productCount = ois.readInt();
            System.out.println("Inventory loaded from file.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous inventory found. Starting fresh.");
        }
    }

    // Multithreading: Auto-Save Inventory
    private void startAutoSave() {
        Thread autoSaveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Save every 30 seconds
                    saveInventory();
                } catch (InterruptedException e) {
                    System.out.println("Auto-save interrupted.");
                }
            }
        });
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }

    // AWT GUI
    private void setupGUI() {
        setTitle("Stock Management System");
        setSize(400, 300);
        setLayout(new FlowLayout());

        Button addButton = new Button("Add Product");
        Button updateButton = new Button("Update Stock");
        Button orderButton = new Button("Place Order");
        Button displayButton = new Button("Display Inventory");
        Button exitButton = new Button("Exit");

        add(addButton);
        add(updateButton);
        add(orderButton);
        add(displayButton);
        add(exitButton);

        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(prompt("Enter Product ID:"));
                String name = prompt("Enter Product Name:");
                int quantity = Integer.parseInt(prompt("Enter Product Quantity:"));
                double price = Double.parseDouble(prompt("Enter Product Price:"));
                addProduct(id, name, quantity, price);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please try again.");
            }
        });

        updateButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(prompt("Enter Product ID to update:"));
                int newQuantity = Integer.parseInt(prompt("Enter new stock quantity:"));
                updateStock(id, newQuantity);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please try again.");
            }
        });

        orderButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(prompt("Enter Product ID to order:"));
                int quantity = Integer.parseInt(prompt("Enter order quantity:"));
                placeOrder(id, quantity);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please try again.");
            }
        });

        displayButton.addActionListener(e -> displayInventory());

        exitButton.addActionListener(e -> {
            saveInventory();
            System.out.println("Exiting the system. Goodbye!");
            System.exit(0);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveInventory();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private String prompt(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    public static void main(String[] args) {
        int maxProducts = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum number of products:"));
        new StockManagementSystem(maxProducts);
    }
}
