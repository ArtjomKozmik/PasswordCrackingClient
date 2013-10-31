/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.rhs.distributedpasswordcracking;

import com.sun.accessibility.internal.resources.accessibility;
import static dk.rhs.distributedpasswordcracking.CrackerCentralized.checkSingleWord;
import static dk.rhs.distributedpasswordcracking.CrackerCentralized.checkWordWithVariations;
import static dk.rhs.distributedpasswordcracking.SplitDictionary.words;
import dk.rhs.util.StringUtilities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.TIMEOUT;

/**
 *
 * @author Artjom
 */
public class split {

    static ArrayList<String> words = new ArrayList<String>();
    static ArrayList<String> wordToSplit = new ArrayList<String>();
    static ArrayList<List> arrayOfWords = new ArrayList<List>();
    private static MessageDigest messageDigest;
    private static final Logger LOGGER = Logger.getLogger("passwordCracker");

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private static void readFromFile() throws IOException {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader("webster-dictionary.txt");

            final BufferedReader dictionary = new BufferedReader(fileReader);
            while (true) {
                final String dictionaryEntry = dictionary.readLine();

                if (dictionaryEntry == null) {
                    break;
                }
                words.add(dictionaryEntry);
            }
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        int newNumber = 1;
        final long startTime = System.currentTimeMillis();
        final List<UserInfo> userInfos = PasswordFileHandler.readPasswordFile("passwords.txt");
        final List<UserInfoClearText> result = new ArrayList<UserInfoClearText>();
        readFromFile();
        System.out.println(words.size());
        splitDictionary();
        System.out.println("parts: " +arrayOfWords.size());
        System.out.println(arrayOfWords.get(2));
     
//        for (int y = 0; y < arrayOfWords.size(); y++) {
//            for (int i = 0; i < arrayOfWords.get(y).size(); i++) {
//                final List<UserInfoClearText> partialResult = checkWordWithVariations(arrayOfWords.get(y).get(i).toString(), userInfos);
//                result.addAll(partialResult);
//
//                System.out.println(arrayOfWords.get(y).get(i));
//                System.out.println(result);
////                System.out.println(i);
//
//                if (y == newNumber) {
//                    System.out.println(result);
//                    newNumber = newNumber + 1;
//                }
//            }
//        }
        final long endTime = System.currentTimeMillis();
        final long usedTime = endTime - startTime;
        System.out.println(result);
        System.out.println("Used time: " + usedTime / 1000 + " seconds = " + usedTime / 60000.0 + " minutes");
    }

    private static void splitDictionary() {
        int startPoint = 0;
        int lastIndex = words.size();
        int endPoint = 25000;
        int jump = endPoint;
        int numberOfSplits = 0;
        numberOfSplits = roundUpSplit(lastIndex, endPoint);

        for (int i = 0; i < numberOfSplits; i++) {
            arrayOfWords.add(words.subList(startPoint, endPoint));


            startPoint = startPoint + jump;
            endPoint = endPoint + jump;
            if (endPoint > lastIndex) {
                endPoint = lastIndex;
                System.out.println("endpoint: "+ endPoint);

            }

        }
    }

    /**
     * Checks a single word from a dictionary, against a list of encrypted
     * passwords. Tries different variations on the dictionary entry, like all
     * uppercase, adding digits to the end of the entry, etc.
     *
     * @param dictionaryEntry a single word from a dictionary, i.e. a possible
     * password
     * @param userInfos a list of user information records: username + encrypted
     * password
     */
    static List<UserInfoClearText> checkWordWithVariations(final String dictionaryEntry, final List<UserInfo> userInfos) {
        final List<UserInfoClearText> result = new ArrayList<UserInfoClearText>();

        final String possiblePassword = dictionaryEntry;
//        System.out.println("possible password: " + possiblePassword);
        final List<UserInfoClearText> partialResult = checkSingleWord(userInfos, possiblePassword);
        result.addAll(partialResult);

        final String possiblePasswordUpperCase = dictionaryEntry.toUpperCase();
//        System.out.println("possible Upper: " + possiblePassword);
        final List<UserInfoClearText> partialResultUpperCase = checkSingleWord(userInfos, possiblePasswordUpperCase);
        result.addAll(partialResultUpperCase);

        final String possiblePasswordCapitalized = StringUtilities.capitalize(dictionaryEntry);
        final List<UserInfoClearText> partialResultCapitalized = checkSingleWord(userInfos, possiblePasswordCapitalized);
        result.addAll(partialResultCapitalized);

        final String possiblePasswordReverse = new StringBuilder(dictionaryEntry).reverse().toString();
        final List<UserInfoClearText> partialResultReverse = checkSingleWord(userInfos, possiblePasswordReverse);
        result.addAll(partialResultReverse);

        for (int i = 0; i < 100; i++) {
            final String possiblePasswordEndDigit = dictionaryEntry + i;
            final List<UserInfoClearText> partialResultEndDigit = checkSingleWord(userInfos, possiblePasswordEndDigit);
            result.addAll(partialResultEndDigit);
        }

        for (int i = 0; i < 100; i++) {
            final String possiblePasswordStartDigit = i + dictionaryEntry;
            final List<UserInfoClearText> partialResultStartDigit = checkSingleWord(userInfos, possiblePasswordStartDigit);
            result.addAll(partialResultStartDigit);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100; j++) {
                final String possiblePasswordStartEndDigit = i + dictionaryEntry + j;
                final List<UserInfoClearText> partialResultStartEndDigit = checkSingleWord(userInfos, possiblePasswordStartEndDigit);
                result.addAll(partialResultStartEndDigit);
            }
        }

        return result;
    }

    /**
     * Check a single word (may include a single variation)from the dictionary
     * against a list of encrypted passwords
     *
     * @param userInfos a list of user information records: username + encrypted
     * password
     * @param possiblePassword a single dictionary entry (may include a single
     * variation)
     * @return the user information record, if the dictionary entry matches the
     * users password, or {@code  null} if not.
     */
    static List<UserInfoClearText> checkSingleWord(final List<UserInfo> userInfos, final String possiblePassword) {
        final byte[] digest = messageDigest.digest(possiblePassword.getBytes());
        final List<UserInfoClearText> results = new ArrayList<UserInfoClearText>();
        for (UserInfo userInfo : userInfos) {
            if (Arrays.equals(userInfo.getEntryptedPassword(), digest)) {
                results.add(new UserInfoClearText(userInfo.getUsername(), possiblePassword));
            }
        }
        return results;
    }

    private static int roundUpSplit(int lastIndex, int endPoint) {
        int numberOfSplits;
        if (lastIndex % endPoint > endPoint / 2) {
            numberOfSplits = lastIndex / endPoint;
        } else {
            numberOfSplits = lastIndex / endPoint + 1;
        }
        return numberOfSplits;
    }
}