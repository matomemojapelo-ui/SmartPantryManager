package com.yourname.smartpantry.model;

/**
 * Represents one ingredient the user owns at home.
 * Mirrors the "pantry_items" table exactly.
 *
 * The 4-arg constructor (no id) is used when creating a NEW item
 * before it has been assigned a database id.
 */
public class PantryItem {

    private int id;              // PK, -1 if not yet saved
    private String name;         // stored lowercase
    private double quantity;
    private String unit;
    private String expiryDate;   // ISO-8601 "YYYY-MM-DD", nullable

    /** Used when reading from DB (has an id). */
    public PantryItem(int id, String name, double quantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    /** Used when creating a NEW item (no id yet). */
    public PantryItem(String name, double quantity,
                      String unit, String expiryDate) {
        this(-1, name, quantity, unit, expiryDate);
    }

    // ----- Getters -----
    public int getId()            { return id; }
    public String getName()       { return name; }
    public double getQuantity()   { return quantity; }
    public String getUnit()       { return unit; }
    public String getExpiryDate() { return expiryDate; }

    // ----- Setters (needed for editing) -----
    public void setId(int id)                 { this.id = id; }
    public void setName(String name)          { this.name = name; }
    public void setQuantity(double quantity)  { this.quantity = quantity; }
    public void setUnit(String unit)          { this.unit = unit; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    /** Helpful for logs and the ListView fallback. */
    @Override
    public String toString() {
        return name + " — " + quantity + " " + (unit == null ? "" : unit);
    }
}