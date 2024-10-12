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

            mainContactsVBox.getChildren().add(contactRoot);
        }
    }
    public static void loadCustomContacts(int mainUserId,int[] contactsId,VBox mainContactsVBox) throws IOException, SQLException {
        for (int contactId: contactsId) {
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

            mainContactsVBox.getChildren().add(contactRoot);
        }
    }






//    public void addContactToList(int contactId) throws SQLException {
//
//
//        // opens dialog pane
//        userPane.setOnMouseClicked(mouseEvent -> {
//            try {
//
//                // Load FXML new contact window ( pane )
//                FXMLLoader loader = new FXMLLoader(MainContactList.class.getResource("/main/MainDialog.fxml"));
//                Parent dialogRoot = loader.load();
//
//                // Pass the anchor pane of main window to settings controller file
//                DialogController dialog = loader.getController();
//                dialog.setContactId(contactId);
//                dialog.setMainUserId(mainUserId);
//                dialog.setMainAnchorPane(mainAnchorPane);
//                dialog.initializeWithValue();
//
//                mainAnchorPane.getChildren().removeIf(child -> Objects.equals(child.getId(), "dialogBackgroundPane"));
//                mainAnchorPane.getChildren().add(0,dialogRoot);
//
//
//            } catch (SQLException | IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    private String convertTimeToHours(String time) {
//        String hoursPattern = "(\\d+):(\\d+)";
//        Pattern compliedPattern = Pattern.compile(hoursPattern);
//        Matcher matcher = compliedPattern.matcher(time);
//        if (matcher.find()) {
//            return String.format("%s:%s",matcher.group(1),matcher.group(2));
//        }
//        return null;
//    }
//
//    public void addUserContactsToList() throws SQLException {
//        // List of users id's
//        ArrayList<Integer> contacts = DetailedDataBase.getContactsIds(mainUserId);
//        for (int contactId: contacts) {
//            addContactToList(contactId);
//        }
//    }
//    public void addCustomContactsToList(ArrayList<Integer> contactsIds) throws SQLException {
//        for (int contactId: contactsIds) {
//            addContactToList(contactId);
//        }
//    }


}
