package com.example;

public class PersonPojo {
    private String name;
    private int id;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PersonPojo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
