import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class algorithm {


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
        String ans = "";
        cleanGraph(nodes);
        ArrayList<String[]> myQuery = nodesInVariableElmination(query, nodes);
        ArrayList<String[]> hiddenVariables = new ArrayList<>();
        for(int i = 0; i < myQuery.size(); i++) {
            if (myQuery.get(i).length == 1){
                hiddenVariables.add(myQuery.get(i));
                myQuery.remove(i);
                i--;
            }
        }

        ArrayList<cpt> myCpts = new ArrayList<>();
        for (node myNode : nodes) {
            cpt myCpt = new cpt(myNode.getMyCpt());
            myCpts.add(myCpt);
        }

        ans = findInCpt(myQuery, myCpts);
        if(!ans.isEmpty())
            return ans;

        minimizeCpts(myQuery, myCpts);

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
                   cpt joined = joinCpts(tableOne, tableTwo, hiddenVariable[0]);
                   cptPriorityQueue.add(joined);
            }

            myCpts.add(cptPriorityQueue.poll());


        }




        return ans;
    }


    private static String findInCpt(ArrayList<String[]> myQuery, ArrayList<cpt> myCpts) {




        return "";
    }


    private static void minimizeCpts(ArrayList<String[]> myQuery, ArrayList<cpt> myCpts) {
        if(myQuery.size() > 1) {
           // myQuery.remove(0); //TODO: check if it makes problems
            for (int j = 1; j < myQuery.size(); j++) {
                for (cpt cptTable : myCpts) {
                    ArrayList<ArrayList<String>> currentCpt = cptTable.getCptTable();
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
                    }
                }
            }
        }
    }


    private static cpt joinCpts(cpt tableOne, cpt tableTwo, String hiddenVariable) {
        cpt jointCpt = new cpt();
        ArrayList<String> commonVariables = new ArrayList<>();
        ArrayList<String> differentVariables = new ArrayList<>();
        for (String variable : tableOne.getCptTable().get(0)) {
            if(tableTwo.getCptTable().get(0).contains(variable))
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

        ArrayList<String[]> commonValues = new ArrayList<>();
        for (int i = 1; i < tableOne.getCptTable().size(); i++) {
            
        }



        return jointCpt;
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
        for (String hiddenVar : hiddenVariables.split("-")){
            String[] hiddenVarValue = new String[1];
            hiddenVarValue[0] = hiddenVar;
            nodesInVE.add(hiddenVarValue);
        }
        return nodesInVE;
    }


    private static void cleanGraph(ArrayList<node> graph) {
        for (node graphNode : graph) {
            graphNode.setVisited(false);
            graphNode.setEvidence(false);
            graphNode.setParent(false);
        }
    }


}
