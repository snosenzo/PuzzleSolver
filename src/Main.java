import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println(args.length);
        String filename = args[0];
        String searchType = args[1];

        SearchAgent a = new SearchAgent();
        a.setProblem(filename);
        a.setSearch(searchType);
        int[] a1 = {3, 4, 6, 7};
        int[] a2 = new int[4];
        for( int i = 0; i < a1.length; i++) {
            a2[i] = a1[i];
        }
        System.out.println(a1);
        System.out.println(Arrays.hashCode(a1));

        System.out.println(a2);
        System.out.println(Arrays.hashCode(a2));

    }
}
