/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.rhs.distributedpasswordcracking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Artjom
 */
public class SplitDictionary {

    static ArrayList<String> words = new ArrayList<String>();
    static ArrayList<ArrayList> arrayOfWords = new ArrayList<ArrayList>();

    private static void split() throws IOException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("webster-dictionary.txt");
            final BufferedReader dictionary = new BufferedReader(fileReader);
            while (true) {
                final String dictionaryEntry = dictionary.readLine();
                words.add(dictionaryEntry);
                if (dictionaryEntry == null) {

                    break;
                }
            }
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }

    public SplitDictionary() {
        splitDictionary();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        new SplitDictionary();
        System.out.println(words.size());
        System.out.println(arrayOfWords.size());
        split();


        System.out.println(words.size());
    }

    private void splitDictionary() {
        FileReader fileReader = null;
        boolean isFinished = false;
        try {
            try {
                fileReader = new FileReader("webster-dictionary.txt");
                final BufferedReader dictionary = new BufferedReader(fileReader);
                while (isFinished == false) {
                    final String dictionaryEntry = dictionary.readLine();
                    for (int i = 0; i < 12; i++) {
                        for (int y = 0; i < 9999; i++) {
                            words.add(dictionaryEntry);
                            if (dictionaryEntry == null) {
                                isFinished = true;
                                break;
                            }
                        }
                        arrayOfWords.add(words);
                        if (isFinished == true) {
                            break;
                        }
                    }

                }
            } finally {
                if (fileReader != null) {
                    fileReader.close();
                }
            }
        } catch (IOException ex) {//empty};
        }
    }
}
