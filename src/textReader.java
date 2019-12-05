import java.io.*;
import java.util.ArrayList;

public class textReader {

    //Parse File from Text into ArrayList
    public static ArrayList<ArrayList<String>> fileAsArray(String filename) throws IOException {
        ArrayList<ArrayList<String>> fileArray = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        while(br.ready()) {
            ArrayList<String> tmpFileArray = new ArrayList<>();
            String line = br.readLine();
            while(line != null) {
                if(line.length() == 0)
                    break;
                tmpFileArray.add(line);
                line = br.readLine();
            }
            if(!tmpFileArray.isEmpty())
                fileArray.add(tmpFileArray);
        }
        return fileArray;
    }

    //Create charArray for variables
    public static char[] objectAsCharArray(ArrayList<String> objectArray, String variable) {
            for (String objectLine : objectArray) {
                if(objectLine.contains(variable)) {
                   String tmpLine = objectLine.replace(" ", "");
                   tmpLine = tmpLine.substring(tmpLine.indexOf(variable) + variable.length() + 1);
                   String[] objStringArray = tmpLine.split(",");
                   char[] objCharArray = new char[objStringArray.length];
                   for (int i = 0; i < objCharArray.length; i ++) {
                       objCharArray[i] = objStringArray[i].charAt(0);
                   }
                   return objCharArray;
                }
            }
        return null;
    }

    //create StringArray for Queries
    public static String[] queriesAsStringArray(ArrayList<String> queriesArray) {
        String[] queriesStringArray = new String[queriesArray.size()-1];
        for (int i = 1; i< queriesArray.size(); i++) {
            queriesStringArray[i-1] = queriesArray.get(i);
        }
        return queriesStringArray;
    }

}
