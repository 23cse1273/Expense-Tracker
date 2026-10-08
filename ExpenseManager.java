import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom Exception for ExpenseManager business logic errors.
 */
class ExpenseManagerException extends Exception {
    public ExpenseManagerException(String message) {
        super(message);
    }

    public ExpenseManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Manages financial accounts, monthly savings, and emergency funds per individual person.
 */
class PersonFund {
    private final String personName;
    private double monthlySavingsBalance;
    private double emergencyFundBalance;

    public PersonFund(String personName) {
        this.personName = personName.trim();
        this.monthlySavingsBalance = 0.0;
        this.emergencyFundBalance = 0.0;
    }

    public PersonFund(String personName, double monthlySavingsBalance, double emergencyFundBalance) {
        this.personName = personName.trim();
        this.monthlySavingsBalance = Math.max(0, monthlySavingsBalance);
        this.emergencyFundBalance = Math.max(0, emergencyFundBalance);
    }

    public String getPersonName() {
        return personName;
    }

    public double getMonthlySavingsBalance() {
        return monthlySavingsBalance;
    }

    public double getEmergencyFundBalance() {
        return emergencyFundBalance;
    }

    public void addMonthlySavings(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.monthlySavingsBalance += amount;
    }

    public void addEmergencyFund(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.emergencyFundBalance += amount;
    }

    public void withdrawEmergencyFund(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > emergencyFundBalance) {
            throw new IllegalArgumentException(String.format("Insufficient emergency fund! Requested $%.2f, Available: $%.2f", amount, emergencyFundBalance));
        }
        this.emergencyFundBalance -= amount;
    }

    public String toCsvString() {
        return String.format("%s,%.2f,%.2f", personName.replace(",", ";"), monthlySavingsBalance, emergencyFundBalance);
    }

    public static PersonFund fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid PersonFund CSV line.");
        }
        String name = parts[0].trim().replace(";", ",");
        double savings = Double.parseDouble(parts[1].trim());
        double emergency = Double.parseDouble(parts[2].trim());
        return new PersonFund(name, savings, emergency);
    }

    @Override
    public String toString() {
        return String.format("Person: %-12s | Monthly Savings: $%-10.2f | Emergency Fund: $%-10.2f",
                personName, monthlySavingsBalance, emergencyFundBalance);
    }
}

/**
 * Business Logic Controller for Expense Tracker.
 * Handles adding/removing expenses, category-wise spending analysis,
 * dynamic multi-criteria filtering, per-person savings & emergency fund tracking,
 * error handling, and persistent storage.
 */
