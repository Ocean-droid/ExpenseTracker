package main.java.com.tracker.repository;

import main.java.com.tracker.model.Budget;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
Simple file-based repository for Budget.
File format: each line = Budget.serialize()
*/
public class BudgetRepository {
    private final File file;

    public BudgetRepository(String filename) {
        this.file = new File(filename);
    }

    public List<Budget> findAll() {
        List<Budget> out = new ArrayList<Budget>();
        if (!file.exists()) return out;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                out.add(Budget.deserialize(line));
            }
        } catch (IOException e) {
            System.err.println("Error reading budgets: " + e.getMessage());
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        }
        return out;
    }

    // Find single budget by year and month
    public Budget find(int year, int month) {
        List<Budget> all = findAll();
        for (Budget b : all) {
            if (b.getYear() == year && b.getMonth() == month) return b;
        }
        return null;
    }

    // Insert or update a budget
    public void upsert(Budget b) {
        List<Budget> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            Budget cur = all.get(i);
            if (cur.getYear() == b.getYear() && cur.getMonth() == b.getMonth()) {
                all.set(i, b);
                found = true;
                break;
            }
        }
        if (!found) all.add(b);
        writeAll(all);
    }

    private void writeAll(List<Budget> list) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            for (Budget b : list) {
                writer.write(b.serialize());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing budgets: " + e.getMessage());
        } finally {
            try { if (writer != null) writer.close(); } catch (IOException ignored) {}
        }
    }
}