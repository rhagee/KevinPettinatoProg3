package com.client.utils;


public enum Colors {
    GREEN("#a4edb5"),
    DARKGREEN("#618f6c"),
    RED("#eda4a4"),
    DARKRED("#916161"),
    YELLOW("#ffd399"),
    BLUE("#a4c7ed"),
    WHITE("#ededed"),
    LIGHT_DARK("#171717");

    private final String color;

    Colors(String color) {
        this.color = color;
    }

    public String getValue() {
        return color;
    }
}
