import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class graphCreator {


    static ArrayList<node> createGraph(ArrayList<ArrayList<String>> file) {
        char[] varCharArray = textReader.objectAsCharArray(file.get(0), "Variables");
        ArrayList<node> nodes = createNodes(varCharArray);
        createConnections(file, varCharArray.length, nodes);
        createCptTables(file, nodes);
        return nodes;
    }


    private static ArrayList<node> createNodes(char[] varCharArray) {
        ArrayList<node> nodes = new ArrayList<>();
        for(char var : varCharArray) {
            nodes.add(new node(var));
        }
        return nodes;
    }


    private static void createConnections(ArrayList<ArrayList<String>> fileArray, int numOfNodes, ArrayList<node> nodes) {
        fileArray.remove(0);
        for (int i = 0; i < numOfNodes; i++) {
            ArrayList<String> tmpObject = fileArray.get(i);
            String childString = tmpObject.get(0);
            childString = childString.replace("Var", "").replace(" ", "");
            char childChar = childString.charAt(0);
            node child = getNode(childChar, nodes);
            if(!tmpObject.get(2).contains("none")) {
                char[] parentsCharArray = textReader.objectAsCharArray(tmpObject, "Parents");
                for (char parentChar : parentsCharArray) {
                    node parent = getNode(parentChar, nodes);
                    child.addParent(parent);
                    parent.addChildren(child);
                }
            }
        }
    }


    private static void createCptTables(ArrayList<ArrayList<String>> fileArray, ArrayList<node> nodes) {
        for(int i = 0; i < nodes.size(); i ++) {
            nodes.get(i).setValues(fileArray.get(i));
            nodes.get(i).setCpt(fileArray.get(i), nodes.get(i));
        }
    }


    static node getNode(char name, ArrayList<node> nodes) {
        for (node tmp : nodes) {
            if (tmp.getVar() == (name))
                return tmp;
        }
        return null;
    }


    static int getIndex(node tmp, ArrayList<node> nodes) {
        for (int i = 0; i < nodes.size();i++) {
            if(tmp.getVar() == nodes.get(i).getVar())
                return i;
        }
        return -1;
    }


}
