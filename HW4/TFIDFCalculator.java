import java.io.BufferedReader;
import java.io.BufferedWriter;
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
        TrieNode wholeIdfRoot = new TrieNode();

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
            StringBuilder documentContent = new StringBuilder();
            int lineCount = 0;
            String line;
            List<String> addedString = new ArrayList<>();
            boolean contain = false;

            while ((line = reader.readLine()) != null) {

                line = line.toLowerCase().replaceAll("[^a-z]+"," ").trim();

                    if (lineCount%5 == 0 && lineCount != 0) {
                    documentArray = documentContent.toString().trim().split(" ");
                    singleDocRoot = new TrieNode();

                    //同時處理每個文本token的trie和所有文本token的trie
                    for (String word : documentArray) {
                        insert(word,singleDocRoot);
                        addedString.add(word);
                        for (String token : addedString) {

                            if (!(token.equals(word))) {
                                contain = true;
                                break;
                            }
                        }
                        if (!contain) {
                            insert(word,wholeIdfRoot);

                        }
                        contain = false;

  
                    }
                    wholeDocRoot.add(singleDocRoot);
                    docSizeList.add(documentArray.length);
                    documentContent.setLength(0);
                    addedString.clear();
                }

                documentContent.append(line).append(" ");
                lineCount++;

                
            }
            documentArray = documentContent.toString().trim().split(" ");
            singleDocRoot = new TrieNode();
            for (String word : documentArray) {
                insert(word,singleDocRoot);
                addedString.add(word);
                        for (String token : addedString) {
                            if (!token.equals(word)) {
                                contain = true;
                                break;
                            }
                        }
                        if (!contain) {
                            insert(word,wholeIdfRoot);

                        }
                        contain = false;
            }
            wholeDocRoot.add(singleDocRoot);
	        docSizeList.add(documentArray.length);
            addedString.clear();
            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        
        }
        try {
            String result = "";
            for (int i = 0; i < stringArgumentArray.length; i++) {
                String argument = stringArgumentArray[i];
                String documentIndex = documentIndexArray[i];
                TrieNode currentRoot = wholeDocRoot.get(Integer.parseInt(documentIndex));
                int docSize = docSizeList.get(Integer.parseInt(documentIndex));

                termCount = getTermCount(argument, currentRoot);

                termFrequency = tf(termCount,docSize);

                idfValue = idf(wholeIdfRoot,argument,wholeDocRoot.size());

                tfIdfValue = termFrequency * idfValue;

                result += String.format("%.5f", tfIdfValue) + " ";

            }

            fileOutput(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static double tf(int number_term_in_doc,double documentSize) {        
        return number_term_in_doc / documentSize;
    }

    public static double idf(TrieNode idfRoot, String term,double documentsSize) {
        int count = 0;
        if (search(term,idfRoot)) {
            count = getTermCount(term, idfRoot);
        }
        
        System.out.println(count);
        
        return Math.log(documentsSize / count);
    }
    
    public static void fileOutput(String result) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt",true));
        writer.write(result);
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