import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;

public class TFIDFCalculator {
    public static void main(String args[]) {

        String inputStringArgument;
        String inputDocumentIndex;
        String[] stringArgumentArray = null;
        String[] documentIndexArray = null;
        String[] documentArray;
        TrieNode singleDocRoot;
        List<TrieNode> wholeDocRoot = new ArrayList<>();
        List<Integer> docSizeList = new ArrayList<>();
        int termCount = 0;
        double termFrequency;
        double idfValue;
        double tfIdfValue;

        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            BufferedReader reader= new BufferedReader(new FileReader("docs.txt"));
            int lineCount = 0;
            String line;
            String documentContent = "";

            while ((line = reader.readLine()) != null) {

                line = line.toLowerCase().replaceAll("[^a-z]+"," ").trim();
                

                if (lineCount%5 == 0 && lineCount != 0) {
                    documentArray = documentContent.trim().split(" ");
                    singleDocRoot = new TrieNode();
                    for (String word : documentArray) {
                        insert(word,singleDocRoot);
                    }
                    wholeDocRoot.add(singleDocRoot);
                    docSizeList.add(documentArray.length);
                    documentContent = "";
                }

                documentContent += line + " ";
                lineCount++;

                
            }
            documentArray = documentContent.trim().split(" ");
            singleDocRoot = new TrieNode();
            for (String word : documentArray) {
                insert(word,singleDocRoot);
            }
            wholeDocRoot.add(singleDocRoot);
	    docSizeList.add(documentArray.length);
            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        
        }
        try {
            for (int i = 0; i < stringArgumentArray.length; i++) {
                String argument = stringArgumentArray[i];
                String documentIndex = documentIndexArray[i];
                TrieNode currentRoot = wholeDocRoot.get(Integer.parseInt(documentIndex));
                int docSize = docSizeList.get(Integer.parseInt(documentIndex));

                termCount = getTermCount(argument, currentRoot);

                termFrequency = tf(termCount,docSize);

                idfValue = idf(wholeDocRoot,argument,wholeDocRoot.size());

                tfIdfValue = termFrequency * idfValue;

                fileOutput(tfIdfValue);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static double tf(int number_term_in_doc,double documentSize) {        
        return number_term_in_doc / documentSize;
    }

    public static double idf(List<TrieNode> idfRoot, String term,double documentsSize) {
        int count = 0;
        for (TrieNode root : idfRoot) {
            if (search(term,root)) {
                count++;
            }
        }
        
        return Math.log(documentsSize / count);
    }
    
    public static void fileOutput(double tfIdfValue) throws IOException {
        FileWriter writer = new FileWriter("output.txt",true);
        writer.write(String.format("%.5f", tfIdfValue) + " ");
        writer.close();
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
        node.count++;
    }

    public static boolean search(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }

    public static int getTermCount(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return 0;
            }
        }
        return node.count;
    }   
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count = 0;
}