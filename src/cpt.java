import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class cpt implements Comparable<cpt>{

    private ArrayList<ArrayList<String>> cptTable;


    public cpt(ArrayList<String> nodeArray, node myNode) {
        cptTable = new ArrayList<>();
        fillCptTable(nodeArray, myNode);
    }

    public cpt(cpt cptTable) {
        this.cptTable = new ArrayList<>();
        for(ArrayList<String> cptTableRow : cptTable.cptTable) {
            this.cptTable.add((ArrayList<String>) cptTableRow.clone());
        }
    }

    public cpt() {
        cptTable = new ArrayList<>();
    }


    //TODO: find a way to merge with parents and without parents
    private void fillCptTable(ArrayList<String> nodeArray, node myNode){
        fillTableHeader(nodeArray, myNode);

        int numOfCols = cptTable.get(0).size();
        int numOfParents = myNode.getParents().size();

        if(numOfParents != 0) {
            for(int i = 4; i < nodeArray.size(); i++) {
                ArrayList<String> myNodeValues = new ArrayList<>(Arrays.asList(myNode.getValues()));
                String line = nodeArray.get(i);

                String parentsStringValues = line.substring(0, line.indexOf('=')-1);
                String[] parentsValues = parentsStringValues.split(",");

                String nodeStringValues = line.substring(line.indexOf('='));
                nodeStringValues = nodeStringValues.replace("=", "");
                String[] nodeValues = nodeStringValues.split(",");

                BigDecimal probability = new BigDecimal("0.0");
                BigDecimal complement = new BigDecimal("1.0");

                for (int j = 0; j < nodeValues.length - 1; j+=2){
                    ArrayList<String> cptRowValues = new ArrayList<>();
                    Collections.addAll(cptRowValues, parentsValues);
                    cptRowValues.add(nodeValues[j]);
                    myNodeValues.remove(nodeValues[j]);
                    cptRowValues.add(nodeValues[j+1]);
                    probability = probability.add(new BigDecimal(nodeValues[j+1]));
                    cptTable.add(cptRowValues);
                }

                complement = complement.subtract(probability);
                ArrayList<String> cptRowValues = new ArrayList<>();
                Collections.addAll(cptRowValues, parentsValues);
                cptRowValues.add(myNodeValues.get(0));
                cptRowValues.add(complement.toString());
                cptTable.add(cptRowValues);
            }
        }

        else {
            for(int i = 4; i < nodeArray.size(); i++) {
                ArrayList<String> myNodeValues = new ArrayList<>(Arrays.asList(myNode.getValues()));
                String line = nodeArray.get(i);

                line = line.replace("=", "");
                String[] nodeValues = line.split(",");
                BigDecimal probability = new BigDecimal("0.0");
                BigDecimal complement = new BigDecimal("1.0");

                for (int j = 0; j < nodeValues.length - 1; j+=2){
                    int index = 0;
                    ArrayList<String> cptRowValues = new ArrayList<>();
                    cptRowValues.add(nodeValues[j]);
                    myNodeValues.remove(nodeValues[j]);
                    cptRowValues.add(nodeValues[j+1]);
                    probability = probability.add(new BigDecimal(nodeValues[j+1]));
                    cptTable.add(cptRowValues);
                }
                complement = complement.subtract(probability);
                ArrayList<String> cptRowValues = new ArrayList<>();
                cptRowValues.add(myNodeValues.get(0));
                cptRowValues.add(complement.toString());
                cptTable.add(cptRowValues);
            }
        }
    }


    private void fillTableHeader(ArrayList<String> nodeArray, node myNode){
        ArrayList<String> header = new ArrayList<>();
        if(myNode.getParents().size() != 0) {
                for (node parent : myNode.getParents()) {
                    header.add(String.valueOf(parent.getVar()));
                }
                header.add(String.valueOf(myNode.getVar()));
                StringBuilder prob = new StringBuilder("P(" + myNode.getVar() + "|");
                for (node parent : myNode.getParents()) {
                    prob.append(parent.getVar()).append(",");
                }
                prob = new StringBuilder(prob.substring(0, prob.length() - 1));
                prob.append(")");
                header.add(prob.toString());

        }
        else {
            header.add(String.valueOf(myNode.getVar()));
            header.add("P(" + myNode.getVar() + ")");
        }
        cptTable.add(header);
    }


    @Override
    public int compareTo(cpt o) {
        int compare =  Integer.compare(this.cptTable.size(), o.cptTable.size());
        if(compare == 0) {
            int asciiOne = 0;
            int asciiTwo = 0;
            for (String var : this.cptTable.get(0)){
                if(!var.contains("P"))
                    asciiOne += var.charAt(0);
            }
            for (String var : o.cptTable.get(0)){
                if(!var.contains("P"))
                    asciiTwo += var.charAt(0);
            }
            if(asciiOne > asciiTwo)
                compare = 1;
            else
                compare = -1;
        }
        return compare;
    }


    public ArrayList<ArrayList<String>> getCptTable() {
        return cptTable;
    }

}
