import java.util.*;
import java.util.stream.Stream;

public class test {
    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {

            Map<String, Integer> temp = new Hashtable<String, Integer>();
            List<String> names = Arrays.asList("kim", "lee", "park", "choi");

            ArrayList<Integer> numbers = new ArrayList<Integer>();
            while (numbers.size() < 4) {
                int rando = (int) (Math.random() * 4) + 1;
                if (!numbers.contains(rando)) {
                    numbers.add(rando);
                }
            }


            names.stream().forEach(e -> temp.put(e, numbers.get(names.indexOf(e))));

            temp.forEach((u, v) -> System.out.print(u + " " + v + "   "));
            System.out.println();
        }
    }

}
