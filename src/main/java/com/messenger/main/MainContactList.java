package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;


public class MainContactList {
    public static void loadContacts(int mainUserId,VBox mainContactsVBox) throws SQLException, IOException {
        mainContactsVBox.setSpacing(4.0);
        int[] contactsId = ContactsDataBase.getContactsIdList(mainUserId);
        for (int contactId: contactsId) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox);
        }
    }
    public static void loadCustomContacts(int mainUserId,int[] contactsId,VBox mainContactsVBox) throws IOException, SQLException {
        for (int contactId: contactsId) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox);
        }
    }
    public static void addContactToList(int mainUserId,int contactId,VBox mainContactsVBox) throws SQLException, IOException {
        loadContactFromFXML(mainUserId,contactId,mainContactsVBox);
    }





    private static void loadContactFromFXML(int mainUserId,int contactId,VBox mainContactsVBox) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainContactList.class.getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContact contactPane = fxmlLoader.getController();
        String contactName = UsersDataBase.getNameWithId(contactId);
        String lastMessage = ChatsDataBase.getLastMessage(mainUserId,contactId);
        String lastMessageTime = ChatsDataBase.getLastMessageTime(mainUserId,contactId);

        contactPane.setName(contactName);
        contactPane.setAvatar(contactId);
        contactPane.setMessage(lastMessage);
        contactPane.setTime(lastMessageTime);
        contactPane.setPaneId(String.valueOf(contactId));

        mainContactsVBox.getChildren().add(0,contactRoot);
    }

}
