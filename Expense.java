import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;

/**
 * Category enum representing standardized expense categories.
 */
enum Category {
    FOOD("Food & Dining"),
    HOUSING("Housing & Rent"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities & Bills"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    MEDICAL("Healthcare & Medical"),
    SAVINGS("Monthly Savings"),
    EMERGENCY("Emergency Fund"),
    OTHER("Miscellaneous");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }
}

/**
 * Represents an individual financial Expense demonstrating Object-Oriented
 * Programming (OOP) principles, strictly enforcing Encapsulation through
 * private fields, validation logic, and robust getters/setters.
 */
public class Expense {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Encapsulated private fields
    private String id;
    private String description;
    private double amount;
    private Category category;
    private LocalDate date;
    private String personName;

    /**
     * Full Constructor with auto-generated ID
     */
    public Expense(String description, double amount, Category category, LocalDate date, String personName) {
        this(UUID.randomUUID().toString().substring(0, 8), description, amount, category, date, personName);
    }

    /**
     * Full Constructor with explicit ID (used when loading saved data)
     */
    public Expense(String id, String description, double amount, Category category, LocalDate date, String personName) {
        setId(id);
        setDescription(description);
        setAmount(amount);
        setCategory(category);
        setDate(date);
        setPersonName(personName);
    }

    // --- ENCAPSULATED GETTERS & SETTERS WITH VALIDATION ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense ID cannot be null or empty.");
        }
        this.id = id.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty.");
        }
        this.description = description.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero. Provided: " + amount);
        }
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        this.date = date;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        if (personName == null || personName.trim().isEmpty()) {
            throw new IllegalArgumentException("Person name cannot be empty.");
        }
        this.personName = personName.trim();
    }

    // --- UTILITY & PERSISTENCE METHODS ---

    /**
     * Formats expense details into a CSV line for file storage.
     */
    public String toCsvString() {
        return String.join(",",
                id,
                escapeCsv(description),
                String.format("%.2f", amount),
                category.name(),
                date.format(DATE_FORMATTER),
                escapeCsv(personName)
        );
    }

    /**
     * Parses a CSV line into an Expense object.
     */
    public static Expense fromCsvString(String csvLine) throws IllegalArgumentException {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line is empty.");
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid CSV line format: " + csvLine);
        }

        try {
            String id = parts[0].trim();
            String description = unescapeCsv(parts[1].trim());
            double amount = Double.parseDouble(parts[2].trim());
            Category category = Category.valueOf(parts[3].trim().toUpperCase());
            LocalDate date = LocalDate.parse(parts[4].trim(), DATE_FORMATTER);
            String personName = unescapeCsv(parts[5].trim());

            return new Expense(id, description, amount, category, date, personName);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format in CSV. Expected yyyy-MM-dd.", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric amount in CSV.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Expense CSV line: " + e.getMessage(), e);
        }
    }

    private static String escapeCsv(String text) {
        return text.replace(",", ";");
    }

    private static String unescapeCsv(String text) {
        return text.replace(";", ",");
    }

    @Override
    public String toString() {
        return String.format("[ID: %-8s] | %-10s | %-15s | %-20s | $%-9.2f | %s",
                id, personName, date.format(DATE_FORMATTER), category.getDisplayName(), amount, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expense)) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
