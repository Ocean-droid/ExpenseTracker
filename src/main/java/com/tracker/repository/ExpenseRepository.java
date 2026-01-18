package main.java.com.tracker.repository;

import main.java.com.tracker.model.Expense;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
Simple file-based repository for Expense.

File format: each line is an Expense serialized with Expense.serialize()
File name is provided in constructor, e.g. "expenses.txt".
*/
public class ExpenseRepository {
    private final File file;

    public ExpenseRepository(String filename) {
        this.file = new File(filename);
    }

    // Return all expenses (empty list if file missing)
    public List<Expense> findAll() {
        List<Expense> out = new ArrayList<Expense>();
        if (!file.exists()) {
            return out;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                out.add(Expense.deserialize(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading expenses: " + e.getMessage());
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        }
        return out;
    }

    // Find by id
    public Expense findById(long id) {
        List<Expense> all = findAll();
        for (Expense e : all) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    // Add expense: assigns id and writes file
    public long add(Expense exp) {
        List<Expense> all = findAll();
        long max = 0;
        for (Expense e : all) {
            if (e.getId() > max) max = e.getId();
        }
        long next = max + 1;
        exp.setId(next);
        all.add(exp);
        writeAll(all);
        return next;
    }

    // Update existing expense (by id)
    public boolean update(Expense exp) {
        List<Expense> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == exp.getId()) {
                all.set(i, exp);
                found = true;
                break;
            }
        }
        if (found) {
            writeAll(all);
        }
        return found;
    }

    // Delete by id
    public boolean delete(long id) {
        List<Expense> all = findAll();
        boolean removed = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == id) {
                all.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            writeAll(all);
        }
        return removed;
    }

    // Find expenses for given year and month (month 1-12). Year and month parsed from date string "YYYY-MM-DD"
    public List<Expense> findByMonthYear(int year, int month) {
        List<Expense> all = findAll();
        List<Expense> out = new ArrayList<Expense>();
        String yearPrefix = String.valueOf(year) + "-";
        String monthPrefix = (month < 10) ? "0" + month : String.valueOf(month);
        for (Expense e : all) {
            String d = e.getDate();
            if (d.length() >= 7 && d.startsWith(yearPrefix) && d.substring(5,7).equals(monthPrefix)) {
                out.add(e);
            }
        }
        return out;
    }

    // Find by category (case-insensitive)
    public List<Expense> findByCategory(String category) {
        List<Expense> all = findAll();
        List<Expense> out = new ArrayList<Expense>();
        for (Expense e : all) {
            String cat = e.getCategory();
            if (cat != null && cat.equalsIgnoreCase(category)) {
                out.add(e);
            }
        }
        return out;
    }

    // Sum of amounts for a given month/year
    public double totalForMonth(int year, int month) {
        List<Expense> list = findByMonthYear(year, month);
        double total = 0.0;
        for (Expense e : list) {
            total += e.getAmount();
        }
        return total;
    }

    // Write all expenses to file (overwrite)
    private void writeAll(List<Expense> list) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            for (Expense e : list) {
                writer.write(e.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing expenses: " + e.getMessage());
        } finally {
            try { if (writer != null) writer.close(); } catch (IOException ignored) {}
        }
    }
}