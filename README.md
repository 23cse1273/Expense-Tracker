# 💰 Java OOP Expense Tracker Application

A comprehensive, command-line **Expense Tracker Application** built in standard Java (JDK 8+) demonstrating core **Object-Oriented Programming (OOP)** principles, **strict encapsulation**, **category-wise spending analysis**, **multi-criteria filtering**, **per-person monthly savings & emergency money tracking**, **file persistence**, and **robust error handling**.

---

## 🌟 Key Features

### 1. 🔒 OOP Architecture & Encapsulation
- **`Expense` Model Class**: Private encapsulated attributes (`id`, `description`, `amount`, `category`, `date`, `personName`) with input validation in constructors and setters.
- **Category Enum**: Strongly-typed categories (`FOOD`, `HOUSING`, `TRANSPORTATION`, `UTILITIES`, `ENTERTAINMENT`, `SHOPPING`, `MEDICAL`, `SAVINGS`, `EMERGENCY`, `OTHER`).
- **Separation of Concerns**: Decoupled architecture dividing data models (`Expense`), business logic & data structures (`ExpenseManager`), and console interface (`Main`).

### 2. 📊 Category-Wise Spending Breakdown
- Aggregate expenses by spending categories across all users.
- Generate category spending breakdowns specific to an individual person.
- Displays exact dollar totals and calculated percentage allocations.

### 3. 🔍 Multi-Criteria Filtering System
Filter transactions dynamically by:
- **Category**: Show all expenses under a specific category.
- **Person**: View expenses logged by a specific individual (e.g., Alice, Bob).
- **Date Range**: View expenses within custom start and end dates (`yyyy-MM-dd`).
- **Month & Year**: Filter expenses for a specific month (e.g. October 2026).
- **Amount Range**: Filter expenses between minimum and maximum dollar values.

### 4. 🏦 Monthly Savings & Emergency Money (Separated by Person)
- Track **Monthly Savings** and **Emergency Funds** independently per individual.
- **Deposit Monthly Savings**: Allocates money into a person's monthly savings pool.
- **Emergency Fund Management**: Deposit funds into a dedicated emergency balance and record emergency withdrawals with stated reasons and overdraft protection validation.

### 5. ⚠️ Comprehensive Error Handling
- **Input Validation**: Wraps `Scanner` with defensive type checking to prevent `InputMismatchException` or numeric conversion crashes when users type invalid strings or dates.
- **Custom Exception Handling**: Uses `ExpenseManagerException` to signal illegal state operations (e.g., negative amounts, empty names, start date after end date, insufficient emergency funds).
- **Safe File Parsing**: Gracefully ignores corrupt file lines during loading while informing the user without terminating execution.

### 6. 💾 File Persistence
- Automatically persists expenses and person fund balances into local CSV text files (`expenses.csv` and `person_funds.csv`).
- Seamlessly reloads previous financial state upon application restart.

---

## 📂 Project Architecture

```
Expense Tracker/
│
├── Expense.java           # Model Class (Encapsulation, Enums, Serialization)
├── ExpenseManager.java    # Business Logic Controller (Filtering, Aggregations, Person Funds, File I/O)
├── Main.java              # User Interface (Console Menu, Input Sanitation, Displays)
└── README.md              # Project Documentation
```

### Class Responsibilities Overview

| Class | Primary Responsibility | OOP Concepts Demonstrated |
| :--- | :--- | :--- |
| **`Expense`** | Encapsulates single transaction data with validation rules and formatting helpers. | Encapsulation, Type Safety, Abstraction |
| **`PersonFund`** | Tracks monthly savings and emergency money for an individual. | Encapsulation, Business Rule Validation |
| **`ExpenseManager`** | Manages collections of expenses & person funds, performs filtering, category aggregations, and file persistence. | Single Responsibility, Aggregation, Streams |
| **`Main`** | Interactive CLI interface providing user options, menu flow, and error-trapped input collection. | Separation of Concerns, User Interaction |

---

## 🚀 How to Compile and Run

### Prerequisites
- **Java Development Kit (JDK)** version 8 or higher installed on your machine.
- Verify installation by running:
  ```bash
  java -version
  javac -version
  ```

### Step 1: Navigate to Project Directory
```bash
cd "c:/Users/manpr/csprojects/Expense Tracker"
```

### Step 2: Compile the Java Source Files
Compile all `.java` source files using `javac`:
```bash
javac Expense.java ExpenseManager.java Main.java
```

### Step 3: Run the Application
Launch the application by executing the `Main` class:
```bash
java Main
```

---

## 🕹️ CLI Menu Options Overview

When you launch the app, you'll be greeted by an interactive menu:

```
==========================================================================
                  💰 JAVA OOP EXPENSE TRACKER 💰                          
     Demonstrating OOP, Encapsulation, Category Spending & Person Funds   
==========================================================================
--------------------------------------------------------------------------
                             MAIN MENU                                    
--------------------------------------------------------------------------
1. ➕ Add New Expense
2. 📋 View All Expenses
3. 📊 View Category-Wise Spending Breakdown
4. 🔍 Filter Expenses (by Person, Category, Date, Amount)
5. 🏦 Person Funds (Monthly Savings & Emergency Fund)
6. 🗑️ Delete Expense
7. 💾 Save Data to File
8. 📂 Load Data from File
9. ⚡ Load Sample Demo Data
0. 🚪 Save & Exit
--------------------------------------------------------------------------
```

---

## 📝 Example Output Demonstration

### Category-Wise Spending Report
```
--- Overall Category Breakdown ---
Category                  | Spent ($)    | Percentage  
----------------------------------------------------------
Food & Dining             | $145.50      |      13.7%
Housing & Rent            | $850.00      |      80.0%
Transportation            | $45.00       |       4.2%
Entertainment             | $21.50       |       2.0%
----------------------------------------------------------
TOTAL                     | $1062.00     |     100.0%
```

### Person Financial Accounts (Monthly Savings & Emergency Funds)
```
Person: Alice        | Monthly Savings: $300.00     | Emergency Fund: $500.00   
Person: Bob          | Monthly Savings: $250.00     | Emergency Fund: $400.00   
```

---

## 📄 License
This project is open-source and created for learning Object-Oriented Programming, Encapsulation, and Java Data Structures.
