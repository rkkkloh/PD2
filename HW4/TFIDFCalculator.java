import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TFIDFCalculator {
    public static void main(String args[]) {

        String inputStringArgument;
        String inputDocumentIndex;
        String[] stringArgumentArray = null;
        String[] documentIndexArray = null;
        List<String> documenList = new ArrayList<>();

        // handle input arguments
        try {

            BufferedReader reader = new BufferedReader(new FileReader(args[1]));
            String line;

            inputStringArgument = reader.readLine();
            inputDocumentIndex = reader.readLine();

            stringArgumentArray = inputStringArgument.split(" ");
            documentIndexArray = inputDocumentIndex.split(" ");

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // grouping content into a document
        try {
            BufferedReader reader= new BufferedReader(new FileReader(args[0]));
            int lineCount = 0;
            String line;
            String documentContent = "";

            while ((line = reader.readLine()) != null) {

                line = line.toLowerCase().replaceAll("[^a-z]+"," ").trim();
                

                if (lineCount%5 == 0 && lineCount != 0) {
                    documenList.add(documentContent);

		        documentContent = "";
                }

                documentContent += line + " ";
                lineCount++;
            }
            if (!documentContent.isEmpty()) {
                    documenList.add(documentContent);
            }
            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        
        }

        List<String[]> documentContentArrayList = new ArrayList<>();
        for (int i = 0; i < documentIndexArray.length; i++) {
            String[] documentContent = documenList.get(Integer.parseInt(documentIndexArray[i])).split(" ");
            documentContentArrayList.add(documentContent);
        }
        
        TrieNode root = new TrieNode();

        for (int i = 0; i < documentContentArrayList.size(); i++){
            for (int j = 0; j < documentContentArrayList.get(i).length; j++) {
                insert(documentContentArrayList.get(i)[j],root);
            }
            
        }
        
    }

    public static void insert(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
    }

    public boolean search(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
}

class Trie {
    TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }
}
