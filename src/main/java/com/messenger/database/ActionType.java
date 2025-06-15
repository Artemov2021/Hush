package com.messenger.database;

public enum ActionType {
    NEW, DELETED, EDITED;

    public static ActionType fromString(String value) {
        return ActionType.valueOf(value.toUpperCase());
    }
}
