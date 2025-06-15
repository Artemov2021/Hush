package com.messenger.database;

import java.sql.SQLException;
import java.util.ArrayList;

public class Action {
    public int id;
    public ActionType change_type;
    public int message_id;
    public int sender_id;
    public int receiver_id;
    public String message;
    public byte[] picture;
    public int reply_message_id;
    public String message_time;
    public String message_type;

    public Action(int actionId) throws SQLException {
        ArrayList<Object> action = LogsDataBase.getAction(actionId);
        this.id = (int) action.get(0);
        this.change_type = (ActionType) action.get(1);
        this.message_id = (int) action.get(2);
        this.sender_id = (int) action.get(3);
        this.receiver_id = (int) action.get(4);
        this.message = (String) action.get(5);
        this.picture = (byte[]) action.get(6);
        this.reply_message_id = (int) action.get(7);
        this.message_time = (String) action.get(8);
        this.message_type = (String) action.get(9);
    }
}
