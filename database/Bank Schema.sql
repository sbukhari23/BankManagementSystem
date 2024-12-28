-- Initial Step: Select Database
DROP DATABASE IF EXISTS BankDataBase;
CREATE DATABASE BankDataBase;
USE BankDataBase;

-- Step 1: Create Branch Table
CREATE TABLE Branch (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    contact_number VARCHAR(15),
    manager_id INT
);

-- Step 2: Create Employee Table
CREATE TABLE Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50),
    contact_number VARCHAR(15),
    branch_id INT,
    dob DATE,
    username VARCHAR(50),
    passhash VARCHAR(100),
    cnic CHAR(13),
    joining_date DATE,
    street VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);

-- Step 3: Create Customer Table
CREATE TABLE Customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(15),
    email VARCHAR(100),
    dob DATE,
    street VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    cnic CHAR(13) UNIQUE
);

-- Step 4: Create Account Table
CREATE TABLE Account (
    account_number VARCHAR(20) PRIMARY KEY,
    account_title VARCHAR(80),
    balance DECIMAL(15, 2),
    branch_id INT,
    status ENUM('open', 'closed') DEFAULT 'open',
    open_account_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    close_account_date DATETIME,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);

-- Step 5: Create AccountCustomer Table
CREATE TABLE AccountCustomer (
    customer_id INT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (account_number) REFERENCES Account(account_number),
    PRIMARY KEY (customer_id, account_number)
);

-- Step 6: Create Deleted Accounts Table
CREATE TABLE Deleted_Accounts (
    customer_id INT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, account_number)
);

-- Step 7: Insert Data into Branch Table
INSERT INTO Branch (location, contact_number) VALUES
('Islamabad Main', '0511234567'),
('Lahore City Center', '0427654321'),
('Karachi Hub', '0219876543');

-- Step 8: Insert Data into Employee Table
INSERT INTO Employee (name, role, contact_number, branch_id, dob, username, passhash, cnic, joining_date, street, city, state) VALUES
('Ahmed Khan', 'Branch Manager', '03001234567', 1, '1980-03-15', 'ahmedkhan', 'hash123', '1234567890123', '2010-05-01', '10 Blue Area', 'Islamabad', 'Punjab'),
('Sara Ali', 'Teller', '03019876543', 1, '1992-06-25', 'saraali', 'hash234', '2345678901234', '2015-03-20', '20 Melody Market', 'Islamabad', 'Punjab'),
('Zain Riaz', 'Branch Manager', '03214567890', 2, '1985-10-05', 'zainriaz', 'hash345', '3456789012345', '2008-11-15', '45 Gulberg', 'Lahore', 'Punjab'),
('Ayesha Iqbal', 'Teller', '03451234567', 2, '1990-09-20', 'ayesha', 'hash456', '4567890123456', '2017-01-10', '78 Liberty Market', 'Lahore', 'Punjab'),
('Bilal Ahmed', 'Branch Manager', '03029876543', 3, '1983-02-28', 'bilalahmed', 'hash567', '5678901234567', '2009-07-22', '12 Clifton', 'Karachi', 'Sindh');

-- Step 9: Update Manager IDs in Branch Table
UPDATE Branch SET manager_id = 1 WHERE branch_id = 1;
UPDATE Branch SET manager_id = 3 WHERE branch_id = 2;
UPDATE Branch SET manager_id = 5 WHERE branch_id = 3;

-- Step 10: Insert Data into Customer Table
INSERT INTO Customer (name, contact_number, email, dob, street, city, state, cnic) VALUES
('Ali Khan', '03211234567', 'alikhan@example.com', '1990-05-10', '45 Street A', 'Islamabad', 'Punjab', '1234567890128'),
('Fatima Iqbal', '03124567890', 'fatimaiqbal@example.com', '1985-11-20', '67 Block B', 'Lahore', 'Punjab', '9876543210987'),
('Usman Tariq', '03021234567', 'usmantariq@example.com', '1995-07-15', '89 Sector C', 'Karachi', 'Sindh', '1122334455667');

-- Step 11: Insert Data into Account Table
INSERT INTO Account (account_number, account_title, balance, branch_id) VALUES
('1234567890', 'Ali Savings', 50000.00, 1),
('9876543210', 'Fatima Current', 75000.00, 2),
('1122334455', 'Usman Business', 100000.00, 3);

-- Step 12: Insert Data into AccountCustomer Table
INSERT INTO AccountCustomer (customer_id, account_number) VALUES
(1, '1234567890'),
(2, '9876543210'),
(3, '1122334455');



-- Final Step: Verification Queries
SELECT * FROM Customer;
SELECT * FROM Account;
SELECT * FROM Transaction;
SELECT * FROM AccountCustomer;
SELECT * FROM Employee;
SELECT * FROM Branch;
SELECT * FROM Deleted_Accounts;
SELECT * FROM Loan;
SELECT * FROM customer_view;
SELECT * FROM account_view;
