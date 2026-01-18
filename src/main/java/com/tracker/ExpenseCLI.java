package main.java.com.tracker;

import main.java.com.tracker.model.Budget;
import main.java.com.tracker.model.Expense;
import main.java.com.tracker.repository.BudgetRepository;
import main.java.com.tracker.repository.ExpenseRepository;
import main.java.com.tracker.view.ConsoleView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/*
Entry point and CLI orchestration.

This version uses only basic Java constructs so it's suitable for early Java learners.
Usage: java tracker.ExpenseCLI <command> [options]

Commands:
- add --description "text" --amount 12.50 [--date YYYY-MM-DD] [--category cat]
- update <id> [--description ...] [--amount ...] [--date ...] [--category ...]
- delete <id>
- list [--category cat] [--month M]   # month 1-12 (current year)
- summary
- month-summary <month>
- set-budget --month M --amount 500 [--year YYYY]
- export --file filename.csv [--month M] [--category cat]
- help
*/
public class ExpenseCLI {
    private static final String EXPENSES_FILE = "expenses.txt";
    private static final String BUDGETS_FILE  = "budgets.txt";

    private ExpenseRepository expenseRepo;
    private BudgetRepository budgetRepo;

    public ExpenseCLI() {
        expenseRepo = new ExpenseRepository(EXPENSES_FILE);
        budgetRepo  = new BudgetRepository(BUDGETS_FILE);
    }

    public static void main(String[] args) {
        ExpenseCLI app = new ExpenseCLI();
        if (args.length == 0) {
            printHelp();
            return;
        }
        app.run(args);
    }

