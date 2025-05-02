package com.example.payroll;

public class Employee {
    private int id;
    private String name, department, position;
    private double salary, hours;

    public Employee(int id, String name, String department, String position, double salary, double hours) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.position = position;
        this.salary = salary;
        this.hours = hours;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public double getSalary() { return salary; }
    public double getHours() { return hours; }
}
