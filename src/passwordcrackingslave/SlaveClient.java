/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package passwordcrackingslave;

import dk.rhs.distributedpasswordcracking.PasswordFileHandler;
import dk.rhs.distributedpasswordcracking.UserInfo;
import dk.rhs.distributedpasswordcracking.UserInfoClearText;
import dk.rhs.util.StringUtilities;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Artjom
 */
public class SlaveClient {

    static ArrayList<String> words = new ArrayList<String>();
    private static MessageDigest messageDigest;
    private static final Logger LOGGER = Logger.getLogger("passwordCracker");
    static boolean noMoreWords = false;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Connects to the server, cracks passwords and sends back the result
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        final long startTime = System.currentTimeMillis();
        final List<UserInfo> userInfos = PasswordFileHandler.readPasswordFile("passwords.txt");
        while (noMoreWords == false) {
            readyWords();


            final List<UserInfoClearText> result = new ArrayList<UserInfoClearText>();
            crackPasswords(userInfos, result);
            getResults(startTime, result);
            sendResult2(result.toString());
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

        final List<UserInfoClearText> partialResult = checkSingleWord(userInfos, possiblePassword);
        result.addAll(partialResult);

        final String possiblePasswordUpperCase = dictionaryEntry.toUpperCase();
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

    /**
     * Ready words take the words sent by server and adds them to and ArrayList
     */
    private static void readyWords() {
        words.clear();
        List<Object> wordsObject = getWords();
        if (wordsObject.size() > 0) {
            System.out.println("Recieved words");
            
            for (int i = 0; i < wordsObject.size(); i++) {
                words.add(wordsObject.get(i).toString());
                
            }
        }
        
        else {
            System.out.println("No more words");
            noMoreWords= true;
        }
    }

    /**
     * Initiates the method to check words with variations and sends one word at
     * a time
     */
    private static void crackPasswords(final List<UserInfo> userInfos, final List<UserInfoClearText> result) {
        for (int i = 0; i < words.size(); i++) {

            final List<UserInfoClearText> partialResult = checkWordWithVariations(words.get(i).toString(), userInfos);
            result.addAll(partialResult);
        }
    }

    /**
     * Gets result and notes the time when the program finishes working
     *
     * @param startTime time when program started
     * @param result result of cracking
     */
    private static void getResults(final long startTime, final List<UserInfoClearText> result) {
        final long endTime = System.currentTimeMillis();
        final long usedTime = endTime - startTime;
        if (result.size() == 0) {
            System.out.println("No passwords found");
        } else {
            System.out.println(result);
        }
        System.out.println("Used time: " + usedTime / 1000 + " seconds = " + usedTime / 60000.0 + " minutes");
    }

    private static void sendResult2(java.lang.String result) {
        master.MasterWebService_Service service = new master.MasterWebService_Service();
        master.MasterWebService port = service.getMasterWebServicePort();
        port.sendResult2(result);
    }

    private static java.util.List<java.lang.Object> getWords() {
        master.MasterWebService_Service service = new master.MasterWebService_Service();
        master.MasterWebService port = service.getMasterWebServicePort();
        return port.getWords();
    }
}
