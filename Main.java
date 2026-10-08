import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Main User Interface (UI) class for the Expense Tracker CLI Application.
 * Handles menu navigation, user interaction, input formatting, and robust
 * exception/error handling to prevent runtime crashes.
 */
public class Main {
    private static final String EXPENSES_FILE = "expenses.csv";
    private static final String FUNDS_FILE = "person_funds.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExpenseManager manager;
    private final Scanner scanner;

    public Main() {
        this.manager = new ExpenseManager();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        printBanner();

        // Try loading existing saved data on startup automatically
        try {
            manager.loadData(EXPENSES_FILE, FUNDS_FILE);
        } catch (ExpenseManagerException e) {
            System.out.println("ℹ️  No previous data loaded. Starting with a fresh session.");
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntInput("Select an option (0-9): ", 0, 9);
            System.out.println();

            switch (choice) {
                case 1:
                    handleAddExpense();
                    break;
                case 2:
                    handleViewAllExpenses();
                    break;
                case 3:
                    handleCategoryWiseSpending();
                    break;
                case 4:
                    handleFilteringMenu();
                    break;
                case 5:
                    handlePersonFundsMenu();
                    break;
                case 6:
                    handleDeleteExpense();
                    break;
                case 7:
                    handleSaveData();
                    break;
                case 8:
                    handleLoadData();
                    break;
                case 9:
                    loadSampleData();
                    break;
                case 0:
                    handleSaveData();
                    System.out.println("👋 Thank you for using Expense Tracker! Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
            System.out.println();
        }
    }

    // --- MENU HANDLERS ---

    private void printBanner() {
        System.out.println("==========================================================================");
        System.out.println("                  💰 JAVA OOP EXPENSE TRACKER 💰                          ");
        System.out.println("     Demonstrating OOP, Encapsulation, Category Spending & Person Funds   ");
        System.out.println("==========================================================================");
    }

    private void printMainMenu() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("                             MAIN MENU                                    ");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("1. ➕ Add New Expense");
        System.out.println("2. 📋 View All Expenses");
        System.out.println("3. 📊 View Category-Wise Spending Breakdown");
        System.out.println("4. 🔍 Filter Expenses (by Person, Category, Date, Amount)");
        System.out.println("5. 🏦 Person Funds (Monthly Savings & Emergency Fund)");
        System.out.println("6. 🗑️ Delete Expense");
        System.out.println("7. 💾 Save Data to File");
        System.out.println("8. 📂 Load Data from File");
        System.out.println("9. ⚡ Load Sample Demo Data");
        System.out.println("0. 🚪 Save & Exit");
        System.out.println("--------------------------------------------------------------------------");
    }

    private void handleAddExpense() {
        System.out.println("--- ➕ ADD NEW EXPENSE ---");
        try {
            String personName = readNonEmptyString("Enter Person Name (e.g. Alice, Bob): ");
            String description = readNonEmptyString("Enter Expense Description: ");
            double amount = readPositiveDouble("Enter Amount ($): ");
            Category category = selectCategoryPrompt();
            LocalDate date = readDateInput("Enter Date (yyyy-MM-dd) [Press Enter for Today]: ", true);

            Expense expense = new Expense(description, amount, category, date, personName);
            manager.addExpense(expense);

            System.out.println("✅ Expense added successfully!");
            System.out.println("   " + expense);
        } catch (IllegalArgumentException | ExpenseManagerException e) {
            System.out.println("❌ Error adding expense: " + e.getMessage());
        }
    }

    private void handleViewAllExpenses() {
        System.out.println("--- 📋 ALL EXPENSES ---");
        List<Expense> list = manager.getAllExpenses();
        displayExpenseList(list);
        if (!list.isEmpty()) {
            System.out.printf("👉 Grand Total Spending: $%.2f%n", manager.getTotalSpending());
        }
    }

    private void handleCategoryWiseSpending() {
        System.out.println("--- 📊 CATEGORY-WISE SPENDING BREAKDOWN ---");
        System.out.println("1. View overall spending by category");
        System.out.println("2. View spending by category for a specific person");
        int subChoice = readIntInput("Select option (1-2): ", 1, 2);

        Map<Category, Double> categoryMap;
        if (subChoice == 1) {
            categoryMap = manager.getCategoryWiseSpending();
            System.out.println("\n--- Overall Category Breakdown ---");
        } else {
            String personName = readNonEmptyString("Enter Person Name: ");
            categoryMap = manager.getCategoryWiseSpendingByPerson(personName);
            System.out.println("\n--- Category Breakdown for " + personName + " ---");
        }

        double total = categoryMap.values().stream().mapToDouble(Double::doubleValue).sum();

        System.out.printf("%-25s | %-12s | %-12s%n", "Category", "Spent ($)", "Percentage");
        System.out.println("----------------------------------------------------------");
        for (Map.Entry<Category, Double> entry : categoryMap.entrySet()) {
            double amount = entry.getValue();
            double percent = total > 0 ? (amount / total) * 100 : 0.0;
            System.out.printf("%-25s | $%-11.2f | %6.1f%%%n", entry.getKey().getDisplayName(), amount, percent);
        }
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-25s | $%-11.2f | %6.1f%%%n", "TOTAL", total, total > 0 ? 100.0 : 0.0);
    }

    private void handleFilteringMenu() {
        System.out.println("--- 🔍 FILTER EXPENSES ---");
        System.out.println("1. Filter by Category");
        System.out.println("2. Filter by Person");
        System.out.println("3. Filter by Date Range");
        System.out.println("4. Filter by Month and Year");
        System.out.println("5. Filter by Amount Range");
        int subChoice = readIntInput("Select filter type (1-5): ", 1, 5);

        try {
            List<Expense> filteredResults = new ArrayList<>();
            switch (subChoice) {
                case 1:
                    Category cat = selectCategoryPrompt();
                    filteredResults = manager.filterByCategory(cat);
                    break;
                case 2:
                    String person = readNonEmptyString("Enter Person Name: ");
                    filteredResults = manager.filterByPerson(person);
                    break;
                case 3:
                    LocalDate start = readDateInput("Enter Start Date (yyyy-MM-dd): ", false);
                    LocalDate end = readDateInput("Enter End Date (yyyy-MM-dd): ", false);
                    filteredResults = manager.filterByDateRange(start, end);
                    break;
                case 4:
                    int year = readIntInput("Enter Year (e.g. 2026): ", 1900, 2100);
                    int month = readIntInput("Enter Month (1-12): ", 1, 12);
                    filteredResults = manager.filterByMonth(year, month);
                    break;
                case 5:
                    double min = readPositiveDouble("Enter Minimum Amount ($): ");
                    double max = readPositiveDouble("Enter Maximum Amount ($): ");
                    filteredResults = manager.filterByAmountRange(min, max);
                    break;
            }

            System.out.println("\n--- Filter Results (" + filteredResults.size() + " matches) ---");
            displayExpenseList(filteredResults);

        } catch (ExpenseManagerException e) {
            System.out.println("❌ Filter error: " + e.getMessage());
        }
    }

    private void handlePersonFundsMenu() {
        System.out.println("--- 🏦 PERSON FUNDS (Monthly Savings & Emergency Funds) ---");
        System.out.println("1. 👤 View Person Financial Accounts");
        System.out.println("2. 💵 Deposit Monthly Savings for a Person");
        System.out.println("3. 🚨 Deposit to Emergency Fund for a Person");
        System.out.println("4. ⚠️ Withdraw Emergency Money for a Person");
        int subChoice = readIntInput("Select option (1-4): ", 1, 4);

        try {
            switch (subChoice) {
                case 1:
                    Collection<PersonFund> funds = manager.getAllPersonFunds();
                    if (funds.isEmpty()) {
                        System.out.println("ℹ️  No person funds registered yet.");
                    } else {
                        System.out.println("\n--------------------------------------------------------------------------");
                        for (PersonFund pf : funds) {
                            System.out.println("  " + pf);
                        }
                        System.out.println("--------------------------------------------------------------------------");
                    }
                    break;
                case 2:
                    String p1 = readNonEmptyString("Enter Person Name: ");
                    double sAmount = readPositiveDouble("Enter Monthly Savings Deposit Amount ($): ");
                    manager.depositMonthlySavings(p1, sAmount);
                    System.out.println("✅ Successfully added $" + String.format("%.2f", sAmount) + " to Monthly Savings for " + p1);
                    break;
                case 3:
                    String p2 = readNonEmptyString("Enter Person Name: ");
                    double eAmount = readPositiveDouble("Enter Emergency Fund Deposit Amount ($): ");
                    manager.depositEmergencyFund(p2, eAmount);
                    System.out.println("✅ Successfully deposited $" + String.format("%.2f", eAmount) + " to Emergency Fund for " + p2);
                    break;
                case 4:
                    String p3 = readNonEmptyString("Enter Person Name: ");
                    double wAmount = readPositiveDouble("Enter Emergency Fund Withdrawal Amount ($): ");
                    String reason = readNonEmptyString("Enter Reason for Emergency Withdrawal: ");
                    manager.withdrawEmergencyFund(p3, wAmount, reason);
                    System.out.println("⚠️ Emergency Withdrawal of $" + String.format("%.2f", wAmount) + " completed for " + p3);
                    break;
            }
        } catch (ExpenseManagerException e) {
            System.out.println("❌ Person Fund error: " + e.getMessage());
        }
    }

    private void handleDeleteExpense() {
        System.out.println("--- 🗑️ DELETE EXPENSE ---");
        String id = readNonEmptyString("Enter Expense ID to delete: ");
        try {
            manager.removeExpense(id);
            System.out.println("✅ Expense with ID [" + id + "] deleted successfully.");
        } catch (ExpenseManagerException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void handleSaveData() {
        try {
            manager.saveData(EXPENSES_FILE, FUNDS_FILE);
            System.out.println("💾 Data saved successfully to '" + EXPENSES_FILE + "' and '" + FUNDS_FILE + "'.");
        } catch (ExpenseManagerException e) {
            System.out.println("❌ Save failed: " + e.getMessage());
        }
    }

    private void handleLoadData() {
        try {
            manager.loadData(EXPENSES_FILE, FUNDS_FILE);
        } catch (ExpenseManagerException e) {
            System.out.println("❌ Load failed: " + e.getMessage());
        }
    }

    private void loadSampleData() {
        System.out.println("⚡ Loading demo sample data...");
        try {
            manager.addExpense(new Expense("Grocery Shopping at Supermarket", 145.50, Category.FOOD, LocalDate.now().minusDays(2), "Alice"));
            manager.addExpense(new Expense("Monthly Apartment Rent Share", 850.00, Category.HOUSING, LocalDate.now().minusDays(5), "Alice"));
            manager.addExpense(new Expense("Gasoline Refill", 45.00, Category.TRANSPORTATION, LocalDate.now().minusDays(1), "Bob"));
            manager.addExpense(new Expense("Electricity & Water Bill", 120.75, Category.UTILITIES, LocalDate.now().minusDays(4), "Bob"));
            manager.addExpense(new Expense("Movie Night & Popcorn", 35.00, Category.ENTERTAINMENT, LocalDate.now().minusDays(3), "Alice"));
            manager.addExpense(new Expense("Pharmacy Prescription", 62.30, Category.MEDICAL, LocalDate.now().minusDays(6), "Bob"));

            manager.depositMonthlySavings("Alice", 300.00);
            manager.depositEmergencyFund("Alice", 500.00);
            manager.depositMonthlySavings("Bob", 250.00);
            manager.depositEmergencyFund("Bob", 400.00);

            System.out.println("✅ Sample demo expenses and person funds loaded!");
        } catch (ExpenseManagerException e) {
            System.out.println("❌ Error populating sample data: " + e.getMessage());
        }
    }

    // --- HELPER METHODS & INPUT VALIDATORS ---

    private void displayExpenseList(List<Expense> list) {
        if (list.isEmpty()) {
            System.out.println("ℹ️  No expenses found matching the criteria.");
            return;
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-10s | %-12s | %-20s | %-10s | %s%n", "ID", "Person", "Date", "Category", "Amount", "Description");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (Expense e : list) {
            System.out.printf("%-10s | %-10s | %-12s | %-20s | $%-9.2f | %s%n",
                    e.getId(), e.getPersonName(), e.getDate().format(DATE_FORMATTER),
                    e.getCategory().getDisplayName(), e.getAmount(), e.getDescription());
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
        double total = list.stream().mapToDouble(Expense::getAmount).sum();
        System.out.printf("Total Count: %d transactions | Total Amount: $%.2f%n", list.size(), total);
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("⚠️ Input cannot be blank. Please try again.");
        }
    }

    private int readIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                int val = Integer.parseInt(input.trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("⚠️ Input out of range. Please enter an integer between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid input! Please enter a valid number.");
            }
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                double val = Double.parseDouble(input.trim());
                if (val > 0) {
                    return val;
                }
                System.out.println("⚠️ Amount must be greater than $0.00.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Invalid input! Please enter a valid decimal number (e.g. 49.99).");
            }
        }
    }

    private LocalDate readDateInput(String prompt, boolean allowDefaultToday) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (allowDefaultToday && (input == null || input.trim().isEmpty())) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(input.trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Invalid date format! Please enter date in yyyy-MM-dd format (e.g., 2026-10-08).");
            }
        }
    }

    private Category selectCategoryPrompt() {
        System.out.println("Select Category:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%2d. %s%n", (i + 1), categories[i].getDisplayName());
        }
        int sel = readIntInput("Enter category number (1-" + categories.length + "): ", 1, categories.length);
        return categories[sel - 1];
    }
}
