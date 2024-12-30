-- Step 1: Select Database
DROP DATABASE IF EXISTS BankDataBase;
CREATE DATABASE BankDataBase;
USE BankDataBase;

-- Step 2: Create Tables
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
    manager_id INT
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
    street varchar(30),
    city varchar(50),
    state varchar(50),
    joining_date DATE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id) ON DELETE CASCADE
);

-- Update Branch Table with Manager Relationship
ALTER TABLE Branch
ADD FOREIGN KEY (manager_id) REFERENCES Employee(employee_id) ON DELETE SET NULL;

-- Account Table
CREATE TABLE Account (
    account_number VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
    branch_id INT NOT NULL,
    status ENUM('open', 'closed') DEFAULT 'open',
    account_title VARCHAR(80),
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
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    to_account_number VARCHAR(20) NOT NULL,
    from_account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    transaction_date_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (to_account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    FOREIGN KEY (from_account_number) REFERENCES Account(account_number) ON DELETE CASCADE
);

-- Loan Table
CREATE TABLE Loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_amount DECIMAL(10, 2) NOT NULL,
    loan_date_time DATETIME NOT NULL DEFAULT current_timestamp,
    loan_duration varchar(20) NOT NULL, 
    loan_purpose ENUM('home', 'car', 'personal', 'educational', 'corporate', 'others') NOT NULL,
    remaining_amount DECIMAL(10, 2) NOT NULL,
    loan_application_status ENUM('accepted', 'rejected', 'pending') NOT NULL DEFAULT 'pending',
    customer_id BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Deleted Accounts Table
CREATE TABLE Deleted_Accounts (
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, account_number)
);

-- Step 3: Create Views
CREATE OR REPLACE VIEW customer_view AS
SELECT DISTINCT(customer_id) AS 'Customer ID', 
       name AS 'Name', 
       cnic AS 'CNIC', 
       contact_number AS 'Contact',
       email AS 'Email', 
       dob AS 'DOB', 
       street AS 'Street', 
       city AS 'City', 
       state AS 'State' 
FROM Customer
NATURAL JOIN AccountCustomer a 
WHERE a.customer_id IS NOT NULL
ORDER BY (customer_id) ASC;

CREATE OR REPLACE VIEW account_view AS
SELECT a.account_title, 
       a.account_number AS 'Account Number', 
       a.balance AS 'Balance', 
       b.location AS 'Branch Location'
FROM Account a
JOIN Branch b ON a.branch_id = b.branch_id AND a.status = 'open';

-- Step 4: Create Procedures
DELIMITER //
CREATE PROCEDURE close_account(IN acc_number VARCHAR(20))
BEGIN
    UPDATE Account
    SET status = 'closed',
        close_account_date = NOW()
    WHERE account_number = acc_number;

    INSERT INTO Deleted_Accounts
    SELECT * FROM AccountCustomer WHERE account_number = acc_number;
    
    INSERT INTO Account_logs (employee_id, action, account_number)
    VALUES (@current_user_id, 'Account closed', acc_number);
    
    DELETE FROM AccountCustomer WHERE account_number = acc_number;