public class ExpenseManager {
    private final List<Expense> expenses;
    private final Map<String, PersonFund> personFundsMap;

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.personFundsMap = new HashMap<>();
    }

    // --- EXPENSE MANAGEMENT ---

    /**
     * Adds an expense to the records.
     */
    public void addExpense(Expense expense) throws ExpenseManagerException {
        if (expense == null) {
            throw new ExpenseManagerException("Cannot add a null expense.");
        }
        expenses.add(expense);
        ensurePersonExists(expense.getPersonName());
    }

    /**
     * Removes an expense by ID.
     */
    public boolean removeExpense(String id) throws ExpenseManagerException {
        if (id == null || id.trim().isEmpty()) {
            throw new ExpenseManagerException("Expense ID cannot be empty.");
        }
        boolean removed = expenses.removeIf(e -> e.getId().equalsIgnoreCase(id.trim()));
        if (!removed) {
            throw new ExpenseManagerException("No expense found with ID: " + id);
        }
        return true;
    }

    /**
     * Retrieves an unmodifiable list of all expenses.
     */
    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    /**
     * Calculates the grand total of all expenses.
     */
    public double getTotalSpending() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    // --- CATEGORY WISE SPENDING ANALYSIS ---

    /**
     * Aggregates spending by Category across all persons.
     */
    public Map<Category, Double> getCategoryWiseSpending() {
        Map<Category, Double> categoryMap = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            categoryMap.put(c, 0.0);
        }
        for (Expense e : expenses) {
            categoryMap.put(e.getCategory(), categoryMap.get(e.getCategory()) + e.getAmount());
        }
        return categoryMap;
    }

    /**
     * Aggregates category spending for a specific person.
     */
    public Map<Category, Double> getCategoryWiseSpendingByPerson(String personName) {
        Map<Category, Double> categoryMap = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            categoryMap.put(c, 0.0);
        }
        expenses.stream()
                .filter(e -> e.getPersonName().equalsIgnoreCase(personName.trim()))
                .forEach(e -> categoryMap.put(e.getCategory(), categoryMap.get(e.getCategory()) + e.getAmount()));
        return categoryMap;
    }

    // --- FILTERING LOGIC ---

    /**
     * Filters expenses by category.
     */
    public List<Expense> filterByCategory(Category category) {
        if (category == null) return getAllExpenses();
        return expenses.stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses by person.
     */
    public List<Expense> filterByPerson(String personName) {
        if (personName == null || personName.trim().isEmpty()) return getAllExpenses();
        return expenses.stream()
                .filter(e -> e.getPersonName().equalsIgnoreCase(personName.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses within a date range [startDate, endDate].
     */
    public List<Expense> filterByDateRange(LocalDate startDate, LocalDate endDate) throws ExpenseManagerException {
        if (startDate == null || endDate == null) {
            throw new ExpenseManagerException("Start date and end date must not be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ExpenseManagerException("Start date (" + startDate + ") cannot be after end date (" + endDate + ").");
        }
        return expenses.stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses for a specific Year and Month.
     */
    public List<Expense> filterByMonth(int year, int month) throws ExpenseManagerException {
        if (month < 1 || month > 12) {
            throw new ExpenseManagerException("Invalid month value: " + month + ". Must be between 1 and 12.");
        }
        return expenses.stream()
                .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses between minAmount and maxAmount.
     */
    public List<Expense> filterByAmountRange(double minAmount, double maxAmount) throws ExpenseManagerException {
        if (minAmount < 0 || maxAmount < 0) {
            throw new ExpenseManagerException("Amounts must be non-negative.");
        }
        if (minAmount > maxAmount) {
            throw new ExpenseManagerException("Min amount cannot be greater than Max amount.");
        }
        return expenses.stream()
                .filter(e -> e.getAmount() >= minAmount && e.getAmount() <= maxAmount)
                .collect(Collectors.toList());
    }

    // --- PERSON SAVINGS & EMERGENCY FUNDS SEPARATED BY PERSON ---

    private PersonFund ensurePersonExists(String personName) {
        String cleanName = personName.trim();
        return personFundsMap.computeIfAbsent(cleanName, PersonFund::new);
    }

    public void depositMonthlySavings(String personName, double amount) throws ExpenseManagerException {
        try {
            PersonFund fund = ensurePersonExists(personName);
            fund.addMonthlySavings(amount);

            // Create a record of savings deposit
            Expense savingsRecord = new Expense(
                    "Monthly Savings Deposit",
                    amount,
                    Category.SAVINGS,
                    LocalDate.now(),
                    personName
            );
            expenses.add(savingsRecord);
        } catch (IllegalArgumentException e) {
            throw new ExpenseManagerException("Failed to deposit monthly savings: " + e.getMessage(), e);
        }
    }

    public void depositEmergencyFund(String personName, double amount) throws ExpenseManagerException {
        try {
            PersonFund fund = ensurePersonExists(personName);
            fund.addEmergencyFund(amount);

            // Create a record of emergency fund deposit
            Expense emergencyRecord = new Expense(
                    "Emergency Fund Allocation",
                    amount,
                    Category.EMERGENCY,
                    LocalDate.now(),
                    personName
            );
            expenses.add(emergencyRecord);
        } catch (IllegalArgumentException e) {
            throw new ExpenseManagerException("Failed to deposit emergency money: " + e.getMessage(), e);
        }
    }

    public void withdrawEmergencyFund(String personName, double amount, String reason) throws ExpenseManagerException {
        try {
            PersonFund fund = personFundsMap.get(personName.trim());
            if (fund == null) {
                throw new ExpenseManagerException("No fund account found for person: " + personName);
            }
            fund.withdrawEmergencyFund(amount);

            // Log withdrawal as emergency expense
            Expense withdrawalExpense = new Expense(
                    "Emergency Expense: " + (reason != null && !reason.trim().isEmpty() ? reason : "Emergency Withdrawal"),
                    amount,
                    Category.EMERGENCY,
                    LocalDate.now(),
                    personName
            );
            expenses.add(withdrawalExpense);
        } catch (IllegalArgumentException e) {
            throw new ExpenseManagerException(e.getMessage(), e);
        }
    }

    public Collection<PersonFund> getAllPersonFunds() {
        return Collections.unmodifiableCollection(personFundsMap.values());
    }

    public PersonFund getPersonFund(String personName) {
        return personFundsMap.get(personName.trim());
    }

    // --- DATA FILE PERSISTENCE ---

    /**
     * Saves expenses and person funds to CSV files.
     */
    public void saveData(String expensesFilename, String fundsFilename) throws ExpenseManagerException {
        // 1. Save expenses
        try (PrintWriter writer = new PrintWriter(new FileWriter(expensesFilename))) {
            for (Expense e : expenses) {
                writer.println(e.toCsvString());
            }
        } catch (IOException e) {
            throw new ExpenseManagerException("Failed to save expenses file: " + e.getMessage(), e);
        }

        // 2. Save person funds
        try (PrintWriter writer = new PrintWriter(new FileWriter(fundsFilename))) {
            for (PersonFund f : personFundsMap.values()) {
                writer.println(f.toCsvString());
            }
        } catch (IOException e) {
            throw new ExpenseManagerException("Failed to save person funds file: " + e.getMessage(), e);
        }
    }

    /**
     * Loads expenses and person funds from CSV files.
     */
    public void loadData(String expensesFilename, String fundsFilename) throws ExpenseManagerException {
        int loadedExpenses = 0;
        int loadedFunds = 0;

        // Load Person Funds
        File fundsFile = new File(fundsFilename);
        if (fundsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fundsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        PersonFund fund = PersonFund.fromCsvString(line);
                        personFundsMap.put(fund.getPersonName(), fund);
                        loadedFunds++;
                    } catch (Exception e) {
                        System.err.println("Warning: Skipped corrupted person fund line: " + line);
                    }
                }
            } catch (IOException e) {
                throw new ExpenseManagerException("Error reading funds file: " + e.getMessage(), e);
            }
        }

        // Load Expenses
        File expensesFile = new File(expensesFilename);
        if (expensesFile.exists()) {
            expenses.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(expensesFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        Expense expense = Expense.fromCsvString(line);
                        expenses.add(expense);
                        ensurePersonExists(expense.getPersonName());
                        loadedExpenses++;
                    } catch (Exception e) {
                        System.err.println("Warning: Skipped corrupted expense line: " + line);
                    }
                }
            } catch (IOException e) {
                throw new ExpenseManagerException("Error reading expenses file: " + e.getMessage(), e);
            }
        }
        System.out.println("Data loaded successfully: " + loadedExpenses + " expenses, " + loadedFunds + " person funds.");
    }
}
