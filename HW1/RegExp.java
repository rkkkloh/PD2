import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class RegExp {
    
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("argument error!");
            return;
        }
            
        String str1 = args[1];
        String str2 = args[2];
        int s2Count = Integer.parseInt(args[3]);
        
        File file = new File(args[0]);
        if (file.length() == 0) {
            System.out.println("Y,N,N,N");
        }

        //For your testing of input correctness
        //System.out.println("The input file:"+args[0]);
        //System.out.println("str1="+str1);
        //System.out.println("str2="+str2);
        //System.out.println("num of repeated requests of str2 = "+s2Count);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase();
                String reverse = "";

                for(int i = line.length()-1;i >= 0;i--)
                    reverse += line.charAt(i);
                if (line.equals(reverse)) {
                    System.out.print("Y,");
                } else 
                    System.out.print("N,");

                if (containsSubstring(line, str1))
                    System.out.print("Y,");
                else
                    System.out.print("N,");

                int count = countOccurrences(line, str2);
                if (count >= s2Count)
                    System.out.print("Y,");
                else
                    System.out.print("N,");

                if (containsSubstring(line,"a") && containsSubstring(line,"bb")) {
                    if (indexOf(line,"a",0) < lastIndexOf(line,"bb"))
                        System.out.println("Y");
                    else 
                        System.out.println("N");
                } else
                    System.out.println("N");
                
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countOccurrences(String line, String str) {
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = indexOf(line ,str, lastIndex)) != -1) {
            count++;
            lastIndex += 1;
        }
        return count;
    }

    public static int indexOf(String str, String subString, int startingIndex) {
        if (str == null || subString == null)
            return -1;
        for (int i = startingIndex; i <= str.length() - subString.length(); i++) {
            if (subString.length() == 0)
                return i;
            for (int j = 0; j < subString.length(); j++) {
                if (subString.charAt(j) != str.charAt(j+i)) 
                    break;
                if (j == subString.length() - 1)
                    return i;
            }
            
        }
        return -1;
    }

    public static int lastIndexOf(String str, String subString) {
        if (str == null || subString == null)
            return -1;
        for (int i = str.length() - subString.length() ; i >= 0; i--) {
            if (subString.length() == 0)
                return i;
            for (int j = 0; j < subString.length(); j++) {
                if (subString.charAt(j) != str.charAt(j+i)) 
                    break;
                if (j == subString.length() - 1)
                    return i;
                
            }
            
        }
        return -1;
    }

    public static boolean containsSubstring (String str, String target) {
        if (str == null || target == null) 
            return false;

        for (int i = 0; i <= str.length() - target.length(); i++) {
            if (matchesSubstring (str, target, i))
                return true;
            
        }

        return false;
        
    }

    public static boolean matchesSubstring (String str, String target, int startingIndex) {
        for (int j = 0; j < target.length(); j++) {
            if (str.charAt(startingIndex + j) != target.charAt(j))
                return false;
        }

        return true;
    }
}