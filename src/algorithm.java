import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class algorithm {

    static int addOperations;
    static int multiplyOperations;

    static String isIndependent(String query, ArrayList<node> nodes) {
        cleanGraph(nodes);
        ArrayList<node> nodesInQuery = nodesInQuery(query, nodes);

        node source = nodesInQuery.get(0);
        node target = nodesInQuery.get(1);
        if(nodesInQuery.size() > 2) {
            for(int i = 2; i < nodesInQuery.size(); i++) {
                node evidence = nodesInQuery.get(i);
                evidence.setEvidence(true);
            }
        }

        Queue<node> myQueue = new LinkedList<>();
        for(node parent : source.getParents()){
            parent.setParent(true);
            myQueue.add(parent);
        }
        for(node child : source.getChildren()) {
            child.setParent(false);
            myQueue.add(child);
        }
        source.setVisited(true);

        while(!myQueue.isEmpty()) {
            node queueNode = myQueue.poll();
            if(queueNode.getVar() == target.getVar())
                return "no";

            if(!queueNode.getVisited()) {
                if (queueNode.getIsParent() && !queueNode.getEvidence()) {
                    for (node parent : queueNode.getParents()) {
                        parent.setParent(true);
                        myQueue.add(parent);
                    }
                    for (node child : queueNode.getChildren()) {
                        child.setParent(false);
                        myQueue.add(child);
                    }
                } else if (!queueNode.getIsParent() && !queueNode.getEvidence()) {
                    for (node child : queueNode.getChildren()) {
                        child.setVisited(false);
                        child.setParent(false);
                        myQueue.add(child);
                    }
                } else if (!queueNode.getIsParent() && queueNode.getEvidence()) {
                    for (node parent : queueNode.getParents()) {
                        parent.setVisited(false);
                        parent.setParent(true);
                        myQueue.add(parent);
                    }
                }
                queueNode.setVisited(true);
            }
        }
        return "yes";
    }




    static String variableElimination(String query, ArrayList<node> nodes) {
        String ans;
        cleanGraph(nodes);
        addOperations = 0;
        multiplyOperations = 0;
        ArrayList<String[]> myQuery = nodesInVariableElmination(query, nodes);
        ArrayList<String[]> hiddenVariables = new ArrayList<>();
        for(int i = 0; i < myQuery.size(); i++) {
            if (myQuery.get(i).length == 1){
                hiddenVariables.add(myQuery.get(i));
                myQuery.remove(i);
                i--;
            }
        }

        ArrayList<String> ancestors = findAncestors(myQuery, nodes);

        //use only tables of relevant nodes - ancestors
        ArrayList<cpt> myCpts = new ArrayList<>();
        for (node myNode : nodes) {
            if(ancestors.contains(Character.toString(myNode.getVar()))) {
                cpt myCpt = new cpt(myNode.getMyCpt());
                myCpts.add(myCpt);
            }
            else{
                for (int i = 0; i < hiddenVariables.size(); i++){
                    if (hiddenVariables.get(i)[0].equals(Character.toString(myNode.getVar())))
                        hiddenVariables.remove(i);
                }
            }
        }


        ans = findInCpt(myQuery, myCpts);
        if(!ans.isEmpty())
            return ans;

        minimizeCpts(myQuery, myCpts);

/*        for (cpt myCpt: myCpts){
            for (ArrayList<String> rowInCpt : myCpt.getCptTable()){
                System.out.println(rowInCpt.toString());
            }
            System.out.println();
        }*/

        for (String[] hiddenVariable : hiddenVariables) {
            PriorityQueue<cpt> cptPriorityQueue = new PriorityQueue<>();
            for (int i = 0; i < myCpts.size(); i++) {
                if (myCpts.get(i).getCptTable().get(0).contains(hiddenVariable[0])) {
                    cptPriorityQueue.add(myCpts.get(i));
                    myCpts.remove(i);
                    i--;
                }
            }

            while(cptPriorityQueue.size() > 1) {
                cpt tableOne = cptPriorityQueue.poll();
                cpt tableTwo = cptPriorityQueue.poll();
                cpt joined = joinCpts(tableOne, tableTwo);
                cptPriorityQueue.add(joined);
            }
            cpt eliminatedTable = eliminateVariable(Objects.requireNonNull(cptPriorityQueue.poll()), hiddenVariable[0]);
            myCpts.add(eliminatedTable);
        }

        PriorityQueue<cpt> cptPriorityQueue = new PriorityQueue<>(myCpts);
        while(cptPriorityQueue.size() > 1) {
            cpt tableOne = cptPriorityQueue.poll();
            cpt tableTwo = cptPriorityQueue.poll();
            cpt joined = joinCpts(tableOne, tableTwo);
            cptPriorityQueue.add(joined);
        }
        cpt answer = cptPriorityQueue.poll();
        BigDecimal normalized = normalize(Objects.requireNonNull(answer), myQuery.get(0)[1]);
        ans = normalized.toString() + "," + addOperations + "," + multiplyOperations;
        return ans;
    }


    private static ArrayList<String> findAncestors(ArrayList<String[]> myQuery, ArrayList<node> nodes) {
        ArrayList<String> ancestors = new ArrayList<>();
        for (String[] variableInQuery : myQuery) {
            node tmp = graphCreator.getNode(variableInQuery[0].charAt(0), nodes);
            Queue<node> nodesQueue = new LinkedList<>();
            nodesQueue.add(tmp);
            while (!nodesQueue.isEmpty()){
                node poll = nodesQueue.poll();
                if(!ancestors.contains(Character.toString(poll.getVar())))
                    ancestors.add(Character.toString(poll.getVar()));
                for (node parent : poll.getParents()){
                    if(!ancestors.contains(Character.toString(parent.getVar())))
                        nodesQueue.add(parent);
                }
            }
        }
        return ancestors;
    }


    private static String findInCpt(ArrayList<String[]> myQuery, ArrayList<cpt> myCpts) {
        String ans = "";
        for (cpt table : myCpts) {
            int index = table.getCptTable().get(0).size();
            boolean isInCpt = true;
            if(!table.getCptTable().get(0).get(index-2).equals(myQuery.get(0)[0]))
                isInCpt = false;
            for (int i = 1; i < myQuery.size(); i++) {
                if (!table.getCptTable().get(0).contains(myQuery.get(i)[0])) {
                    isInCpt = false;
                    break;
                }
            }

            if (isInCpt){
                int rowIndex;
                for (int i = 1; i < table.getCptTable().size(); i++) {
                    boolean isInRow = true;
                    for (String[] variable : myQuery) {
                        int variableIndex = table.getCptTable().get(0).indexOf(variable[0]);
                        if (!table.getCptTable().get(i).get(variableIndex).equals(variable[1]))
                            isInRow = false;
                    }
                    if(isInRow) {
                        rowIndex = i;
                        BigDecimal probability = new BigDecimal(table.getCptTable().get(rowIndex).get(index-1)).setScale(5, RoundingMode.HALF_EVEN);
                        ans = probability.toString()+",0,0";
                    }
                }
            }
        }

        return ans;
    }


    private static void minimizeCpts(ArrayList<String[]> myQuery, ArrayList<cpt> myCpts) {
        if(myQuery.size() > 1) {
            for (int j = 1; j < myQuery.size(); j++) {
                for (int k = 0; k < myCpts.size(); k++) {
                    ArrayList<ArrayList<String>> currentCpt = myCpts.get(k).getCptTable();
                    ArrayList<String> header = currentCpt.get(0);
                    int index = -1;
                    for (int i = 0; i < header.size(); i++) {
                        if (header.get(i).equals(myQuery.get(j)[0]))
                            index = i;
                    }
                    if(index != -1) {
                        for (int i = 1; i < currentCpt.size(); i++) {
                            ArrayList<String> rowInCpt = currentCpt.get(i);
                            if (!rowInCpt.get(index).equals(myQuery.get(j)[1])) {
                                currentCpt.remove(i);
                                i--;
                            }
                        }

                        for (ArrayList<String> rowInCpt : currentCpt) {
                            rowInCpt.remove(index);
                        }

                        if (currentCpt.get(0).size() == 1) {
                            myCpts.remove(k);
                            k--;
                        }
                    }
                }
            }
        }
    }


    private static cpt joinCpts(cpt tableOne, cpt tableTwo) {
        cpt jointCpt = new cpt();
        ArrayList<String> commonVariables = new ArrayList<>();
        ArrayList<String> differentVariables = new ArrayList<>();
        for (String variable : tableOne.getCptTable().get(0)) {
            if(tableTwo.getCptTable().get(0).contains(variable) && !variable.contains("P"))
                commonVariables.add(variable);
            else if (!variable.contains("P")) {
                differentVariables.add(variable);
            }
        }

        for (String variable : tableTwo.getCptTable().get(0)){
            if (!commonVariables.contains(variable) && !variable.contains("P"))
                differentVariables.add(variable);
        }

        ArrayList<String> header = new ArrayList<>();
        header.addAll(commonVariables);
        header.addAll(differentVariables);
        header.add("P()");
        jointCpt.getCptTable().add(header);

        //Start from row 1 in Table one
        for (int i = 1; i < tableOne.getCptTable().size(); i++) {
            ArrayList<String[]> commonValuesList = new ArrayList<>();
            String tableOneValue = tableOne.getCptTable().get(i).get(tableOne.getCptTable().get(i).size()-1);
            BigDecimal firstValue = new BigDecimal(tableOneValue);
            for (String commonVal : commonVariables) {
                String[] commonValue = new String[2];
                commonValue[0] = commonVal;
                int index = tableOne.getCptTable().get(0).indexOf(commonVal);
                commonValue[1] = tableOne.getCptTable().get(i).get(index);
                commonValuesList.add(commonValue);
            }

            //Start from row 1 in Table two and look for the wanted row
            int rowIndexTableTwo = -1;
            boolean flag = true;
            for (int j = 1; j < tableTwo.getCptTable().size(); j++) {
                ArrayList<String[]> allValuesList = new ArrayList<>(commonValuesList);
                for (String[] commonVal : commonValuesList) {
                    int index = tableTwo.getCptTable().get(0).indexOf(commonVal[0]);
                    if (!tableTwo.getCptTable().get(j).get(index).equals(commonVal[1]))
                        flag = false;
                    else {
                        flag = true;
                        rowIndexTableTwo = j;
                    }
                }
                if(flag) {
                    int rowSize = tableTwo.getCptTable().get(rowIndexTableTwo).size() - 1;
                    String secondTableValue = tableTwo.getCptTable().get(rowIndexTableTwo).get(rowSize);
                    BigDecimal secondValue = new BigDecimal(secondTableValue);
                    //add all different values from the 2 tables to the list
                    for (String diffVariable : differentVariables) {
                        if (tableOne.getCptTable().get(0).contains(diffVariable)) {
                            int index = tableOne.getCptTable().get(0).indexOf(diffVariable);
                            String[] diffVal = new String[2];
                            diffVal[0] = diffVariable;
                            diffVal[1] = tableOne.getCptTable().get(i).get(index);
                            allValuesList.add(diffVal);
                        } else if (tableTwo.getCptTable().get(0).contains(diffVariable)) {
                            int index = tableTwo.getCptTable().get(0).indexOf(diffVariable);
                            String[] diffVal = new String[2];
                            diffVal[0] = diffVariable;
                            diffVal[1] = tableTwo.getCptTable().get(rowIndexTableTwo).get(index);
                            allValuesList.add(diffVal);
                        }
                    }

                    //add new row in the new cpt Table
                    ArrayList<String> newCptRow = new ArrayList<>();
                    for (String[] value : allValuesList) {
                        int index = header.indexOf(value[0]);
                        newCptRow.add(index, value[1]);
                    }
                    BigDecimal newValue = firstValue.multiply(secondValue);
                    multiplyOperations++;
                    String newValueString = newValue.toString();
                    newCptRow.add(newValueString);
                    jointCpt.getCptTable().add(newCptRow);
                }
            }

        }
        return jointCpt;
    }


    private static cpt eliminateVariable(cpt table, String hiddenVariable) {
        cpt eliminatedTable = new cpt();
        ArrayList<String[]> variablesValues = new ArrayList<>();
        for (String variable : table.getCptTable().get(0)){
            if(!variable.equals(hiddenVariable) && !variable.contains("P")) {
                String[] variableValue = new String[2];
                variableValue[0] = variable;
                variablesValues.add(variableValue);
            }
        }

        ArrayList<String> header = new ArrayList<>();
        for (String[] variable : variablesValues){
            header.add(variable[0]);
        }
        header.add("P()");
        eliminatedTable.getCptTable().add(header);

        for (int i = 1; i < table.getCptTable().size() - 1; i++){
            int size = table.getCptTable().get(i).size();
            BigDecimal value = new BigDecimal(table.getCptTable().get(i).get(size-1));
            for (String[] variablesValue : variablesValues) {
                int variableIndex = table.getCptTable().get(0).indexOf(variablesValue[0]);
                variablesValue[1] = table.getCptTable().get(i).get(variableIndex);
            }
            for (int j = i + 1; j < table.getCptTable().size(); j++) {
                boolean flag = true;
                for (String[] variableValue: variablesValues) {
                    int variableIndex = table.getCptTable().get(0).indexOf(variableValue[0]);
                    if(!table.getCptTable().get(j).get(variableIndex).equals(variableValue[1])){
                        flag = false;
                    }
                }
                if (flag) {
                    BigDecimal valueToAdd = new BigDecimal(table.getCptTable().get(j).get(size-1));
                    value = value.add(valueToAdd);
                    addOperations++;
                    table.getCptTable().remove(j);
                    j--;
                }
            }
            //add new row in the new cpt Table
            ArrayList<String> newCptRow = new ArrayList<>();
            for (String[] variableValue : variablesValues) {
                int index = header.indexOf(variableValue[0]);
                newCptRow.add(index, variableValue[1]);
            }
            String newValueString = value.toString();
            newCptRow.add(newValueString);
            eliminatedTable.getCptTable().add(newCptRow);
        }
        return eliminatedTable;
    }




    private static ArrayList<node> nodesInQuery(String query, ArrayList<node> nodes) {
        ArrayList<node> nodesInQuery = new ArrayList<>();

        String source = query.substring(0, 1);
        String target = query.substring(2, 3);
        String evidence = query.substring(query.indexOf('|'));

        node sourceNode = graphCreator.getNode(source.charAt(0), nodes);
        node targetNode = graphCreator.getNode(target.charAt(0), nodes);
        nodesInQuery.add(sourceNode);
        nodesInQuery.add(targetNode);

        if(evidence.length() > 1) {
            evidence = evidence.substring(1);
            if(evidence.contains(",")) {
                String[] evidenceNodes = evidence.split(",");
                for (String tmpEvidence : evidenceNodes) {
                    node evidenceNode = graphCreator.getNode(tmpEvidence.charAt(0), nodes);
                    nodesInQuery.add(evidenceNode);
                }
            }
            node evidenceNode = graphCreator.getNode(evidence.charAt(0), nodes);
            nodesInQuery.add(evidenceNode);
        }
        return nodesInQuery;
    }


    private static ArrayList<String[]> nodesInVariableElmination(String query, ArrayList<node> nodes) {
        String myQuery = query;
        ArrayList<String[]> nodesInVE = new ArrayList<>();

        myQuery = myQuery.replace("P(", "");
        String variable = myQuery.substring(0, myQuery.indexOf('|'));
        String evidence = myQuery.substring(myQuery.indexOf('|') + 1, myQuery.indexOf(')'));
        String hiddenVariables = myQuery.substring(myQuery.indexOf(')') + 2);

        String[] varValue = variable.split("=");
        nodesInVE.add(varValue);

        String[] tmpEvidence = evidence.split(",");
        for (String evidenceVar : tmpEvidence){
            String[] evidenceValue = evidenceVar.split("=");
            nodesInVE.add(evidenceValue);
        }
        if(!hiddenVariables.isEmpty()) {
            for (String hiddenVar : hiddenVariables.split("-")) {
                String[] hiddenVarValue = new String[1];
                hiddenVarValue[0] = hiddenVar;
                nodesInVE.add(hiddenVarValue);
            }
        }
        return nodesInVE;
    }


    private static BigDecimal normalize(cpt answer, String value){
        BigDecimal numerator = new BigDecimal("0.0");
        BigDecimal denominator = new BigDecimal("0.0");
        for (int i = 1; i < answer.getCptTable().size(); i++) {
            int size = answer.getCptTable().get(i).size();
            if(answer.getCptTable().get(i).get(0).equals(value))
                numerator = numerator.add(new BigDecimal(answer.getCptTable().get(i).get(size-1)));
            denominator = denominator.add(new BigDecimal(answer.getCptTable().get(i).get(size-1)));
            addOperations++;
        }
        BigDecimal normalized = numerator.divide(denominator, 5, RoundingMode.HALF_EVEN);
        addOperations-=1;
        return normalized;
    }


    private static void cleanGraph(ArrayList<node> graph) {
        for (node graphNode : graph) {
            graphNode.setVisited(false);
            graphNode.setEvidence(false);
            graphNode.setParent(false);
        }
    }


}
