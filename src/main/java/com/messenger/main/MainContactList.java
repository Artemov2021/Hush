package com.messenger.main;

import com.messenger.database.ChatsDataBase;
import com.messenger.database.ContactsDataBase;
import com.messenger.database.UsersDataBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


public class MainContactList {
    public static void loadContacts(int mainUserId,VBox mainContactsVBox,AnchorPane mainAnchorPane) throws SQLException, IOException {
        mainContactsVBox.getChildren().clear();
        mainContactsVBox.setSpacing(4.0);
        int[] allContactsId = ContactsDataBase.getContactsIdList(mainUserId);
        int[] lastContacts = Arrays.copyOfRange(allContactsId, allContactsId.length - Math.min(allContactsId.length, 20), allContactsId.length);
        for (int contactId: lastContacts) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
        }
    }
    public static void loadCustomContacts(int mainUserId,int[] contactsId,VBox mainContactsVBox,AnchorPane mainAnchorPane) throws IOException, SQLException {
        for (int contactId: contactsId) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
        }
    }
    public static void loadMoreContacts(int mainUserId,VBox mainContactsVBox,AnchorPane mainAnchorPane,int lastContactId) throws SQLException, IOException {
        int[] leftContacts = ContactsDataBase.getContactsIdListAfterContact(mainUserId,lastContactId);
        int[] lastContacts = Arrays.copyOfRange(leftContacts, leftContacts.length - Math.min(leftContacts.length,20), leftContacts.length);
        int[] reversedLastContacts = IntStream.range(0, lastContacts.length)
                .map(i -> lastContacts[lastContacts.length - 1 - i]) // Access elements in reverse order
                .toArray();

        for (int contactId: reversedLastContacts) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainContactList.class.getResource("/main/fxml/MainContact.fxml"));
            Pane contactRoot = fxmlLoader.load();

            MainContact contactPane = fxmlLoader.getController();
            String contactName = UsersDataBase.getNameWithId(contactId);
            List<Object> lastMessageWithId = ChatsDataBase.getLastMessageWithId(mainUserId,contactId);
            String lastMessageTime = ChatsDataBase.getLastMessageTime(mainUserId,contactId);

            contactPane.setMainUserId(mainUserId);
            contactPane.setContactId(contactId);
            contactPane.setName(contactName);
            contactPane.setAvatar(contactId);
            contactPane.setMessage((String)lastMessageWithId.get(0),(int)lastMessageWithId.get(1));
            contactPane.setTime(lastMessageTime);
            contactPane.setPaneId(contactId);
            contactPane.setMainAnchorPane(mainAnchorPane);

            mainContactsVBox.getChildren().add(contactRoot);
        }
    }
    public static void addContactToList(int mainUserId,int contactId,VBox mainContactsVBox,AnchorPane mainAnchorPane) throws SQLException, IOException {
        loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
    }
    public static void removeContact(VBox mainContactsVBox,int contactId) {
        Pane targetContactPane = (Pane) mainContactsVBox.lookup("#mainContactAnchorPane"+contactId);
        mainContactsVBox.getChildren().remove(targetContactPane);
    }


    private static void loadContactFromFXML(int mainUserId, int contactId, VBox mainContactsVBox, AnchorPane mainAnchorPane) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainContactList.class.getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContact contactPane = fxmlLoader.getController();
        String contactName = UsersDataBase.getNameWithId(contactId);
        List<Object> lastMessageWithId = ChatsDataBase.getLastMessageWithId(mainUserId,contactId);
        String lastMessageTime = ChatsDataBase.getLastMessageTime(mainUserId,contactId);

        contactPane.setMainUserId(mainUserId);
        contactPane.setContactId(contactId);
        contactPane.setName(contactName);
        contactPane.setAvatar(contactId);
        contactPane.setMessage((String)lastMessageWithId.get(0),(int)lastMessageWithId.get(1));
        contactPane.setTime(lastMessageTime);
        contactPane.setPaneId(contactId);
        contactPane.setMainAnchorPane(mainAnchorPane);
        contactPane.setMainAnchorPaneId();
        contactPane.setMainContactVBox(mainContactsVBox);

        mainContactsVBox.getChildren().add(0,contactRoot);
    }

}
