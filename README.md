```markdown
# ExpenseTracker

This project shows a simple command-line expense tracker, split into:
- tracker.model (Expense, Budget)
- tracker.repository (ExpenseRepository, BudgetRepository)
- tracker.view (ConsoleView)
- tracker.ExpenseCLI (entry point)

It is intentionally implemented using basic Java that fits OCA-level knowledge: arrays/ArrayList, loops, String operations, File I/O.

Requirements
- Java 8+ (javac)

Compile
From the directory that contains the `tracker` folder run:
```
javac tracker/model/*.java tracker/repository/*.java tracker/view/*.java tracker/ExpenseCLI.java
```

Run
```
java tracker.ExpenseCLI <command> [options]
```

Examples
- Add an expense:
```
java tracker.ExpenseCLI add --description "Lunch" --amount 12.50 --category food
```

- Update:
```
java tracker.ExpenseCLI update 3 --amount 18.00 --description "Dinner"
```

- Delete:
```
java tracker.ExpenseCLI delete 5
```

- List all:
```
java tracker.ExpenseCLI list
```

- List by category:
```
java tracker.ExpenseCLI list --category food
```

- Month summary:
```
java tracker.ExpenseCLI month-summary 1
```

- Set budget:
```
java tracker.ExpenseCLI set-budget --month 1 --amount 500
```

- Export CSV:
```
java tracker.ExpenseCLI export --file expenses.csv
```

Notes / Limitations (simple design)
- Date is stored as a plain String in format YYYY-MM-DD. The CLI will use today's date if not provided.
- To keep code simple, the fields are not escaped. Avoid using the '|' character in description or category.
- Amounts are double (not BigDecimal) for simplicity.
- Storage files: `expenses.txt` and `budgets.txt` are created in the working directory.
- The code focuses on clarity for learners; it is straightforward to extend with better parsing, validation, or use BigDecimal / java.time APIs later.

If you'd like, I can:
- Convert amounts to BigDecimal and use LocalDate (still simple).
- Add input validation and friendly error messages.
- Provide a short walkthrough of the code for study (explain key methods and flows).
```
