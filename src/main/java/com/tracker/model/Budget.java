package main.java.com.tracker.model;
/*
Simple Budget model: year, month, amount
*/
public class Budget {
    private int year;
    private int month; // 1-12
    private double amount;

    public Budget(int year, int month, double amount) {
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    // Serialize: year|month|amount
    public String serialize() {
        return year + "|" + month + "|" + amount;
    }

    public static Budget deserialize(String line) {
        String[] p = line.split("\\|", -1);
        int y = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        double a = Double.parseDouble(p[2]);
        return new Budget(y, m, a);
    }

    @Override
    public String toString() {
        return "Budget{" + year + "-" + month + "=" + amount + "}";
    }
}