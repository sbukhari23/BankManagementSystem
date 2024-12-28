select * from customer;
select * from account;
select * from transaction;
select * from accountcustomer;
select * from employee;
select * from branch;
select * from deleted_accounts;
select * from loan;

delete from accountcustomer where account_number = 445291773423;

create or replace view customer_view as
select distinct(customer_id) as 'Customer ID', name as 'Name', cnic as 'CNIC', contact_number as 'Contact',
email as 'Email', dob as 'DOB', street as 'Street', city as 'City', state as 'State' from customer
natural join accountcustomer a where a.customer_id is not null;

select * from customer_view;


create or replace view account_view as
SELECT a.account_title, a.account_number as 'Account Number', a.balance as 'Balance', b.location as 'Branch Location'
FROM account a
JOIN branch b ON a.branch_id = b.branch_id AND a.status = 'open';

select * from account_view;


select c.name, c.cnic, a.account_number, a.balance, b.location from customer c natural join accountcustomer natural join account a natural join branch b;


alter table account
add column account_title VARCHAR(80);

alter table account
add column status ENUM('open', 'closed');

update account
set status = 'open';

DROP PROCEDURE IF EXISTS close_account;
delimiter //
create procedure close_account(IN acc_number VARCHAR(20))

begin
update account
set status = 'closed',
close_account_date = now()
where account_number = acc_number;

insert into deleted_accounts
select * from accountcustomer where account_number = acc_number;

delete from accountcustomer where account_number = acc_number;
end //

delimiter ;


alter table account
drop column account_type_id;

CREATE TABLE Deleted_Accounts (
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (account_number) REFERENCES Account(account_number) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, account_number)
);

alter table account
add column open_account_date datetime default current_timestamp;

alter table account
add column close_account_date datetime;



ALTER TABLE account
ALTER COLUMN status SET DEFAULT 'open';

DELIMITER //

CREATE FUNCTION get_branch_id (emp_id INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE branch INT; -- Declare a variable to store the branch ID

    SELECT branch_id INTO branch
    FROM employee
    WHERE employee_id = emp_id;

    RETURN branch; -- Return the branch ID
END //

DELIMITER ;

drop function if exists get_cid;
select get_branch_id(5);

delimiter //
create function get_cid (nic char(13))
returns int
deterministic

begin
	declare id INT;
    
    select customer_id into id from customer
    where cnic = nic;
    
    return id;
end //

delimiter ;

select get_cid('1234567890128');

alter table employee
add column joining_date DATE;