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
        System.out.println(Arrays.toString(allContactsId));

        int[] firstContactsId = Arrays.copyOf(IntStream.range(0, allContactsId.length)
                .map(i -> allContactsId[allContactsId.length - 1 - i])
                .toArray(),12);
        int[] leftContactsId = Arrays.copyOfRange(IntStream.range(0, allContactsId.length)
                .map(i -> allContactsId[allContactsId.length - 1 - i])
                .toArray(), 12, allContactsId.length);
        for (int contactId: firstContactsId) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
        }
        for (int contactId: leftContactsId) {
            loadEmptyContactFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
        }
    }
    public static void loadCustomContacts(int mainUserId,int[] contactsId,VBox mainContactsVBox,AnchorPane mainAnchorPane) throws IOException, SQLException {
        for (int contactId: contactsId) {
            loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
        }
    }
    public static void addContactToList(int mainUserId,int contactId,VBox mainContactsVBox,AnchorPane mainAnchorPane) throws SQLException, IOException {
        loadContactFromFXML(mainUserId,contactId,mainContactsVBox,mainAnchorPane);
    }


    private static void loadContactFromFXML(int mainUserId, int contactId, VBox mainContactsVBox, AnchorPane mainAnchorPane) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainContactList.class.getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContact contactPane = fxmlLoader.getController();
        String contactName = UsersDataBase.getNameWithId(contactId);
        List<Object> lastMessageWithId = ChatsDataBase.getLastMessageWithId(mainUserId,contactId);
        String lastMessageTime = ChatsDataBase.getLastMessageTime(mainUserId,contactId);

        contactPane.setMainUserId(mainUserId);
        contactPane.setName(contactName);
        contactPane.setAvatar(contactId);
        contactPane.setMessage((String)lastMessageWithId.get(0),(int)lastMessageWithId.get(1));
        contactPane.setTime(lastMessageTime);
        contactPane.setPaneId(contactId);
        contactPane.setMainAnchorPane(mainAnchorPane);

        mainContactsVBox.getChildren().add(contactRoot);
    }
    private static void loadEmptyContactFXML(int mainUserId, int contactId, VBox mainContactsVBox, AnchorPane mainAnchorPane) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainContactList.class.getResource("/main/fxml/MainContact.fxml"));
        Pane contactRoot = fxmlLoader.load();

        MainContact contactPane = fxmlLoader.getController();

        contactPane.setMainUserId(mainUserId);
        contactPane.setPaneId(contactId);
        contactPane.setMainAnchorPane(mainAnchorPane);
        contactPane.deleteAllElements();

        mainContactsVBox.getChildren().add(contactRoot);
    }

}
