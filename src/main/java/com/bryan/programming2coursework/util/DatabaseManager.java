package com.bryan.programming2coursework.util;

import com.bryan.programming2coursework.model.MenuItem;
import com.bryan.programming2coursework.model.User.UserRole;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Random;

import static com.bryan.programming2coursework.model.MenuItem.MenuCategory.*;
import static com.bryan.programming2coursework.model.MenuItem.MenuCategory.CHICKEN_AND_FISH_SANDWICHES;

/**
 * DatabaseManager handles SQLite database connection and initialization
 * Provides centralized database management for the application
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/mcronalds.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Get a connection to the database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initialize database schema and create default data
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            createDefaultData(conn);
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL,
                            role TEXT NOT NULL,
                            email TEXT NOT NULL,
                            phone TEXT NOT NULL
                        )
                    """);

            // menu items
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS menu_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            category TEXT NOT NULL,
                            price REAL NOT NULL,
                            stock INTEGER NOT NULL,
                            description TEXT,
                            image_path TEXT,
                            calories INTEGER NOT NULL
                        )
                    """);

            // orders
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS orders (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER NOT NULL,
                            order_date TEXT NOT NULL,
                            status TEXT NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """);

            // this depends on both orders and menu_items so it's created last
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS order_items (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            order_id INTEGER NOT NULL,
                            menu_item_id INTEGER NOT NULL,
                            quantity INTEGER NOT NULL,
                            FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                            FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                        )
                    """);

        } catch (SQLException e) {
            System.err.println("Database Creation Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create default users and menu items if tables are empty
     */
    private void createDefaultData(Connection conn) throws SQLException {
        // get number of users
        Statement checkStmt = conn.createStatement();
        ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users");
        rs.next();
        int userCount = rs.getInt(1);
        rs.close();

        if (userCount == 0) {
            // if none, create some defaults
            createDefaultUsers(conn);
        }

        // check if menu exists
        rs = checkStmt.executeQuery("SELECT COUNT(*) FROM menu_items");
        rs.next();
        int menuCount = rs.getInt(1);
        rs.close();
        checkStmt.close();

        if (menuCount == 0) {
            // same thing, add defaults
            createDefaultMenuItems(conn);
        }
    }

    /**
     * Create default admin and customer accounts
     */
    private void createDefaultUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users(username, password, role, email, phone) VALUES(?,?,?,?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        // default admin account
        stmt.setString(1, "admin");
        stmt.setString(2, "admin123");
        stmt.setString(3, UserRole.ADMIN.toString());
        stmt.setString(4, "admin@mcronalds.com");
        stmt.setString(5, "0123456789");
        stmt.executeUpdate();

        // default customer account
        stmt.setString(1, "customer");
        stmt.setString(2, "customer123");
        stmt.setString(3, UserRole.CUSTOMER.toString());
        stmt.setString(4, "customer@example.com");
        stmt.setString(5, "0163432567");
        stmt.executeUpdate();

        stmt.close();
        System.out.println("Default users created");
    }

    private static final Random rand = new Random();

    private void createDefaultMenuItems(Connection conn) throws SQLException {
        String sql = "INSERT INTO menu_items(name, category, price, stock, description, image_path, calories) VALUES(?,?,?,?,?,?,?)";

        // this is going to be annoying
        // I would use a loop but this allows me to edit each item easily
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            add(stmt, "Bacon, Egg & Cheese Biscuit", BREAKFAST, 4.20, "Start your morning with a delicious Bacon, Egg & Cheese Biscuit breakfast sandwich from the McDonald's breakfast menu! The McDonald’s Bacon, Egg & Cheese Biscuit recipe features a warm buttermilk biscuit brushed with real butter, thick-cut Applewood smoked bacon, a fluffy folded egg, and a slice of melty American cheese.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202405_0085_BaconEggCheeseBiscuit_1564x1564?wid=1000&hei=1000&dpr=off", 460);
            add(stmt, "Egg McMuffin®", BREAKFAST, 3.90, "Satisfy your McDonald's breakfast cravings with our Egg McMuffin® breakfast sandwich—it’s an excellent source of protein and oh so delicious. McDonald's Egg McMuffin® recipe features a freshly cracked Grade A egg placed on a toasted English Muffin topped with real butter, lean Canadian bacon, and melty American cheese.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202602_0046_EggMcMuffin_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 310);
            add(stmt, "Sausage McMuffin®", BREAKFAST, 2.50, "McDonald's Sausage McMuffin recipe features a warm, freshly toasted English muffin, topped with a savory hot sausage patty and a slice of melty American cheese.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202405_0078_SausageMcMuffin_1564x1564-1?wid=1000&hei=1000&dpr=off", 400);
            add(stmt, "Sausage McMuffin® with Egg", BREAKFAST, 4.00, "Start your day off with a McDonald’s Sausage McMuffin with Egg breakfast sandwich. Our Sausage Egg McMuffin recipe features a savory hot sausage, a slice of melty American cheese and a delicious, freshly cracked egg all on a freshly toasted English muffin.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201907_0083_SausageEggMcMuffin_1564x1564?wid=1000&hei=1000&dpr=off", 480);
            add(stmt, "Sausage Biscuit", BREAKFAST, 2.00, "McDonald’s Sausage Biscuit is the perfect sausage breakfast sandwich to start your day! The Sausage Biscuit is made with sizzling hot sausage on a warm buttermilk biscuit that’s topped with real butter and baked to perfection.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201907_0062_SausageBiscuit_1564x1564-1?wid=1000&hei=1000&dpr=off", 460);
            add(stmt, "Sausage Biscuit with Egg", BREAKFAST, 3.50, "McDonald's Sausage and Egg Biscuit sandwich features a warm, flaky biscuit brushed with real butter, a sizzling hot pork sausage patty, and a classic McDonald's folded egg. It's the perfect savory biscuit breakfast sandwich when you're looking for a quick, easy breakfast.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201907_0092_SausageEggBiscuit_1564x1564?wid=1000&hei=1000&dpr=off", 530);
            add(stmt, "Bacon, Egg & Cheese McGriddles®", BREAKFAST, 4.50, "McDonald's Bacon, Egg & Cheese McGriddles recipe features thick-cut Applewood smoked bacon, a fluffy folded egg and a slice of melty American cheese all on soft, warm griddle cakes with the sweet taste of maple. It’s the perfect McGriddles breakfast sandwich! McGriddles cakes have no artificial preservatives or flavors and no colors from artificial sources.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201908_9839_BEC_McGriddle_1564x1564-1?wid=1000&hei=1000&dpr=off", 430);
            add(stmt, "Sausage McGriddles®", BREAKFAST, 3.20, "McDonald’s Sausage McGriddles® is the perfect sausage breakfast sandwich to start the day! Our Sausage McGriddles recipe features soft, warm griddle cakes—with the taste of sweet maple—that hold our savory, sizzling hot sausage. McGriddles cakes have no artificial preservatives or flavors and no colors from artificial sources.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201911_6110_SausageMcGriddle_1564x1564?wid=1000&hei=1000&dpr=off", 430);
            add(stmt, "Sausage, Egg & Cheese McGriddles®", BREAKFAST, 4.50, "Sausage, Egg & Cheese McGriddles® feature soft, warm griddle cakes—with the sweet taste of maple—that hold a fluffy folded egg, savory sausage, and melty American cheese. McGriddles® cakes have no artificial preservatives or flavors and no colors from artificial sources. ", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201907_9841_SausageEggCheeseMcGriddle_1564x1564-2?wid=1000&hei=1000&dpr=off", 550);

            add(stmt, "Bacon, Egg & Cheese Bagel", BREAKFAST, 5.20, "Craving a breakfast sandwich at McDonald's? The Bacon, Egg and Cheese Bagel features a toasted bagel with butter, thick-cut Applewood smoked bacon, a fluffy folded egg, creamy breakfast sauce and two slices of melty American cheese.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202103_1603_BaconEggCheeseBagel_1564x1564-1?wid=1000&hei=1000&dpr=off", 590);
            add(stmt, "Sausage, Egg & Cheese Bagel", BREAKFAST, 5.20, "You can’t go wrong with this classic bagel sandwich—it’s made with a juicy sausage patty, a fluffy folded egg, creamy breakfast sauce and two slices of melty American cheese inside a toasted bagel.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202403_1604_SausageEggCheeseBagel_1564x1564-1?wid=1000&hei=1000&dpr=off", 710);
            add(stmt, "Steak, Egg & Cheese Bagel", BREAKFAST, 5.90, "This breakfast bagel is next level—it’s toasted with real butter and holds a tender, juicy steak patty, a fluffy folded egg, melty American cheese, creamy breakfast sauce and savory grilled onions.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202103_5918_SteakEggCheeseBagel_1564x1564-1?wid=1000&hei=1000&dpr=off", 680);
            add(stmt, "Bagel (plain)", BREAKFAST, 2.20, "Start your morning off right with a breakfast bagel from McDonald’s!", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201909_5428_PlainBagel_1564x1564-1?wid=1000&hei=1000&dpr=off", 270);
            add(stmt, "Egg and Cheese Bagel", BREAKFAST, 4.50, "Start your morning with a satisfying McDonald’s Egg and Cheese Bagel. It’s a bagel breakfast sandwich loaded with folded egg, creamy breakfast sauce and melty American Cheese.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202410_4210_EggCheeseBagel_1564x1564?wid=1000&hei=1000&dpr=off", 520);

            add(stmt, "Big Breakfast®", BREAKFAST, 6.20, "Our full, satisfying Big Breakfast is perfect for any morning. Ever wondered what's in a McDonald's Big Breakfast? Wake up to a breakfast meal with a warm biscuit, fluffy scrambled eggs, savory McDonald's sausage and crispy golden Hash Browns.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202405_0107_BigBreakfast._1564x1564?wid=1000&hei=1000&dpr=off", 1060);
            add(stmt, "Big Breakfast® with Hotcakes", BREAKFAST, 8.50, "McDonald’s Big Breakfast® with Hotcakes satisfies with both sweet and savory breakfast favorites. Fill up with a warm Biscuit, savory hot Sausage, fluffy scrambled Eggs, crispy Hash Browns, and golden brown Hotcakes with a side of real butter and the sweet flavor of maple.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202208_3590_BigBreakfast_HotCakes_1564x1564-1?wid=1000&hei=1000&dpr=off", 1340);
            add(stmt, "Hotcakes", BREAKFAST, 4.00, "If you love hot pancakes, you've got to try McDonald's Hotcakes with a side of real butter and sweet maple flavored Hotcake syrup. This McDonald's breakfast comes with 3 golden brown Hotcakes.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202405_0031_3HotCakes_1564x1564?wid=1000&hei=1000&dpr=off", 580);
            add(stmt, "Hotcakes and Sausage", BREAKFAST, 5.50, "When you're craving pancakes and sausage, McDonald's has you covered with our Hotcakes and Sausage breakfast. Featuring 3 golden brown Hotcakes and real butter, topped with sweet maple flavored hotcake syrup. On the side, you get a hot McDonald's sausage patty.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202405_0032_3HotCakes_Sausage_1564x1564?wid=1000&hei=1000&dpr=off", 770);
            add(stmt, "Sausage Burrito", BREAKFAST, 2.50, "McDonald's Breakfast Burrito is loaded with fluffy scrambled egg, pork sausage, melty cheese, green chiles, and onion! It's wrapped in a soft tortilla, making it the perfect grab and go breakfast.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202004_0334_SausageBurrito_Alt_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 310);
            add(stmt, "Hash Browns", BREAKFAST, 1.80, "McDonald’s Hash Browns are deliciously tasty and perfectly crispy. This crispy Hash Browns recipe features shredded potato hash brown patties that are prepared so they’re fluffy on the inside and crispy and toasty on the outside.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202211_0035_HashBrowns_Upright_1564x1564?wid=1000&hei=1000&dpr=off", 140);
            add(stmt, "Fruit & Maple Oatmeal", BREAKFAST, 3.80, "McDonald's Fruit and Maple Oatmeal recipe features two full servings of whole-grain oats with a touch of cream and brown sugar. McDonald’s oatmeal is loaded with red and green apples, cranberries, and two varieties of raisins, making for a hearty, wholesome breakfast of whole grains and fruit.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202002_1500_Oatmeal_Fruit_1564x1564-1?wid=1000&hei=1000&dpr=off", 320);

            add(stmt, "Egg McMuffin® Meal", BREAKFAST, 6.50, "Get more for breakfast with the McDonald’s Egg McMuffin Meal. This McDonald’s breakfast features an Egg McMuffin, crispy, golden Hash Browns and a small Premium Roast Coffee—everything you need to start the day off right!", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202007_0252_EVM_HB_EggMcMuffin_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 455);
            add(stmt, "Sausage McMuffin® with Egg Meal", BREAKFAST, 6.60, "Wake up to a Sausage McMuffin with Egg Meal for breakfast, served with crispy, golden Hash Browns and a small Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202008_3655_EVM_HB_SausageEggMcMuffin_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 625);
            add(stmt, "Sausage Biscuit with Egg Meal", BREAKFAST, 6.10, "McDonald's Sausage Biscuit with Egg Meal includes a flaky biscuit sandwich with McDonald's folded egg and a pork sausage patty.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202007_0251_EVM_HB_SausageEggBiscuit_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 675);
            add(stmt, "Bacon, Egg & Cheese Biscuit Meal", BREAKFAST, 6.80, "The Bacon, Egg & Cheese Biscuit Meal at McDonald's is the perfectly fluffy and golden-brown way to start your day. This McDonald's breakfast is served with Hash Browns and a small Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202104_0250_EVM_HB_BaconEggCheeseBiscuit_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 605);
            add(stmt, "Bacon, Egg & Cheese McGriddles® Meal", BREAKFAST, 7.20, "The Bacon, Egg, and Cheese McGriddles® Meal features the sweet taste of pancakes and syrup and the taste of savory, thick cut bacon, folded fluffy eggs and perfectly melted cheese. This McDonald’s breakfast meal includes the classic Bacon, Egg and Cheese McGriddles® and is served with golden, crispy hash browns and small, freshly brewed McCafé® Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202008_9840_EVM_HB_BaconEggCheeseMcGriddle_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 575);
            add(stmt, "Sausage, Egg & Cheese McGriddles® Meal", BREAKFAST, 7.20, "Sweet meets savory with the taste of a Sausage, Egg, and Cheese McGriddles® Meal. This breakfast favorite is made with a hot sausage patty, pasteurized American cheese and folded, fluffy eggs sandwiched between two griddle cakes. This McDonald’s breakfast is complete with a Sausage, Egg & Cheese McGriddles breakfast sandwich, crispy, golden Hash Browns and a small Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202008_9842_EVM_HB_SausageEggCheeseMcGriddle_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 695);
            add(stmt, "Sausage McGriddles® Meal", BREAKFAST, 5.90, "Our McDonald’s Sausage McGriddles meal features soft, warm griddle cakes with a touch of sweetness and savory sausage, with crispy golden Hash Browns and a small McCafé® Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202211_6116_EVM_HB_SausageMcGriddle_Coffee_Glass_1564x1564:nutrition-calculator-tile?wid=600&hei=600&dpr=off", 575);
            add(stmt, "Sausage Burrito Meal", BREAKFAST, 5.50, "Add some spice to your morning with a McDonald's Sausage Burrito Meal. This breakfast burrito meal features two Sausage Burritos on warm flour tortillas, plus crispy, golden Hash Browns and a small Premium Roast Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202008_5093_EVM_HB_2SausageBurrito_Coffee_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 765);

            add(stmt, "Big Mac®", BURGERS, 5.80, "When a craving hits for those two all-beef patties it’s time to stop thinking about what’s on a Big Mac® and take a bite. The McDonald's Big Mac is a 100% beef burger with a taste like no other. The mouthwatering perfection starts with two 100% pure all beef patties and Big Mac Sauce sandwiched between a sesame seed bun. It’s topped off with pickles, crisp shredded lettuce, finely chopped onion and a slice of American cheese. It contains no artificial flavors, preservatives, or added colors from artificial sources. Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202302_0005-999_BigMac_1564x1564-1?wid=1000&hei=1000&dpr=off", 580);
            add(stmt, "Quarter Pounder® with Cheese", BURGERS, 5.40, "Each Quarter Pounder with Cheese burger features a ¼ lb. of 100% fresh beef that’s hot, deliciously juicy and cooked when you order. What comes on a Quarter Pounder? Each fresh beef burger is seasoned with just a pinch of salt and pepper, sizzled on a flat iron grill, then topped with slivered onions, tangy pickles and two slices of melty American cheese on a sesame seed bun. Our QPC® contains no artificial flavors, preservatives or added colors from artificial sources. Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202201_0007-005_QuarterPounderwithCheese_1564x1564?wid=1000&hei=1000&dpr=off", 520);
            add(stmt, "Double Quarter Pounder® with Cheese", BURGERS, 7.20, "Each Double Quarter Pounder with Cheese features two quarter pound 100% fresh beef burger patties that are hot, deliciously juicy and cooked when you order. McDonald’s beef patties are seasoned with just a pinch of salt and pepper, sizzled on a flat iron grill, then topped with slivered onions, tangy pickles and two slices of melty cheese on a sesame seed bun. It contains no artificial flavors, preservatives or added colors from artificial sources. Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202201_3426-005_DoubleQuarterPounderwithCheese_1564x1564-1?wid=1000&hei=1000&dpr=off", 740);
            add(stmt, "Quarter Pounder® with Cheese Deluxe", BURGERS, 5.90, "McDonald's Quarter Pounder® with Cheese Deluxe is a fresh take on a Quarter Pounder® classic burger. Crisp shredded lettuce and three Roma tomato slices top a ¼ lb. of 100% McDonald’s fresh beef that’s hot, deliciously juicy and cooked when you order. Seasoned with just a pinch of salt and pepper and sizzled on our flat iron grill. Layered with two slices of melty American cheese, creamy mayo, slivered onions and tangy pickles on a soft, fluffy sesame seed hamburger bun.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202309_4282_QuarterPounderCheeseDeluxe_Shredded_1564x1564?wid=1000&hei=1000&dpr=off", 630);
            add(stmt, "McDouble®", BURGERS, 2.90, "The classic McDouble burger stacks two 100% pure beef patties seasoned with just a pinch of salt and pepper. Wondering what the difference is between a McDouble and a Double Cheeseburger? A slice of cheese! What comes on a McDouble? Well, it’s topped with tangy pickles, chopped onions, ketchup, mustard and a melty slice of American cheese. There are 390 calories in a McDouble. The McDouble contains no artificial flavors, preservatives or added colors from artificial sources. Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202302_0592-999_McDouble_Alt_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 390);
            add(stmt, "Daily Double", BURGERS, 3.20, "Curious about what is in a Daily Double? It’s made with two 100% beef patties, seasoned to perfection, and melty American cheese topped off with shredded lettuce, slivered onions, mayo and two juicy slices of tomato.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202307_5101-999_DailyDouble_1564x1564?wid=1000&hei=1000&dpr=off", 490);
            add(stmt, "Bacon Quarter Pounder® with Cheese", BURGERS, 6.50, "Each Bacon Quarter Pounder® with Cheese burger features thick-cut applewood smoked bacon atop ¼ lb. of 100% McDonald's fresh beef that's cooked when you order. It's a hot, deliciously juicy bacon cheeseburger, seasoned with just a pinch of salt and pepper and sizzled on our flat iron grill. Layered with two slices of melty American cheese, slivered onions and tangy pickles on a soft, fluffy sesame seed hamburger bun.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202201_4295-005_BaconQPC_1564x1564-1?wid=1000&hei=1000&dpr=off", 630);
            add(stmt, "Cheeseburger", BURGERS, 1.90, "Enjoy the cheesy deliciousness of a McDonald’s Cheeseburger! Our simple, classic cheeseburger begins with a 100% pure beef burger patty seasoned with just a pinch of salt and pepper. The McDonald’s Cheeseburger is topped with a tangy pickle, chopped onions, ketchup, mustard, and a slice of melty American cheese. It contains no artificial flavors, preservatives or added colors from artificial sources. Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202302_0003-999_CheeseburgerAlt_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 300);
            add(stmt, "Double Cheeseburger", BURGERS, 3.10, "The McDonald's Double Cheeseburger features two 100% pure all beef patties seasoned with just a pinch of salt and pepper. It's topped with tangy pickles, chopped onions, ketchup, mustard, and two melty American cheese slices. Wondering what is the difference between McDouble and Double Cheeseburger? It's the extra slice of American cheese in the Double Cheeseburger.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202411_0004-999_DoubleCheeseburger_Alt_McValueRegistered_1564x1564?wid=1000&hei=1000&dpr=off", 440);
            add(stmt, "Hamburger: The Classic McDonald's Burger", BURGERS, 1.60, "The Classic McDonald's Hamburger starts with a 100% pure beef patty seasoned with just a pinch of salt and pepper. Then, the McDonald’s burger is topped with a tangy pickle, chopped onions, ketchup, and mustard. What's the difference between a Hamburger and a Cheeseburger, you ask? A slice of cheese in the latter! There are 250 calories in a McDonald’s Hamburger. It contains no artificial flavors, preservatives, or added colors from artificial sources.* Our pickle contains an artificial preservative, so skip it if you like.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202302_0001-999_Hamburger_Alt_1564x1564?wid=1000&hei=1000&dpr=off", 250);

            add(stmt, "McCrispy®", CHICKEN_AND_FISH_SANDWICHES, 5.20, "Yeah, this is the McDonald’s crispy chicken sandwich you were dreaming of. The McCrispy is a McDonald’s southern-style fried chicken sandwich that's crispy, juicy and tender perfection. It’s topped with crinkle-cut pickles and served on a toasted, buttered potato roll. If you’re thinking, “McCrispy vs. McChicken®?” you can’t go wrong either way.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202012_0383_CrispyChickenSandwich_PotatoBun_1564x1564-1?wid=1000&hei=1000&dpr=off", 470);
            add(stmt, "Deluxe McCrispy®", CHICKEN_AND_FISH_SANDWICHES, 6.10, "Go deluxe with a McDonald’s deluxe crispy chicken sandwich. Wondering what comes on the sandwich? The Deluxe McCrispy features a crispy chicken fillet with shredded lettuce, Roma tomatoes and mayo to take crispy, juicy and tender to the next level.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202012_0370_DeluxeCrispyChicken_PotatoBun_1564x1564-1?wid=1000&hei=1000&dpr=off", 530);
            add(stmt, "Spicy McCrispy®", CHICKEN_AND_FISH_SANDWICHES, 5.50, "Yeah, this is the spicy crispy chicken sandwich you were dreaming about. With a Spicy Pepper Sauce topping the southern style fried chicken fillet on a toasted potato roll, this sandwich was made for those who like it crispy, juicy, tender and hot.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202012_0116_SpicyCrispyChicken_PotatoBun_1564x1564-1?product-header-desktop&wid=1000&hei=1000&dpr=off", 530);
            add(stmt, "Spicy Deluxe McCrispy®", CHICKEN_AND_FISH_SANDWICHES, 6.40, "The Spicy Deluxe McCrispy Chicken sandwich is big on everything, including heat. It's a southern-style fried chicken fillet on a potato roll, topped with shredded lettuce, Roma tomatoes and Spicy Pepper Sauce to kick crispy, juicy and tender up to the highest level.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202104_0100_DeluxeSpicyCrispyChickenSandwich_PotatoBun_1564x1564-1?wid=1000&hei=1000&dpr=off", 530);
            add(stmt, "Filet-O-Fish®", CHICKEN_AND_FISH_SANDWICHES, 4.30, "Dive into our wild-caught Filet-O-Fish, a classic McDonald's fish sandwich! Our fish sandwich recipe features a crispy fish filet patty made with wild-caught Alaskan Pollock on melty American cheese and is topped with creamy McDonald’s tartar sauce, all served on a soft, steamed bun.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202503_5926-999_Filet-O-Fish_HalfSlice_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 380);
            add(stmt, "McChicken®", CHICKEN_AND_FISH_SANDWICHES, 3.80, "It’s a classic for a reason. Savor the satisfying crunch of our juicy chicken patty, topped with shredded lettuce and just the right amount of creamy mayonnaise, all served on a perfectly toasted bun.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202302_4314-999_McChicken_1564x1564-1?wid=1000&hei=1000&dpr=off", 390);

            add(stmt, "4 Piece Chicken McNuggets®", MC_NUGGETS_AND_MC_CRISPY_STRIPS, 2.50, "Enjoy tender, juicy Chicken McNuggets with your favorite dipping sauces. Wondering what are McDonald's Chicken Nuggets made of? Chicken McNuggets are made with all white meat chicken and no artificial colors, flavors, or preservatives.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202006_0483_4McNuggets_Stacked_1564x1564-1?wid=1000&hei=1000&dpr=off", 170);
            add(stmt, "McCrispy® Strips", MC_NUGGETS_AND_MC_CRISPY_STRIPS, 4.80, "McCrispy Strips are here! Crispy, juicy and peppery, McCrispy Strips are the next big thing in dipping. Coated with a golden-brown breading and delicious black pepper flavor, McCrispy Strips from McDonald’s are made with all white meat chicken.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202509_25157_3PieceMcCrispyStrips_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 350);

            add(stmt, "Ranch Snack Wrap®", SNACK_WRAP, 2.50, "The Ranch Snack Wrap is here to stay at McDonald’s. This fan favorite features a McCrispy® Strip topped with shredded cheese and shredded lettuce, all brought together with creamy Ranch sauce and wrapped in a soft tortilla.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202504_25254_RanchSnackWrap_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 390);
            add(stmt, "Spicy Snack Wrap®", SNACK_WRAP, 2.50, "The Snack Wrap is here to stay at McDonald’s and it’s hotter than ever. The Spicy Snack Wrap features a McCrispy® Strip topped with shredded cheese and shredded lettuce, all brought together with Spicy Pepper sauce and wrapped in a soft tortilla.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202504_25261_SpicySnackWrap_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 380);

            add(stmt, "Small World Famous Fries®", FRIES_AND_SIDES, 1.90, "Everyone wants to know why McDonald’s French Fries taste so good—it’s a simple answer. McDonald's World Famous Fries® are made with premium potatoes such as the Russet Burbank and the Shepody. With 0g of trans fat per labeled serving, these epic fries are crispy and golden on the outside and fluffy on the inside.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202408_6050_SmallFrenchFries_Standing_1564x1564?wid=1000&hei=1000&dpr=off", 230);
            add(stmt, "Apple Slices", FRIES_AND_SIDES, 1.50, "McDonald’s Apple Slices are a wholesome, tasty side made from real apples. Specially selected varieties mean our apple slices are always crisp and juicy, making for a tasty snack with 15 calories per labelled serving. Enjoy it as a Snack or Side to your meal!", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202002_2794_AppleSlices_NoBag_1564x1564?wid=1000&hei=1000&dpr=off", 15);


            add(stmt, "Hamburger Happy Meal®", HAPPY_MEAL, 3.99, "Enjoy a Hamburger meal from the McDonald’s Happy Meal menu that your kiddies are sure to love! Wondering what comes in a Happy Meal with Hamburger? McDonald's Hamburger Happy Meal includes a juicy McDonald's Hamburger with kid-sized World Famous Fries® and Apple Slices. Then pick a kid’s drink: 1% Low Fat Milk Jug, Reduced Sugar* Low Fat Chocolate Milk, DASANI® Water, or Honest Kids® Appley Ever After® Organic Juice Drink. Plus, a McDonald’s Happy Meal toy that completes every McDonald’s Kids meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202307_6975_HamburgerHappyMeal_AppleSlices_WhiteMilkJug_Left_1564x1564-1?wid=1000&hei=1000&dpr=off", 475);
            add(stmt, "4 Piece McNuggets® Happy Meal®", HAPPY_MEAL, 4.25, "Enjoy a 4 Piece Chicken McNuggets Happy Meal from the McDonald’s Happy Meal menu. Our Chicken McNugget Happy Meal features four tender Chicken McNuggets made with white meat, kid-sized World Famous Fries and a side of Apple Slices. Then pick a kids’ drink: 1% Low Fat Milk Jug, Reduced Sugar* Low Fat Chocolate Milk Jug, DASANI® Water or Honest Kids® Appley Ever After® Organic Juice Drink. Plus, a fun toy completes every McDonald's kid's meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202103_7002_4McNuggetsHappyMeal_AppleSlices_WhiteMilkJug_Left_Protein_1564x1564?wid=1000&hei=1000&dpr=off", 395);
            add(stmt, "6 Piece McNuggets® Happy Meal®", HAPPY_MEAL, 4.75, "Enjoy a McDonald’s Happy Meal and get six tender Chicken McNuggets made with white meat, a kid-sized World Famous Fries and a side of Apple Slices. Then pick a kids’ drink: 1% Low Fat Milk Jug, Reduced Sugar* Low Fat Chocolate Milk Jug, DASANI® Water or Honest Kids® Appley Ever After® Organic Juice Drink. Plus, every McDonald’s kids’ meal comes with a McDonald’s Happy Meal toy!", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202103_7780_6McNuggetsHappyMeal_AppleSlices_WhiteMilkJug_Left_1564x1564-1?wid=1000&hei=1000&dpr=off", 475);

            add(stmt, "Derpy McFlurry®", SWEET_AND_TREATS, 4.20, "Inspired by KPop Demon Hunters, the Derpy McFlurry blends vanilla soft serve with popping pearls and a sweet wild berry sauce, creating a dessert that’s smooth, bright and packed with little pops of fun. But be careful, because for a limited time, having one may cause spontaneous tongue-out selfies, Derpy style.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202602_25655_Q2BRBerryPoppingPearlsMcFlurry_1564x1564?wid=1000&hei=1000&dpr=off", 400);
            add(stmt, "OREO® McFlurry®", SWEET_AND_TREATS, 3.80, "It's your cookies and cream dream—a classic combo made with creamy vanilla soft serve and crunchy pieces of OREO® cookies mixed in.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202002_3832_OREOMcFlurry_1564x1564-1?wid=1000&hei=1000&dpr=off", 410);
            add(stmt, "M&M'S® McFlurry®", SWEET_AND_TREATS, 3.80, "Taking creamy vanilla soft serve to the next level means swirling in crunchy M&M’S® candies—so naturally, it’s going to be the perfectly sweet addition to any McDonald's meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202505_3830_MandMMcFlurry_1564x1564?wid=1000&hei=1000&dpr=off", 570);
            add(stmt, "Vanilla Cone", SWEET_AND_TREATS, 1.50, "Treat yourself to a delicious Vanilla Cone Treat from McDonald’s! Our Vanilla Cone features creamy vanilla soft serve in a crispy cone. It's the perfect sweet treat in addition to any McDonald's meal or on its own.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202106_0336_LargeVanillaCone_1564x1564?wid=1000&hei=1000&dpr=off", 200);
            add(stmt, "Chocolate Shake", SWEET_AND_TREATS, 3.50, "Looking for the perfect sweet treat for any time of day? It’s time to sip on a perfectly crafted McDonald’s Chocolate Shake—made with creamy soft serve & chocolate syrup! Our chocolate shake recipe features delicious soft serve and chocolate syrup, finished with whipped light cream.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201907_1509_MediumChocolateShake_Glass_A1_1564x1564?wid=1000&hei=1000&dpr=off", 520);
            add(stmt, "Vanilla Shake", SWEET_AND_TREATS, 3.50, "Sip on a delicious Vanilla Shake from McDonald's—a smooth, rich and creamy treat for any time of day! McDonald's Vanilla Shake combines creamy vanilla soft serve and vanilla syrup finished with whipped light cream on top for a cool, tasty treat.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201907_1598_MediumVanillaShake_Glass_A1_1564x1564-1?wid=1000&hei=1000&dpr=off", 480);
            add(stmt, "Strawberry Shake", SWEET_AND_TREATS, 3.50, "Looking for a perfect sweet treat for any time of day? A Strawberry Shake is calling your name. The McDonald's Strawberry Shake features creamy soft serve, blended with strawberry syrup and finished with whipped light cream.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201907_1513_MediumStrawberryShake_Glass_A1_1564x1564-1?wid=1000&hei=1000&dpr=off", 470);
            add(stmt, "Hot Fudge Sundae", SWEET_AND_TREATS, 2.80, "Treat yourself to a delicious Hot Fudge Sundae from McDonald’s! Our classic Hot Fudge Sundae is made with creamy vanilla soft serve and smothered in chocolatey hot fudge topping. It’s a perfectly sweet addition to any of your favorite McDonald’s items.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201907_0337_HotFudgeSundae_1564x1564?wid=1000&hei=1000&dpr=off", 330);
            add(stmt, "Hot Caramel Sundae", SWEET_AND_TREATS, 2.80, "Treat yourself to a Hot Caramel Sundae at McDonald's! This Caramel Sundae combines creamy vanilla soft serve and warm, buttery caramel topping.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201907_0345_CaramelSundae_1564x1564?wid=1000&hei=1000&dpr=off", 330);
            add(stmt, "Baked Apple Pie", SWEET_AND_TREATS, 1.70, "McDonald's Baked Apple Pie recipe features 100% American-grown apples and a lattice crust baked to perfection, topped with sprinkled sugar.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202004_0706_BakedApplePie_Broken_1564x1564-1?wid=1000&hei=1000&dpr=off", 230);
            add(stmt, "Chocolate Chip Cookie", SWEET_AND_TREATS, 1.20, "Enjoy a warm & tasty Chocolate Chip Cookie from McDonald's! An amazingly delicious, soft and chewy Chocolate Chip Cookie—our Chocolate Chip Cookie recipe features a perfectly warm, soft baked cookie loaded with gooey chocolate chips. Enjoy it on its own as a snack or pair it with your favorite McDonald's meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202211_1852_ChocolateChipCookie_Upright_1564x1564-1?wid=1000&hei=1000&dpr=off", 170);

            add(stmt, "McCafé® Caramel Macchiato", MC_CAFE_COFFEES, 3.90, "Ever wondered what's in a McCafé Caramel Macchiato from McDonald's? Caramel Macchiato combines bold, rich espresso and buttery caramel flavor served with steamed whole milk.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2804_MediumCaramelMacchiato_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 260);
            add(stmt, "McCafé® Cappuccino", MC_CAFE_COFFEES, 3.50, "Warm up with a McCafé Cappuccino—made with rich, sustainably sourced espresso and steamed milk. The ingredients are the same as that of a McCafé® Latte but the main difference between a latte and a cappuccino is that a cappuccino is distinctly layered and has a slightly stronger flavor than the latte.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2692_MediumCappuccino_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 110);
            add(stmt, "McCafé® Caramel Cappuccino", MC_CAFE_COFFEES, 3.90, "Looking for a sweet pick-me-up? The McCafé Caramel Cappuccino recipe features whole steamed milk, bold espresso made from sustainably-sourced beans, fluffy foam and a rich, buttery caramel flavor.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1517_MediumCaramelCappuccino_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 210);
            add(stmt, "McCafé® French Vanilla Cappuccino", MC_CAFE_COFFEES, 3.90, "The McCafé French Vanilla Cappuccino recipe blends bold espresso made from sustainably-sourced beans, steamed milk, fluffy foam, and rich French Vanilla flavor.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1372_MediumVanillaCappuccino_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 210);
            add(stmt, "McCafé® Mocha Latte", MC_CAFE_COFFEES, 3.70, "Enjoy a delicious, hot Mocha Latte from McDonald’s. Wondering what’s in a Mocha Latte? A McCafé Mocha Latte is made with espresso beans that are sustainably sourced from Rainforest Alliance Certified™ farms, steamed whole milk, rich chocolate syrup and whipped light cream and chocolate drizzle.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2731_MediumMocha_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 290);
            add(stmt, "Americano", MC_CAFE_COFFEES, 2.80, "Start your mornings with a bold McCafé® Americano coffee that pairs perfectly with our breakfast menu items. Wondering what’s in an Americano from McDonald’s? Our simple yet satisfying Americano is made with hot water poured over our Rainforest Alliance Certified™ espresso.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_201906_1318_MediumAmericano_Glass_A1_HL_1564x1564?wid=1000&hei=1000&dpr=off", 0);
            add(stmt, "McCafé® Premium Roast Coffee", MC_CAFE_COFFEES, 1.50, "It’s time to treat yourself. The simple and satisfying McCafé Premium Roast Coffee is made with expertly roasted 100% Arabica coffee beans and freshly brewed every 30 minutes. Enjoy your hot coffee black or with your choice of sugar, sweetener and dairy or creamer.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_0321_MediumCoffee_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 5);
            add(stmt, "McCafé® Iced Caramel Macchiato", MC_CAFE_COFFEES, 4.20, "McCafé Iced Caramel Macchiato is the chilled equivalent of the McCafé Caramel Macchiato. The McDonald's Iced Caramel Macchiato recipe features bold, rich and dark-roasted espresso and buttery caramel syrup with extra caramel drizzle. Served with whole milk, it’s almost similar to an Iced Caramel Latte but with an extra buttery caramel drizzle.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201906_2743_MediumIcedCaramelMacchiato_Glass_A1_832x472:product-header-desktop?wid=830&hei=458&dpr=off", 200);
            add(stmt, "McCafé® Iced Mocha Latte", MC_CAFE_COFFEES, 4.00, "Featuring sustainably sourced espresso beans from Rainforest Alliance Certified™ farms, the refreshingly cool Iced Mocha latte is made with whole milk, chocolate syrup, and topped with whipped light cream and chocolate drizzle. Refresh and relax with this iced mocha treat full of chocolatey flavor and rich creaminess.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201906_7659_MediumIcedMocha_Glass_A1_832x472:product-header-desktop?wid=830&hei=456&dpr=off", 270);
            add(stmt, "McCafé® Iced Coffee", MC_CAFE_COFFEES, 2.20, "Enjoy a refreshingly cool Iced Coffee at McDonald’s! The McCafé Iced Coffee features 100% Arabica beans, cream and your choice of flavor like Caramel, French Vanilla and Sugar-Free French Vanilla.* A fan of black coffee? Get it without the sugar or cream to refresh your mornings with a cool Black Iced Coffee.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1212_MediumIcedCoffee_1564x1564-1?wid=1000&hei=1000&dpr=off", 150);
            add(stmt, "McCafé® Iced Caramel Coffee", MC_CAFE_COFFEES, 2.50, "Cool off with a refreshingly cool Iced Caramel Coffee at McDonald’s. The Iced Caramel Coffee recipe features premium-roast coffee, buttery caramel syrup and cream.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1207_MediumIcedCaramelCoffee_1564x1564-1?wid=1000&hei=1000&dpr=off", 150);
            add(stmt, "McCafé® Iced French Vanilla Coffee", MC_CAFE_COFFEES, 2.50, "Morning is calling and here’s an answer—French Vanilla Iced Coffee. Made with premium roast beans, smooth French vanilla flavor and 100% Arabica beans, McCafé® French Vanilla Iced Coffee is the perfect cold coffee to kickstart your day.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1216_MediumIcedFrenchVanillaCoffee_1564x1564-1?wid=1000&hei=1000&dpr=off", 150);
            add(stmt, "McCafé® Latte", MC_CAFE_COFFEES, 3.20, "Want to know what's in a Latte from McDonald's? It’s made from Rainforest Alliance Certified™ espresso and steamed milk. Customize the hot Latte that’s made fresh just for you with whole milk, mixed with your choice of flavor at certain locations.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2698_MediumLatte_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 140);
            add(stmt, "McCafé® Iced Latte", MC_CAFE_COFFEES, 3.50, "What’s in an iced latte? This one is made from Rainforest Alliance Certified™ espresso and steamed milk. To customize the Latte that’s made fresh just for you with whole milk, mix in your choice of flavor at certain locations.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_7606_MediumIcedLatte_1564x1564-1?wid=1000&hei=1000&dpr=off", 80);
            add(stmt, "McCafé® Caramel Latte", MC_CAFE_COFFEES, 3.70, "Take a break and sip on a McCafé® Caramel Latte—made with Rainforest Alliance Certified™ espresso, buttery caramel syrup and milk. The added caramel syrup makes it slightly sweeter than a regular McCafé® Latte.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1324_MediumCaramelLatte_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 250);
            add(stmt, "McCafé® Iced Caramel Latte", MC_CAFE_COFFEES, 4.00, "Cool off with a refreshing McDonald’s Iced Caramel Latte. If you want to know what’s in an Iced Caramel Latte, keep reading. It features Rainforest Alliance Certified™ espresso and buttery caramel flavor.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_0379_MediumIcedCaramelLatte_1564x1564-1?wid=1000&hei=1000&dpr=off", 180);
            add(stmt, "McCafé® French Vanilla Latte", MC_CAFE_COFFEES, 3.70, "Warm up with a rich, creamy McCafé French Vanilla Latte! Wondering what’s in a Vanilla Latte from McDonald’s? It features Rainforest Alliance Certified™ espresso, steamed whole milk and is infused with a smooth French Vanilla syrup.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1331_MediumFrenchVanillaLatte_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 250);
            add(stmt, "McCafé® Iced French Vanilla Latte", MC_CAFE_COFFEES, 4.00, "Cool down with a McCafé® Iced French Vanilla Latte, made with Rainforest Alliance Certified™ espresso. It features bold espresso, whole milk and sweet French Vanilla syrup.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_0187_MediumIcedFrenchVanillaLatte_1564x1564-1?wid=1000&hei=1000&dpr=off", 180);
            add(stmt, "McCafé® Caramel Frappé", MC_CAFE_COFFEES, 4.50, "The McCafé Caramel Frappé recipe blends rich caramel flavor with a hint of coffee, blended with ice and is topped with whipped light cream.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2842_MediumCaramelFrappe_1564x1564-1?wid=1000&hei=1000&dpr=off", 420);
            add(stmt, "McCafé® Mocha Frappé", MC_CAFE_COFFEES, 4.50, "Wondering what’s in a Mocha Frappé? McCafé Mocha Frappé blends rich chocolate flavor with a hint of coffee topped with light cream.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_3491_MediumMochaFrappe_1564x1564-1?wid=1000&hei=1000&dpr=off", 430);


            add(stmt, "Coca-Cola®", BEVERAGES, 1.50, "Enjoy a cold, refreshing Coca-Cola® soda from McDonald's that complements all your menu favorites.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202402_0521_MediumCoke_ContourGlassv1_1564x1564?wid=1000&hei=1000&dpr=off", 200);
            add(stmt, "Sprite®", BEVERAGES, 1.50, "Cool off with the refreshing McDonald's Sprite®—the classic and delicious lemon-lime fountain drink. Now, you might be asking does Sprite® have caffeine? No, Sprite® is a caffeine-free soda that makes the perfect addition to any McDonald’s Combo Meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202212_0721_MediumSprite_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 190);
            add(stmt, "Dr Pepper®", BEVERAGES, 1.50, "McDonald's serves Dr Pepper®, the classic and refreshing fountain drink with a unique blend of 23 flavors. Dr Pepper® pairs perfectly with any of your favorite menu items.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201905_0421_MediumDrPepper_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 190);
            add(stmt, "Fanta® Orange", BEVERAGES, 1.50, "Sip on the bubbly, refreshing orange flavor of Fanta® Orange soda from McDonald’s! Is McDonald’s Fanta® Orange caffeine-free? It sure is, making it a perfect addition to any of your favorite McDonald's items.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202212_1262_MediumFantaOrange_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 210);
            add(stmt, "Diet Coke®", BEVERAGES, 1.50, "Diet Coke fixes everything. That’s why it’s a staple. Wondering why it tastes so good at McDonald’s? The Coke® products are fresh, and the guidelines set by Coca-Cola® are always followed by using a ratio of syrup that allows ice to melt—plus the water and Coca-Cola® syrup are pre-chilled before they enter the soda fountain.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202112_0652_MediumDietCoke_Glass_1564x1564-1?wid=1000&hei=1000&dpr=off", 0);
            add(stmt, "Hi-C® Orange Lavaburst®", BEVERAGES, 1.50, "Hi-C Orange Lavaburst is a must. With a fruity flavor that teases your tastebuds, McDonald's orange drink is the perfect addition to any McDonald’s meal.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202012_0621_MediumHi-COrange_1564x1564-1?wid=1000&hei=1000&dpr=off", 220);
            add(stmt, "Frozen Fanta® Blue Raspberry", BEVERAGES, 2.80, "Sweet, fruity raspberry flavor with a hint of tartness for a super cool refreshing treat that’s perfect for the summer. Pair it with our World Famous Fries® for a satisfying snack, or make it a meal with your favorite McDonald’s Burgers.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202301_7959_MediumFrozenFantaBlueRaspberry_Glass_1564x1564?wid=1000&hei=1000&dpr=off", 60);
            add(stmt, "Frozen Coca-Cola® Classic", BEVERAGES, 2.80, "The great taste of Coca-Cola® in a refreshing frozen drink. It’s the perfect partner for your favorite McDonald’s Burgers and World Famous Fries®.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202301_7963_MediumFrozenCocaColaClassic_Glass_1564x1564?wid=1000&hei=1000&dpr=off", 60);
            add(stmt, "Strawberry Banana Smoothie", BEVERAGES, 3.90, "The McCafé® Strawberry Banana Smoothie recipe features the perfect combination of real strawberry and banana fruit purees and juices, blended with creamy low fat yogurt and ice.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2725_MediumStrawberryBananaSmoothie_1564x1564-1?wid=1000&hei=1000&dpr=off", 190);
            add(stmt, "Mango Pineapple Smoothie", BEVERAGES, 3.90, "Enjoy a refreshing Mango Pineapple Smoothie from McDonald's! Wondering what’s in it? Our Mango Pineapple Smoothie recipe features a sweet combination of fruit juices and purees such as mango and pineapple, blended with creamy low-fat yogurt and ice.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_2790_MediumMangoPineappleSmoothie_1564x1564-1?wid=1000&hei=1000&dpr=off", 200);
            add(stmt, "Lemonade", BEVERAGES, 2.20, "Squeeze a little bit of summer in every sip with this refreshing, ice-cold classic. McDonald’s signature Lemonade recipe includes real lemon juice, bits of lemon pulp and real cane sugar. There are 120 calories in a small Lemonade drink.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202511_14878_Medium_SignatureLemonade_1564x1564v2?wid=1000&hei=1000&dpr=off", 120);
            add(stmt, "Sweet Tea", BEVERAGES, 1.50, "Enjoy a refreshing, icy cold Sweet Tea from McDonald's. Wondering what’s in Sweet Tea? McDonald’s Sweet Tea recipe features a briskly refreshing blend of orange pekoe and pekoe cut black tea, sweetened to perfection. It’s tea in it’s greatest form—a McDonald’s cup.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202105_3429_SweetTea_Glass_A1_1564x1564-1?wid=1000&hei=1000&dpr=off", 170);
            add(stmt, "Unsweetened Iced Tea", BEVERAGES, 1.50, "McDonald’s Iced Tea recipe features a brisk blend of orange pekoe black tea, freshly brewed and served ice cold. Wondering if there are any calories in Unsweet Tea from McDonald’s? Unsweetened Iced Tea is a zero-calorie, sugar-free iced tea that pairs perfectly with your menu favorites.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_201909_9449_UnsweetTea_Glass_A1_1564x1564-1?wid=1000&hei=1000&dpr=off", 0);
            add(stmt, "Hot Tea", BEVERAGES, 1.80, "Refresh your mornings with the best Hot Tea at McDonald's. Wondering what tea does McDonald's use? It’s a flavorful mix of orange pekoe & pekoe cut black tea.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_0573_MediumHotTea_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 10);
            add(stmt, "Minute Maid® Premium Orange Juice", BEVERAGES, 2.50, "Try refreshing Minute Maid® Premium Orange Juice at McDonald's with 100% Orange Juice. It contains pure Orange Juice and the goodness of Vitamin C. Pair it with your favorite McDonald’s Breakfast Menu item to give a fresh start to your day.", "https://s7d1.scene7.com/is/image/mcdonalds/Header_MediumMinuteMaidPremiumOrangeJuice_832x472:product-header-desktop?wid=830&hei=458&dpr=off", 190);
            add(stmt, "Honest Kids® Appley Ever After® Organic Juice Drink", BEVERAGES, 1.90, "Enjoy Honest Kids* Appley Ever After Organic Juice Drink from McDonald’s, an organic drink perfect for a kids meal! Honest Kids Juice drink is an organic apple juice sweetened only with fruit juice.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202008_0322_HonestKidsAppleJuice_1564x1564?wid=1000&hei=1000&dpr=off", 35);
            add(stmt, "1% Low Fat Milk Jug", BEVERAGES, 1.70, "Enjoy a refreshing 1% Low Fat Milk Jug from McDonald’s—an excellent source of calcium and a good source of vitamins A & D! McDonald's Milk Jug is a low fat milk that can be added to your Happy Meal® as a drink option.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202103_0066_MilkJug_1564x1564?wid=1000&hei=1000&dpr=off", 100);
            add(stmt, "Reduced Sugar Low Fat Chocolate Milk Jug", BEVERAGES, 1.70, "Enjoy a Reduced Sugar Low Fat Chocolate Milk Jug with the goodness of Calcium and Vitamins A & D. McDonald’s Low Fat Chocolate Milk recipe is full of low-fat chocolate milk and is a perfect addition to your Happy Meal®.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202103_3179_ReducedSugarLowFatChocolateMilk_1564x1564-1?wid=1000&hei=1000&dpr=off", 130);
            add(stmt, "McCafé® Hot Chocolate", BEVERAGES, 2.90, "Taste the delicious chocolatey flavor of McCafé® Hot Chocolate. McDonald’s Hot Chocolate recipe features steamed whole milk with rich hot chocolate syrup and is finished with whipped light cream and chocolate drizzle.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_202601_1697_MediumPremiumHotChocolate_HL_1564x1564-1?wid=1000&hei=1000&dpr=off", 360);
            add(stmt, "DASANI® Water", BEVERAGES, 1.50, "Satisfy your thirst with the cool and refreshing DASANI purified water! DASANI water is purified water enhanced with minerals for a pure, fresh taste.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_202402_5474_DasaniBottledWater_1564x1564:nutrition-calculator-tile?wid=600&hei=600&dpr=off", 0);

            add(stmt, "Hunter Sauce", SAUCES_AND_CONDIMENTS, 0.50, "Hunter Sauce is a sweet sauce that provides a slight kick, blending high notes of chili, garlic and pepper. It's perfect for dipping, dunking, drizzling (and dancing) with all of your favorites for a limited time.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202602_03248-059__25660_Q2BRSweetChiliSauce_1564x1564?wid=1000&hei=1000&dpr=off", 50);
            add(stmt, "Demon Sauce", SAUCES_AND_CONDIMENTS, 0.50, "Dip into Demon Sauce, a bold purple sauce with heat and tang that brings main-stage hot mustard flavor out. It's only here for a limited time, so don't let your favorites miss out on the show.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202602_16045-014__25661_Q2BRPurpleCajunSauce_1564x1564?wid=1000&hei=1000&dpr=off", 130);
            add(stmt, "Creamy Chili McCrispy® Strip Dip", SAUCES_AND_CONDIMENTS, 0.60, "Creamy Chili Dip is the newest addition to the tasty lineup of McDonald’s sauces. Savory, slightly tangy, sweet with a hint of chili pepper heat and a nutty toasted sesame finish—this dip is the perfect way to dip your McCrispy Strips, World Famous Fries® or anything else that you find dippable.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202504_03248-072__25195_CreamyChiliSauce_1564x1564?wid=1000&hei=1000&dpr=off", 110);
            add(stmt, "Tangy Barbeque Sauce", SAUCES_AND_CONDIMENTS, 0.40, "McDonald's Tangy Barbeque Sauce is sweet, tangy, and has a smoky flavor. Our Tangy BBQ Sauce recipe is made with a tomato paste base, vinegar, and savory spices with a hint of sweet hickory smoke flavor. It’s the perfect BBQ dipping sauce for Chicken McNuggets® and our World Famous Fries®.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_Ingredient_Condiment_202312_00408-144__0900_TangyBBQ_1564x1564?wid=1000&hei=1000&dpr=off", 45);
            add(stmt, "Spicy Buffalo Sauce", SAUCES_AND_CONDIMENTS, 0.40, "Spice it up with the Spicy Buffalo Sauce from McDonald’s. Wondering what our buffalo sauce is made of? McDonald's Spicy Buffalo Sauce recipe fuses is a peppery sauce with vinegar and butter flavor. With a building heat, this buffalo dipping sauce has the perfect amount of spice to complement your McDonald's Chicken McNuggets!", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_Ingredient_Condiment_202203_07812-045__9089_SpicyBuffalo_1564x1564?wid=1000&hei=1000&dpr=off", 30);
            add(stmt, "Creamy Ranch Sauce", SAUCES_AND_CONDIMENTS, 0.40, "You can’t go wrong with our Creamy Ranch Sauce—a zesty and dip-ready ranch sauce from McDonald’s! Our Ranch Sauce recipe fuses the flavors of onion and garlic to create a perfectly creamy dipping sauce! Try our ranch dip with your favorite McDonald's menu items, like Chicken McNuggets® or World Famous Fries®!", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_Ingredient_Condiment_202203_02861-036__0922_CreamyRanch_1564x1564?wid=1000&hei=1000&dpr=off", 110);
            add(stmt, "Honey Mustard Sauce", SAUCES_AND_CONDIMENTS, 0.40, "Enjoy the sweet-tangy combination of McDonald’s Honey Mustard dipping sauce! Wondering what Honey Mustard sauce is made of? Our Honey Mustard Sauce recipe is a unique blend of zesty dijon mustard with sweet notes of honey and a hint of spices. This blend makes a delicious honey mustard dipping sauce for Chicken McNuggets®.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202203_08731-024__9088_HoneyMustard_1564x1564-1?wid=1000&hei=1000&dpr=off", 60);
            add(stmt, "Sweet 'N Sour Sauce", SAUCES_AND_CONDIMENTS, 0.40, "Try our Sweet 'N Sour dipping sauce for a unique sweet and sour mix. Wondering what’s in the sweet and sour sauce from McDonald’s? McDonald's Sweet 'N Sour sauce recipe blends flavors of apricot and peach with savory spices and leaves a slight lingering heat. This is the perfect sweet and sour sauce for dipping Chicken McNuggets® in when you want something slightly sweet with a touch of heat.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_Ingredient_Condiment_202203_00409-120__0901_SweetNSour_1564x1564?wid=1000&hei=1000&dpr=off", 50);
            add(stmt, "Ketchup Packet", SAUCES_AND_CONDIMENTS, 0.00, "If you’re wondering what ketchup does McDonald’s use, the answer might surprise you— McDonald’s tomato ketchup! It’s perfect on your favorite sandwich or as a ketchup dipping sauce for your World Famous Fries®.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202203_02679-243__0912_Ketchup_1564x1564-1?wid=1000&hei=1000&dpr=off", 10);
            add(stmt, "Mayonnaise Packet", SAUCES_AND_CONDIMENTS, 0.00, "What mayo does McDonald’s use? Why, McDonald’s mayo, of course! Enjoy some extra mayo on your sandwich or maybe a mayonnaise dipping sauce for your World Famous Fries®.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202203_11668-000__9008_Mayonnaise_1564x1564-1?wid=1000&hei=1000&dpr=off", 90);
            add(stmt, "Mustard Packet", SAUCES_AND_CONDIMENTS, 0.00, "Add a little something-something to your favorites with mustard sauce.", "https://s7d1.scene7.com/is/image/mcdonalds/DC_Ingredient_Condiment_202203_00046-048__9004_Mustard_1564x1564-1?wid=1000&hei=1000&dpr=off", 0);
            add(stmt, "Honey", SAUCES_AND_CONDIMENTS, 0.40, "McDonald's Honey is Grade A pure, delicious, and simple. Our honey makes the perfect Chicken McNuggets® dipping sauce.", "https://s7d1.scene7.com/is/image/mcdonaldsstage/DC_Ingredient_Condiment_202312_00411-012__0902_Honey_1564x1564?wid=1000&hei=1000&dpr=off", 50);


            System.out.println("Default menu items created successfully.");
        }

    }

    private void add(PreparedStatement pstmt,
                     String name,
                     MenuItem.MenuCategory category,
                     double price,
                     @Nullable String description,
                     @Nullable String image_path,
                     @Nullable Integer calories
    ) throws SQLException {
        add(pstmt, name, category, price, rand.nextInt(25, 75), description, image_path, calories);
    }

    private void add(PreparedStatement pstmt,
                     String name,
                     MenuItem.MenuCategory category,
                     double price,
                     int stock,
                     @Nullable String description,
                     @Nullable String image_path,
                     @Nullable Integer calories) throws SQLException {
        pstmt.setString(1, name);
        pstmt.setString(2, category.name());
        pstmt.setDouble(3, price);
        pstmt.setInt(4, stock);

        // safely set null to prevent garbage data
        if (description != null) pstmt.setString(5, description);
        else pstmt.setNull(5, Types.VARCHAR);

        if (image_path != null) pstmt.setString(6, image_path);
        else pstmt.setNull(6, Types.VARCHAR);

        if (calories != null) pstmt.setInt(7, calories);
        else pstmt.setNull(7, Types.INTEGER);

        pstmt.executeUpdate();
    }
}