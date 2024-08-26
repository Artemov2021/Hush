package com.messenger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log {
    public static void writeNewExceptionLog(Exception e) throws IOException {
        FileWriter logWriter = new FileWriter("exception-log.txt");

        // message beginning: time + exception message
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM HH:mm");
        String time = myDateObj.format(myFormatObj);

        String message = time + " " + String.format("%25s\n",e.getMessage());

        // the longest length of elements
        int theLongestClassLength = theBiggestLength(getExceptionElements(e.getStackTrace(),"class"));
        int theLongestMethodLength = theBiggestLength(getExceptionElements(e.getStackTrace(),"method"));
        int theLongestLineNumberLength = theBiggestLength(getExceptionElements(e.getStackTrace(),"number"));

        for (StackTraceElement element: e.getStackTrace()) {
            Pattern classPattern = Pattern.compile("com\\.messenger");
            Matcher classMatcher = classPattern.matcher(element.getClassName());
            if (classMatcher.find()) {
                byte spaces = (byte) ((e.getStackTrace()[0].equals(element)) ? 2 : 7);
                message += String.format(
                        ("%7s") +
                        ("| class: %" + theLongestClassLength + "s" + " |" ) +
                        (" method: %" + theLongestMethodLength + "s" + " |" ) +
                        (" line: %" + theLongestLineNumberLength + "s" + " |" ) +
                        "\n" ,"", element.getClassName(), element.getMethodName(),element.getLineNumber()
                );
            }
        }
        logWriter.write(message);
        logWriter.close();
    }

    public static void writeNewActionLog(String message) throws IOException {
        FileWriter actionWriter = new FileWriter("actions-log.txt",true);
        actionWriter.write(message);
        actionWriter.close();
    }

    public static void clearActionLog() throws IOException {
        FileWriter actionWriter = new FileWriter("actions-log.txt");
        actionWriter.write("");
        actionWriter.close();
    }

    private static <T> int theBiggestLength(ArrayList<T> array) {
        int maxLength = 0;
        for (T element : array) {
            int length = element.toString().length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    private static ArrayList<String> getExceptionElements(StackTraceElement[] array, String element) {
        ArrayList<String> elements = new ArrayList<>();
        for (StackTraceElement e: array) {
            Pattern classPattern = Pattern.compile("com\\.messenger");
            Matcher classMatcher = classPattern.matcher(e.getClassName());
            if (classMatcher.find()) {
                switch (element) {
                    case "class":
                        elements.add(e.getClassName());
                        break;
                    case "method":
                        elements.add(e.getMethodName());
                        break;
                    case "number":
                        elements.add(String.valueOf(e.getLineNumber()));
                        break;
                }
            }
        }
        return elements;
    }


}
