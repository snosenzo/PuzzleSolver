import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        String filename = args[0];
        String searchType = args[1];
        SearchAgent a = new SearchAgent();
        a.setProblem(filename);
        a.setSearch(searchType);

    }
}