END //
DELIMITER ;
select * from account_logs;
-- Step 5: Create Functions
DELIMITER //
CREATE FUNCTION get_branch_id (emp_id INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE branch INT;

    SELECT branch_id INTO branch
    FROM Employee
    WHERE employee_id = emp_id;

    RETURN branch;
END //

CREATE FUNCTION get_cid (nic CHAR(13))
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE id INT;

    SELECT customer_id INTO id
    FROM Customer
    WHERE cnic = nic;

    RETURN id;
END //
DELIMITER ;

-- Step 6: Data Insertions

-- Insert Data into Branch Table
INSERT INTO Branch (location, contact_number) VALUES
('Islamabad Main', '0511234567'),
('Lahore City Center', '0427654321'),
('Karachi Hub', '0219876543');

-- Insert Data into Employee Table
-- Note: The branch IDs in these insertions must match existing IDs in the Branch table.
INSERT INTO Employee (name, role, contact_number, branch_id, dob, username, passhash, cnic, joining_date) VALUES
('Ahmed Khan', 'Branch Manager', '03001234567', 1, '1980-03-15', 'ahmedkhan', 'hash123', '1234567890123', '2010-05-01'),
('Sara Ali', 'Teller', '03019876543', 1, '1992-06-25', 'saraali', 'hash234', '2345678901234', '2015-03-20'),
('Zain Riaz', 'Branch Manager', '03214567890', 2, '1985-10-05', 'zainriaz', 'hash345', '3456789012345', '2008-11-15'),
('Ayesha Iqbal', 'Teller', '03451234567', 2, '1990-09-20', 'ayesha', 'hash456', '4567890123456', '2017-01-10'),
('Bilal Ahmed', 'Branch Manager', '03029876543', 3, '1983-02-28', 'bilalahmed', 'hash567', '5678901234567', '2009-07-22');

-- Update Manager IDs in Branch Table
-- Ensure the manager_id refers to valid employee IDs
UPDATE Branch SET manager_id = 1 WHERE branch_id = 1;
UPDATE Branch SET manager_id = 3 WHERE branch_id = 2;
UPDATE Branch SET manager_id = 5 WHERE branch_id = 3;

-- Insert Data into Customer Table
-- Ensure CNICs and other unique columns are valid and do not conflict
INSERT INTO Customer (name, contact_number, email, dob, street, city, state, cnic) VALUES
('Ali Khan', '03211234567', 'alikhan@example.com', '1990-05-10', '45 Street A', 'Islamabad', 'Punjab', '1234567890128'),
('Fatima Iqbal', '03124567890', 'fatimaiqbal@example.com', '1985-11-20', '67 Block B', 'Lahore', 'Punjab', '9876543210987'),
('Usman Tariq', '03021234567', 'usmantariq@example.com', '1995-07-15', '89 Sector C', 'Karachi', 'Sindh', '1122334455667');

-- Insert Data into Account Table
-- Ensure branch_id matches existing branches
INSERT INTO Account (account_number, account_title, balance, branch_id) VALUES
('1234567890', 'Ali Savings', 50000.00, 1),
('9876543210', 'Fatima Current', 75000.00, 2),
('1122334455', 'Usman Business', 100000.00, 3);

-- Insert Data into AccountCustomer Table
-- Ensure customer_id and account_number are valid references
INSERT INTO AccountCustomer (customer_id, account_number) VALUES
(1, '1234567890'),
(2, '9876543210'),
(3, '1122334455');
drop trigger customer_update_trigger;
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
drop trigger account_close_trigger;
create table customer_logs (
	log_id int auto_increment primary key,
    employee_id int not null,
    action enum('Customer created') not null,
    timestamp datetime not null default current_timestamp,
    customer_id bigint not null,
    foreign key (customer_id) references customer(customer_id)
);

create table account_logs (
	log_id int auto_increment primary key,
    employee_id int not null,
    action enum('Account created', 'Account closed') not null,
    timestamp datetime not null default current_timestamp,
    account_number varchar(20) not null
);

CREATE TABLE deleted_employees (
    employee_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    contact_number VARCHAR(15) UNIQUE,
    branch_id INT NOT NULL,
    dob DATE NOT NULL,
    cnic CHAR(13) UNIQUE NOT NULL,
    street varchar(30),
    city varchar(50),
    state varchar(50),
    joining_date date not null,
    leaving_date datetime default current_timestamp,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id) ON DELETE CASCADE
);

create table employee_logs (
	log_id int auto_increment primary key,
    manager_id int not null,
    action enum('Employee added', 'Employee updated', 'Employee deleted') not null,
    timestamp datetime not null default current_timestamp,
    employee_id bigint not null
);
 drop trigger if exists loan_trigger;
create table loan_logs(
	log_id int auto_increment primary key,
    manager_id int not null,
    loan_id int not null,
    action enum('Accepted', 'Rejected') not null,
    timestamp datetime not null default current_timestamp,
    foreign key (loan_id) references loan(loan_id)
);

CREATE TRIGGER customer_insert_trigger
AFTER INSERT ON customer
FOR EACH ROW
INSERT INTO customer_logs (employee_id, action, customer_id)
VALUES (@current_user_id, 'Customer created', new.customer_id);

CREATE TRIGGER account_insert_trigger
AFTER INSERT ON account
FOR EACH ROW
INSERT INTO account_logs (employee_id, action, account_number)
VALUES (@current_user_id, 'Account created', new.account_number);

CREATE TRIGGER account_close_trigger
AFTER update ON account
FOR EACH ROW
INSERT INTO account_logs (employee_id, action, account_number)
VALUES (@current_user_id, 'Account closed', old.account_number);

CREATE TRIGGER employee_insert_trigger
AFTER INSERT ON employee
FOR EACH ROW
INSERT INTO employee_logs (manager_id, action, employee_id)
VALUES (@current_user_id, 'Employee added', new.employee_id);

CREATE TRIGGER employee_update_trigger
AFTER UPDATE ON employee
FOR EACH ROW
INSERT INTO employee_logs (manager_id, action, employee_id)
VALUES (@current_user_id, 'Employee updated', new.employee_id);

CREATE TRIGGER employee_after_delete_trigger
AFTER DELETE ON employee
FOR EACH ROW
INSERT INTO deleted_employees (employee_id, name, role, contact_number, branch_id, dob, cnic, street, city, state, joining_date)
values (old.employee_id, old.name, old.role, old.contact_number, old.branch_id, old.dob, old.cnic, old.street, old.city, old.state, old.joining_date);

CREATE TRIGGER employee_delete_trigger
AFTER DELETE ON employee
FOR EACH ROW
INSERT INTO employee_logs (manager_id, action, employee_id)
VALUES (@current_user_id, 'Employee deleted', old.employee_id);

create trigger loan_trigger
after update on loan
for each row
insert into loan_logs(manager_id, loan_id, action)
values (@current_user_id, new.loan_id, new.loan_application_status);

select * from deleted_employees;
select * from employee_logs;
select * from loan_logs;


update customer set transaction_pin = 111111 where customer_id = 5;