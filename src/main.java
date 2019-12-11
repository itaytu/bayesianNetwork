import java.io.IOException;
import java.util.ArrayList;

public class main {


    public static void main(String[] args) throws IOException {
        String path = "input.txt";
        ArrayList<ArrayList<String>> fileArray = textReader.fileAsArray(path);
        ArrayList<node> graph = graphCreator.createGraph(fileArray);
        String[] queries = textReader.queriesAsStringArray(fileArray.get(fileArray.size()-1));
        for (String query : queries) {
            if (!query.contains("P")) {
                String ans = algorithm.isIndependent(query, graph);
                System.out.println(ans);
            }
            else {
                String ans = algorithm.variableElimination(query, graph);
                System.out.println(ans);
            }
        }

    }
}
