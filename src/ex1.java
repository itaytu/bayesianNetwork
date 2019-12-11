import java.io.IOException;
import java.util.ArrayList;

public class ex1 {


    public static void main(String[] args) throws IOException {
        String path = "input2.txt";
      //  long startTime = System.nanoTime();

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

       // long endTime   = System.nanoTime();
       // System.out.println(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

    }
}
