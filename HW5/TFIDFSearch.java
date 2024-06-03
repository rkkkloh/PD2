import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public class TFIDFSearch {
    public static void main(String[] args) {
        String corpusIndex = args[0].substring("corpus".length());
        List<List<String>> docs = new ArrayList<>();
        String[] stringArgumentArray = null;
        List<String[]> stringArgumentList = new ArrayList<>();
        String[] documentArray;
        Trie singleDocRoot;
        List<Trie> wholeDocRoot = new ArrayList<>();
        double tfIdfValue = 0;
        Trie wholeIdfRoot = new Trie();
        try {
            FileInputStream fis = new FileInputStream("corpus" + corpusIndex + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);

            Indexer indexer = (Indexer) ois.readObject();
            ois.close();
            fis.close();
            
            BufferedReader reader = new BufferedReader(new StringReader(indexer.documentContent.toString()));
            String documentContent = "";
            int lineCount = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                if (lineCount%5 == 0 && lineCount != 0) {
                    docs.add(new ArrayList<>(Arrays.asList(documentContent.split(" "))));
                    documentArray = documentContent.split(" ");

                    singleDocRoot = new Trie();
                    for (String word : documentArray) {
                        if (!(singleDocRoot.search(word))){
                            wholeIdfRoot.insert(word);
                        }
                        singleDocRoot.insert(word);
                    }
                    wholeDocRoot.add(singleDocRoot);
                    documentContent = "";
                }
                documentContent += line + " ";
                lineCount++;
            }
            docs.add(new ArrayList<>(Arrays.asList(documentContent.split(" "))));
            documentArray = documentContent.split(" ");

            singleDocRoot = new Trie();
            for (String word : documentArray) {
                if (!(singleDocRoot.search(word))){
                    wholeIdfRoot.insert(word);
                }
                singleDocRoot.insert(word);
            }
            wholeDocRoot.add(singleDocRoot);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

        try {
            BufferedReader argumentReader = new BufferedReader(new FileReader(args[1]));
            int requestQuantity = Integer.parseInt(argumentReader.readLine());
            String argument;
            List<TfIdf> tfIdfList = new ArrayList<>();
            boolean containsAll;
            boolean containsAny;
            boolean contains;
            double totalTfIdfValue = 0;
            Map<String,Double> andHashMap = new HashMap<>();
            Map<String,Double> orHashMap = new HashMap<>();

            while ((argument = argumentReader.readLine()) != null) {
                if (argument.contains("AND")) {
                    stringArgumentArray = argument.replaceAll("[ AND ]+", " ").split(" ");
                    for (int i = 0; i < wholeDocRoot.size(); i++) {
                        containsAll = true;
                        for (String token : stringArgumentArray) {
                            if (!wholeDocRoot.get(i).search(token)) {
                                containsAll = false;
                                break;
                            }
                        }
                        
                        if (containsAll) {
                            for (String token : stringArgumentArray) {
                                if (andHashMap.containsKey(token)) {
                                    tfIdfValue = andHashMap.get(token);
                                    totalTfIdfValue += tfIdfValue;
                                } else {
                                    tfIdfValue = tfIdfCalculate(docs.get(i), docs, token, wholeDocRoot.get(i), wholeIdfRoot);
                                    totalTfIdfValue += tfIdfValue;
                                    andHashMap.put(token,tfIdfValue);
                                }
                            }
                            tfIdfList.add(new TfIdf(totalTfIdfValue, i));
                            andHashMap.clear();
                            totalTfIdfValue = 0;
                        }
                    }
                } else if (argument.contains("OR")) {
                    stringArgumentArray = argument.replaceAll("[ OR ]+", " ").split(" ");
                    for (int i = 0; i < wholeDocRoot.size(); i++) {
                        containsAny = false;
                        for (String token : stringArgumentArray) {
                            if (wholeDocRoot.get(i).search(token)) {
                                containsAny = true;
                                break;
                            }
                        }
                        
                        if (containsAny) {
                            for (String token : stringArgumentArray) {
                                if (orHashMap.containsKey(token)) {
                                    tfIdfValue = orHashMap.get(token);
                                    totalTfIdfValue += tfIdfValue;
                                } else {
                                    tfIdfValue = tfIdfCalculate(docs.get(i), docs, token, wholeDocRoot.get(i), wholeIdfRoot);
                                    totalTfIdfValue += tfIdfValue;
                                    orHashMap.put(token,tfIdfValue);
                                }
                                
                            }
                            tfIdfList.add(new TfIdf(totalTfIdfValue, i));
                            totalTfIdfValue = 0;
                            orHashMap.clear();
                        }
                    }
                } else {
                    for (int i = 0; i < wholeDocRoot.size(); i++) {
                        contains = false;

                        if (wholeDocRoot.get(i).search(argument)) {
                            contains = true;
                        }
                        
                        if (contains) {
                            tfIdfValue = tfIdfCalculate(docs.get(i), docs, argument, wholeDocRoot.get(i), wholeIdfRoot);
                            tfIdfList.add(new TfIdf(tfIdfValue, i));
                            tfIdfValue = 0;
                        }
                    }
                }

                Collections.sort(tfIdfList, (a, b) -> {
                    if (Double.compare(b.tfIdfValue, a.tfIdfValue) == 0) {
                        return Integer.compare(a.index, b.index);
                    } else {
                        return Double.compare(b.tfIdfValue, a.tfIdfValue);
                    }
                });
    
                FileWriter writer = new FileWriter("output.txt", true);
                
                if (requestQuantity < tfIdfList.size()) {
                    for (int i = 0; i < requestQuantity; i++) {
                        writer.write(tfIdfList.get(i).index + " ");
                    }
                } else {
                    for (TfIdf info : tfIdfList) {
                        writer.write(info.index + " ");
                    }
    
                    for (int i = 0; i < (requestQuantity-tfIdfList.size()); i++) {
                        writer.write("-1 ");
                    }
                }
                writer.write("\n");
                writer.close();
                tfIdfList.clear();

            }
            
            argumentReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static double getTermCount(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return 0;
            }
        }
        return node.count;
    }

    public static double tf(List<String> doc, String term, Trie currentRoot) {
        double number_term_in_doc = getTermCount(term, currentRoot.root);

        return number_term_in_doc / doc.size();
    }

    public static double idf(List<List<String>> docs, String term, Trie wholeIdfRoot) {
        int number_doc_contain_term = 0;

        if (wholeIdfRoot.search(term)) {
            number_doc_contain_term = (int)getTermCount(term, wholeIdfRoot.root);
        }

        if (number_doc_contain_term == 0) {
            return 0;
        }

        return Math.log((double)docs.size() / number_doc_contain_term);
    }
    
    public static double tfIdfCalculate(List<String> doc, List<List<String>> docs, String term,Trie currentRoot, Trie wholeIdfRoot) {
        return tf(doc, term, currentRoot) * idf(docs, term, wholeIdfRoot);
    }
}

class TrieNode implements Serializable {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count = 0;
}

class Trie implements Serializable {
    TrieNode root = new TrieNode();
    int index;

    public void insert(String word) {
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

class TfIdf {
    double tfIdfValue;
    int index;

    public TfIdf(double tfIdfValue, int index) {
        this.tfIdfValue = tfIdfValue;
        this.index = index;
    }
}
