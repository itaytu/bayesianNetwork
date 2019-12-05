import java.util.ArrayList;

public class node {

    private char varName;
    private ArrayList<node> parents;
    private ArrayList<node> children;
    private String[] myValues;

    private boolean isParent;
    private boolean isEvidence;
    private boolean isVisited;

    private cpt myCpt;

    public node(char varName){
        this.varName = varName;
        isEvidence = false;
        isVisited = false;
        isParent = false;
        children = new ArrayList<>();
        parents = new ArrayList<>();
    }

    public node(node tmpNode) {

    }

    public void addChildren(node child) {
        if(!children.contains(child))
            children.add(child);
    }

    public ArrayList<node> getChildren() {
        return children;
    }

    public void addParent(node parent) {
        if(!parents.contains(parent))
            parents.add(parent);
    }

    public ArrayList<node> getParents() {
        return parents;
    }

    public char getVar() {
        return varName;
    }


    public void setEvidence(boolean flag) {
        isEvidence = flag;
    }

    public boolean getEvidence(){ return isEvidence; }


    public void setVisited(boolean flag) {
        isVisited = flag;
    }

    public boolean getVisited() {
        return isVisited;
    }


    public void setParent(boolean parent) { isParent = parent; }

    public boolean getIsParent() { return isParent; }


    public void setCpt(ArrayList<String> nodeArray, node myNode) { myCpt = new cpt(nodeArray, myNode); }

    public cpt getMyCpt() { return myCpt; }


    public void setValues(ArrayList<String> nodeArray) {
        makeValues(nodeArray);
    }

    private void makeValues(ArrayList<String> nodeArray) {
        String stringValues = nodeArray.get(1);
        stringValues = stringValues.replaceAll(" ", "");
        stringValues = stringValues.substring(stringValues.indexOf("Values:") + "values".length()+1);
        myValues = stringValues.split(",");
    }

    public String[] getValues() { return myValues; }


}
