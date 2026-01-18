package main.java.com.tracker.view;



import main.java.com.tracker.model.Expense;
import java.util.List;
import java.util.Map;

/*
Simple console view that prints results.
*/
public class ConsoleView {

    public static void showExpenses(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            System.out.println("No expenses.");
            return;
        }
        System.out.printf("%-6s %-10s %-10s %-12s %s%n", "ID", "Date", "Amount", "Category", "Description");
        for (Expense e : expenses) {
            System.out.printf("%-6d %-10s %-10.2f %-12s %s%n",
                    e.getId(),
                    e.getDate(),
                    e.getAmount(),
                    e.getCategory(),
                    e.getDescription());
        }
    }

    public static void showSummary(double total, int count, Map<String, Double> byCategory) {
        System.out.println("Total: " + String.format("%.2f", total));
        System.out.println("Count: " + count);
        System.out.println("By category:");
        for (String cat : byCategory.keySet()) {
            System.out.printf("  %-12s %.2f%n", cat, byCategory.get(cat));
        }
    }

    public static void showMessage(String msg) {
        System.out.println(msg);
    }

    public static void showWarning(String w) {
        System.out.println("WARNING: " + w);
    }
}