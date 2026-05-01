# RoadShield - Road Accident Management System

## 📌 Description
RoadShield is a DBMS-based project designed to manage and analyze road accident data efficiently. It stores information related to drivers, vehicles, accidents, violations, and emergency responses.

## 💻 Technologies Used
- Java Swing (Frontend GUI)
- Core Java
- JDBC (Database Connectivity)
- Oracle SQL (Database)

## ⚙️ Features
- Add, update, delete driver details
- Manage vehicle information
- Record accident details with location
- Track violations and fines
- Emergency response tracking
- SQL queries for analysis (Aggregate, GROUP BY, HAVING, Joins)

## 🗄️ Database Design
- Relational schema with normalization (up to 3NF)
- Tables: Driver, Vehicle, Accident, Location, Violation, Emergency_Response, Involved_In
- Constraints: Primary Key, Foreign Key, UNIQUE, CHECK

## 🔄 Architecture
User (Java Swing UI)  
↓  
Application Layer  
↓  
JDBC  
↓  
Oracle Database  

## 📊 Sample Queries
- Aggregate functions (COUNT, SUM, AVG)
- GROUP BY and HAVING
- Joins
- Nested queries
- Views, Indexing, Triggers

## 🚀 How to Run
1. Compile:
   javac -cp ".:lib/*" *.java

2. Run:
   java -cp ".:lib/*" LoginPage
