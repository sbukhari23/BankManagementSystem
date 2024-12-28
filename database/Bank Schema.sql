DROP DATABASE IF EXISTS BankDataBase;
CREATE DATABASE BankDataBase;
USE BankDataBase;

-- Customer Table
CREATE TABLE Customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(15) UNIQUE,
    email VARCHAR(255) UNIQUE,
    dob DATE NOT NULL,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    username VARCHAR(15) UNIQUE CHECK (username REGEXP '^[a-zA-Z0-9]{5,15}$'),
    passhash VARCHAR(255),
    cnic CHAR(13) UNIQUE NOT NULL CHECK (cnic REGEXP '^[0-9]{13}$'),
    transaction_pin CHAR(6)
);

-- Branch Table
CREATE TABLE Branch (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(255) NOT NULL,
    contact_number VARCHAR(15),
    manager_id INT DEFAULT NULL
);

-- Employee Table
CREATE TABLE Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    contact_number VARCHAR(15) UNIQUE,
    branch_id INT NOT NULL,
    dob DATE NOT NULL,
    username VARCHAR(15) UNIQUE NOT NULL CHECK (username REGEXP '^[a-zA-Z0-9]{5,15}$'),
    passhash VARCHAR(255) NOT NULL,
    cnic CHAR(13) UNIQUE NOT NULL CHECK (cnic REGEXP '^[0-9]{13}$'),
    joining_date DATE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id) ON DELETE CASCADE
);

-- Update Branch Table with Manager Relationship
ALTER TABLE Branch
ADD FOREIGN KEY (manager_id) REFERENCES Employee(employee_id) ON DELETE SET NULL;

-- Account Table
CREATE TABLE Account (
    account_number VARCHAR(20) PRIMARY KEY,
    account_title VARCHAR(80),
    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
    branch_id INT NOT NULL,
    status ENUM('open', 'closed') DEFAULT 'open',
    open_account_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    close_account_date DATETIME,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id) ON DELETE CASCADE
);

-- Account-Customer Mapping Table
CREATE TABLE AccountCustomer (
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, account_number)
);

-- Transaction Table
CREATE TABLE Transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    to_account_number VARCHAR(20) NOT NULL,
    from_account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    transaction_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (to_account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    FOREIGN KEY (from_account_number) REFERENCES Account(account_number) ON DELETE CASCADE
);

-- Loan Table
CREATE TABLE Loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_amount DECIMAL(10, 2) NOT NULL,
    loan_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    loan_duration INT NOT NULL,
    loan_purpose ENUM('home', 'car', 'personal', 'educational', 'corporate', 'others') NOT NULL,
    remaining_amount DECIMAL(10, 2) NOT NULL,
    loan_application_status ENUM('accepted', 'rejected', 'pending') DEFAULT 'pending',
    customer_id BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Deleted Accounts Table
CREATE TABLE Deleted_Accounts (
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    PRIMARY KEY (customer_id, account_number),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE
);

-- Insert Data into Branch Table
INSERT INTO Branch (location, contact_number) VALUES
('Islamabad Main', '0511234567'),
('Lahore City Center', '0427654321'),
('Karachi Hub', '0219876543');

-- Insert Data into Employee Table
INSERT INTO Employee (name, role, contact_number, branch_id, dob, username, passhash, cnic, joining_date) VALUES
('Ahmed Khan', 'Branch Manager', '03001234567', 1, '1980-03-15', 'ahmedkhan', 'hash123', '1234567890123', '2010-05-01'),
('Sara Ali', 'Teller', '03019876543', 1, '1992-06-25', 'saraali', 'hash234', '2345678901234', '2015-03-20'),
('Zain Riaz', 'Branch Manager', '03214567890', 2, '1985-10-05', 'zainriaz', 'hash345', '3456789012345', '2008-11-15'),
('Ayesha Iqbal', 'Teller', '03451234567', 2, '1990-09-20', 'ayesha', 'hash456', '4567890123456', '2017-01-10'),
('Bilal Ahmed', 'Branch Manager', '03029876543', 3, '1983-02-28', 'bilalahmed', 'hash567', '5678901234567', '2009-07-22');

-- Update Manager IDs in Branch Table
UPDATE Branch SET manager_id = 1 WHERE branch_id = 1;
UPDATE Branch SET manager_id = 3 WHERE branch_id = 2;
UPDATE Branch SET manager_id = 5 WHERE branch_id = 3;

-- Insert Data into Customer Table
INSERT INTO Customer (name, contact_number, email, dob, street, city, state, cnic) VALUES
('Ali Khan', '03211234567', 'alikhan@example.com', '1990-05-10', '45 Street A', 'Islamabad', 'Punjab', '1234567890128'),
('Fatima Iqbal', '03124567890', 'fatimaiqbal@example.com', '1985-11-20', '67 Block B', 'Lahore', 'Punjab', '9876543210987'),
('Usman Tariq', '03021234567', 'usmantariq@example.com', '1995-07-15', '89 Sector C', 'Karachi', 'Sindh', '1122334455667');

-- Insert Data into Account Table
INSERT INTO Account (account_number, account_title, balance, branch_id) VALUES
('1234567890', 'Ali Savings', 50000.00, 1),
('9876543210', 'Fatima Current', 75000.00, 2),
('1122334455', 'Usman Business', 100000.00, 3);

-- Insert Data into AccountCustomer Table
INSERT INTO AccountCustomer (customer_id, account_number) VALUES
(1, '1234567890'),
(2, '9876543210'),
(3, '1122334455');
