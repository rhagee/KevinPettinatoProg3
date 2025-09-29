package com.prog.utils;


public enum Colors
{
    GREEN("#a4edb5"),
    RED("#eda4a4"),
    YELLOW("#ffd399"),
    BLUE("#a4c7ed"),
    WHITE("#ededed");

    private final String color;

    Colors(String color) {
        this.color = color;
    }

    public String getValue() {
        return color;
    }
}
