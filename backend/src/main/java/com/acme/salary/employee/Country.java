package com.acme.salary.employee;

/**
 * Countries ACME employs people in. Each country pays in exactly one currency, so salaries are
 * always reported per country and never mixed across currencies.
 */
public enum Country {
    IN("India", "INR"),
    US("United States", "USD"),
    GB("United Kingdom", "GBP"),
    DE("Germany", "EUR"),
    FR("France", "EUR"),
    CA("Canada", "CAD"),
    AU("Australia", "AUD"),
    SG("Singapore", "SGD");

    private final String displayName;
    private final String currency;

    Country(String displayName, String currency) {
        this.displayName = displayName;
        this.currency = currency;
    }

    public String displayName() {
        return displayName;
    }

    public String currency() {
        return currency;
    }
}
