package com.messenger.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ContactsDataBase {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
    private static final String user = "root";
    private static final String password = "112233";

    public static int[] getContactsIdList(int mainUserId) throws SQLException {
        List<Integer> contactsIdList = new ArrayList<>();
        String statement = "SELECT contact_id FROM contacts WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1,mainUserId);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                contactsIdList.add(result.getInt("contact_id"));
            }
        }
        return contactsIdList.stream().mapToInt(Integer::intValue).toArray();
    }
    public static int[] getMatchedUsersId(int mainUserId,String userNamePiece) throws SQLException {
        /* For example: user enters "Ar" and that method gives all users id, which name
           beginns with "Ar" ( e.g. Artur,Ariana )  */
        int[] contactsId = getContactsIdList(mainUserId);
        return Arrays.stream(contactsId)
                .filter(contactId -> getNameMatchingWithId(contactId,userNamePiece.toLowerCase())).toArray();
    }
    public static void addContact(int mainUserId,int contactId) throws SQLException {
        String statement = "INSERT INTO contacts (user_id,contact_id) VALUES (?,?)";

        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, mainUserId);
            preparedStatement.setInt(2, contactId);
            preparedStatement.executeUpdate();
        }
    }






    private static boolean getNameMatching(String name,String namePiece) {
        String pattern = String.format("^%s",namePiece);
        Pattern patternObject = Pattern.compile(pattern);

        Matcher matcher = patternObject.matcher(name);
        return matcher.find();
    }
    private static boolean getNameMatchingWithId(int id,String namePiece) {
        try {
            String[] nameSplited = UsersDataBase.getNameWithId(id).split("\\s+");
            return Arrays.stream(nameSplited).anyMatch(name -> getNameMatching(name.toLowerCase(),namePiece));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
