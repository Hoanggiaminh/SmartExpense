package com.example.smartexpense.model;

public class DayAmount {
    private double income;
    private double expense;

    public DayAmount() {
        this.income = 0.0;
        this.expense = 0.0;
    }

    public DayAmount(double income, double expense) {
        this.income = income;
        this.expense = expense;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public void addIncome(double amount) {
        this.income += amount;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public void addExpense(double amount) {
        this.expense += amount;
    }

    public double getBalance() {
        return income - expense;
    }

    public boolean hasTransactions() {
        return income > 0 || expense > 0;
    }
}

