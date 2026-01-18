package main.java.com.tracker.model;
/*
Simple Expense model.

Fields:
- id: unique numeric id
- date: "YYYY-MM-DD" string
- amount: double (simple for OCA-level)
- description: plain text
- category: plain text (can be empty)

Note: For simplicity this class stores date as a String and
parsing of month/year is done in repository/CLI using substring.
*/
public class Expense {
    private long id;
    private String date;       // format: YYYY-MM-DD
    private double amount;
    private String description;
    private String category;

    public Expense(long id, String date, double amount, String description, String category) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description == null ? "" : description;
        this.category = category == null ? "" : category;
    }

    // getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // Serialize to a single line: id|date|amount|description|category
    public String serialize() {
        // For simplicity we do not escape '|' in fields. Avoid using '|' in descriptions/categories.
        return id + "|" + date + "|" + amount + "|" + description + "|" + category;
    }

    // Create an Expense from a serialized line (assumes correct format)
    public static Expense deserialize(String line) {
        String[] parts = line.split("\\|", -1); // -1 to keep trailing empty fields
        long id = Long.parseLong(parts[0]);
        String date = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String description = parts.length > 3 ? parts[3] : "";
        String category = parts.length > 4 ? parts[4] : "";
        return new Expense(id, date, amount, description, category);
    }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", date=" + date + ", amount=" + amount +
                ", category=" + category + ", description=" + description + "}";
    }
}