    private void run(String[] args) {
        String cmd = args[0].toLowerCase();
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        try {
            if ("add".equals(cmd)) doAdd(rest);
            else if ("update".equals(cmd)) doUpdate(rest);
            else if ("delete".equals(cmd)) doDelete(rest);
            else if ("list".equals(cmd)) doList(rest);
            else if ("summary".equals(cmd)) doSummary();
            else if ("month-summary".equals(cmd)) doMonthSummary(rest);
            else if ("set-budget".equals(cmd)) doSetBudget(rest);
            else if ("export".equals(cmd)) doExport(rest);
            else printHelp();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // ========== Basic flag parser (keeps position order) ==========
    // returns map: flagWithoutDashes -> value (next token) OR "__posN" -> positional
    private Map<String, String> parseFlags(String[] args) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        int posIndex = 0;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String key = a.substring(2);
                if (i + 1 < args.length && !args[i+1].startsWith("--")) {
                    map.put(key, args[i+1]);
                    i++;
                } else {
                    map.put(key, "true");
                }
            } else if (a.startsWith("-")) {
                String key = a.substring(1);
                if (i + 1 < args.length && !args[i+1].startsWith("-")) {
                    map.put(key, args[i+1]);
                    i++;
                } else {
                    map.put(key, "true");
                }
            } else {
                map.put("__pos" + posIndex, a);
                posIndex++;
            }
        }
        return map;
    }

    private String first(Map<String,String> flags, String... keys) {
        for (String k : keys) {
            if (flags.containsKey(k)) return flags.get(k);
        }
        return null;
    }

    // ========== Commands ==========
    private void doAdd(String[] args) {
        Map<String,String> flags = parseFlags(args);
        String desc = first(flags, "description", "d");
        String amtS = first(flags, "amount", "a");
        if (desc == null || amtS == null) {
            ConsoleView.showMessage("add requires --description and --amount");
            return;
        }
        String date = first(flags, "date");
        if (date == null) {
            // default to today's date in simple YYYY-MM-DD format
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH) + 1;
            int d = c.get(Calendar.DAY_OF_MONTH);
            date = y + "-" + (m < 10 ? "0" + m : m) + "-" + (d < 10 ? "0" + d : d);
        }
        String category = first(flags, "category", "c");
        double amount = Double.parseDouble(amtS);
        Expense e = new Expense(0L, date, amount, desc, category);
        long id = expenseRepo.add(e);
        ConsoleView.showMessage("Added expense id=" + id);

        // budget check
        checkBudgetForDate(date);
    }

    private void doUpdate(String[] args) {
        if (args.length == 0) {
            ConsoleView.showMessage("update requires an id");
            return;
        }
        long id = Long.parseLong(args[0]);
        Map<String,String> flags = parseFlags(Arrays.copyOfRange(args, 1, args.length));
        Expense e = expenseRepo.findById(id);
        if (e == null) {
            ConsoleView.showMessage("Expense not found: " + id);
            return;
        }
        String oldDate = e.getDate();
        if (flags.containsKey("description") || flags.containsKey("d")) {
            e.setDescription(first(flags, "description", "d"));
        }
        if (flags.containsKey("amount") || flags.containsKey("a")) {
            e.setAmount(Double.parseDouble(first(flags, "amount", "a")));
        }
        if (flags.containsKey("date")) {
            e.setDate(first(flags, "date"));
        }
        if (flags.containsKey("category") || flags.containsKey("c")) {
            e.setCategory(first(flags, "category", "c"));
        }
        boolean ok = expenseRepo.update(e);
        if (ok) ConsoleView.showMessage("Updated expense id=" + id);
        else ConsoleView.showMessage("Failed to update expense id=" + id);

        // check budgets for both months if changed
        checkBudgetForDate(oldDate);
        checkBudgetForDate(e.getDate());
    }

    private void doDelete(String[] args) {
        if (args.length == 0) {
            ConsoleView.showMessage("delete requires an id");
            return;
        }
        long id = Long.parseLong(args[0]);
        Expense e = expenseRepo.findById(id);
        if (e == null) {
            ConsoleView.showMessage("Expense not found: " + id);
            return;
        }
        boolean ok = expenseRepo.delete(id);
        if (ok) {
            ConsoleView.showMessage("Deleted expense id=" + id);
            checkBudgetForDate(e.getDate());
        } else {
            ConsoleView.showMessage("Failed to delete id=" + id);
        }
    }

    private void doList(String[] args) {
        Map<String,String> flags = parseFlags(args);
        String category = first(flags, "category", "c");
        String monthS = first(flags, "month", "m");
        Integer month = (monthS == null) ? null : Integer.parseInt(monthS);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        List<Expense> results;
        if (category != null) {
            results = expenseRepo.findByCategory(category);
            if (month != null) {
                List<Expense> filtered = new ArrayList<Expense>();
                for (Expense ex : results) {
                    if (matchesYearMonth(ex.getDate(), year, month)) filtered.add(ex);
                }
                results = filtered;
            }
        } else if (month != null) {
            results = expenseRepo.findByMonthYear(year, month);
        } else {
            results = expenseRepo.findAll();
        }
        ConsoleView.showExpenses(results);
    }

    private void doSummary() {
        List<Expense> all = expenseRepo.findAll();
        double total = 0.0;
        Map<String, Double> byCat = new LinkedHashMap<String, Double>();
        for (Expense e : all) {
            total += e.getAmount();
            String cat = e.getCategory();
            if (cat == null || cat.isEmpty()) cat = "(uncategorized)";
            Double prev = byCat.get(cat);
            if (prev == null) prev = 0.0;
            prev = prev + e.getAmount();
            byCat.put(cat, prev);
        }
        ConsoleView.showSummary(total, all.size(), byCat);
    }

    private void doMonthSummary(String[] args) {
        if (args.length == 0) {
            ConsoleView.showMessage("month-summary requires a month number (1-12)");
            return;
        }
        int month = Integer.parseInt(args[0]);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        List<Expense> list = expenseRepo.findByMonthYear(year, month);
        ConsoleView.showExpenses(list);
        double total = 0.0;
        for (Expense e : list) total += e.getAmount();
        ConsoleView.showMessage("Month total: " + String.format("%.2f", total));
        Budget b = budgetRepo.find(year, month);
        if (b != null) {
            ConsoleView.showMessage("Budget: " + String.format("%.2f", b.getAmount()));
            if (total > b.getAmount()) {
                ConsoleView.showWarning("You have exceeded the budget by " + String.format("%.2f", (total - b.getAmount())));
            } else {
                ConsoleView.showMessage("Remaining: " + String.format("%.2f", (b.getAmount() - total)));
            }
        } else {
            ConsoleView.showMessage("No budget set for this month.");
        }
    }

    private void doSetBudget(String[] args) {
        Map<String,String> flags = parseFlags(args);
        String monthS = first(flags, "month", "m");
        String amountS = first(flags, "amount", "a");
        String yearS = first(flags, "year", "y");
        if (monthS == null || amountS == null) {
            ConsoleView.showMessage("set-budget requires --month and --amount");
            return;
        }
        int month = Integer.parseInt(monthS);
        int year;
        if (yearS == null) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
        } else {
            year = Integer.parseInt(yearS);
        }
        double amount = Double.parseDouble(amountS);
        Budget b = new Budget(year, month, amount);
        budgetRepo.upsert(b);
        ConsoleView.showMessage("Budget set for " + month + "/" + year + " : " + String.format("%.2f", amount));
        // check immediately
        double total = expenseRepo.totalForMonth(year, month);
        if (total > amount) {
            ConsoleView.showWarning("You have already exceeded this budget by " + String.format("%.2f", (total - amount)));
        }
    }

    private void doExport(String[] args) {
        Map<String,String> flags = parseFlags(args);
        String file = first(flags, "file", "f");
        if (file == null) {
            ConsoleView.showMessage("export requires --file");
            return;
        }
        String monthS = first(flags, "month", "m");
        Integer month = (monthS == null) ? null : Integer.parseInt(monthS);
        String category = first(flags, "category", "c");
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);

        List<Expense> out;
        if (category != null) {
            out = expenseRepo.findByCategory(category);
            if (month != null) {
                List<Expense> filtered = new ArrayList<Expense>();
                for (Expense e : out) {
                    if (matchesYearMonth(e.getDate(), year, month)) filtered.add(e);
                }
                out = filtered;
            }
        } else if (month != null) {
            out = expenseRepo.findByMonthYear(year, month);
        } else {
            out = expenseRepo.findAll();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write("id,date,amount,category,description");
            writer.newLine();
            for (Expense e : out) {
                writer.write(csvEscape(String.valueOf(e.getId())) + "," +
                        csvEscape(e.getDate()) + "," +
                        csvEscape(String.format("%.2f", e.getAmount())) + "," +
                        csvEscape(e.getCategory()) + "," +
                        csvEscape(e.getDescription()));
                writer.newLine();
            }
            ConsoleView.showMessage("Exported " + out.size() + " expenses to " + file);
        } catch (IOException ex) {
            System.err.println("Error exporting CSV: " + ex.getMessage());
        } finally {
            try { if (writer != null) writer.close(); } catch (IOException ignored) {}
        }
    }

    // ========== Helpers ==========
    private void checkBudgetForDate(String date) {
        // date assumed "YYYY-MM-DD"
        if (date == null || date.length() < 7) return;
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7));
        double total = expenseRepo.totalForMonth(year, month);
        Budget b = budgetRepo.find(year, month);
        if (b != null && total > b.getAmount()) {
            ConsoleView.showWarning("For " + month + "/" + year + " you have spent " + String.format("%.2f", total)
                    + " which exceeds budget " + String.format("%.2f", b.getAmount()) + " by " + String.format("%.2f", (total - b.getAmount())));
        }
    }

    private boolean matchesYearMonth(String date, int year, int month) {
        if (date == null || date.length() < 7) return false;
        try {
            int y = Integer.parseInt(date.substring(0,4));
            int m = Integer.parseInt(date.substring(5,7));
            return y == year && m == month;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String csvEscape(String s) {
        if (s == null) s = "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static void printHelp() {
        System.out.println("ExpenseTracker - simple CLI (OCA-level style)");
        System.out.println("Usage: java tracker.ExpenseCLI <command> [options]");
        System.out.println("Commands:");
        System.out.println("  add --description \"text\" --amount 12.50 [--date YYYY-MM-DD] [--category cat]");
        System.out.println("  update <id> [--description ...] [--amount ...] [--date ...] [--category ...]");
        System.out.println("  delete <id>");
        System.out.println("  list [--category cat] [--month M]");
        System.out.println("  summary");
        System.out.println("  month-summary <month>");
        System.out.println("  set-budget --month M --amount 500 [--year YYYY]");
        System.out.println("  export --file filename.csv [--month M] [--category cat]");
        System.out.println("  help");
    }
}