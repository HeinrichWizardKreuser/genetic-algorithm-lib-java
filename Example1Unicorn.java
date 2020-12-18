/**
 * Here we showcase the use of GeneticAlgorithmLib by creating a population to
 * guess any word of choice. To guess your word, simply run:
 * $ java Example1Unicorn <your word>
 * for example:
 * $ java Example1Unicorn Unicorn
 * and see what happens!
 */
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.Scanner;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Example1Unicorn {

  public static String word;
  private static final double MUTATION_RATE = 0.8;
  private static final int POP_SIZE = 100;
  public static void main(String[] args) {
    // get word from user
    word = null;
    if (args.length > 0) {
      word = args[0];
      if (!validWord(word)) {
        complain(word);
        return;
      }
    } else {
      Scanner sc = new Scanner(System.in);
      System.out.println(REQUEST_WORD);
      word = sc.nextLine();
      while (!validWord(word)) {
        complain(word);
        System.out.println(TRY_AGAIN);
        word = sc.nextLine();
      }
      sc.close();
    }
    int ln = word.length();
    int POP_SIZE = 100;
    String[] population = new String[POP_SIZE];
    for (int i = 0; i < POP_SIZE; i++) {
      population[i] = randomWord(ln);
    }
    
    /** cost function is how many letters are wrong */
    Function<String[], double[]> costFunction = (pop) -> {
      double[] costArr = new double[pop.length];
      for (int i = 0; i < pop.length; i++) {
        String s = pop[i];
        double cost = 0;
        for (int j = 0; j < ln; j++) {
          if (s.charAt(j) != Example1Unicorn.word.charAt(j)) {
            cost += 1;
          }
        }
        costArr[i] = cost;
      }
      return costArr;
    };

    /** changes a random index to a random letter */
    Function<String, String> mutateFunction = (s) -> {
      char[] arr = s.toCharArray();
      arr[r.nextInt(ln)] = randomLetter();
      return new String(arr);
    };

    /** combines two words to make one */
    BiFunction<String, String, String> reproductionFunction = 
      (mother, father) -> {
      char[] child = mother.toCharArray();
      char[] fatherArr = father.toCharArray();
      for (int i = 0; i < ln; i++) {
        if (r.nextBoolean()) {
          child[i] = fatherArr[i];
        }
      }
      return new String(child);
    };

    int generation = 0;
    while (true) {
      // evolve
      LinkedHashMap<String, Double> word2cost = GeneticAlgorithmLib.evolve(
        population,
        costFunction,
        mutateFunction,
        reproductionFunction,
        MUTATION_RATE,
        false, // reverse order
        POP_SIZE,
        4, // topK
        1 // generations
      );
      // now report on findings
      generation += 1;
      System.out.printf("---- GENERATION %d ----\n", generation);
      int iteration = 0;
      int topPerformers = 5;
      System.out.printf("top %d performers:\n", topPerformers);
      // display the outcome of this population
      double lowestCost = ln;
      String bestWord = null;
      for (Entry<String, Double> entry : word2cost.entrySet()) {
        String s = entry.getKey();
        double cost = entry.getValue();
        if (iteration == 0) {
          lowestCost = cost;
          bestWord = s;
        }
        iteration += 1;
        if (iteration > topPerformers) {
          break;
        }
        System.out.printf("%s: %f\n", s, cost);
      }
      if (lowestCost <= 0) {
        System.out.println("---- SUCCESS ----");
        System.out.println("> " + bestWord);
        break;
      }
    }
  }

  private static Random r = new Random();

  private static String randomWord(int len) {
    String word = "";
    for (int i = 0; i < len; i++) {
      word += randomLetter();
    }
    return word;
  }
  /** Generates a random word */
  private static char randomLetter() {
    char c = (char)('a' + r.nextInt(26));
    if (r.nextBoolean()) {
      return Character.toUpperCase(c);
    }
    return c;
  }

  private static final String TRY_AGAIN = 
    "Invalid word, please try again";
  private static final String REQUEST_WORD = 
    "Please enter a word (only lower and uppercase letters are allowed";
  private static void complain(String word) {
    System.out.printf(
      "Invalid word '%s', must all be lower or uppercase letters!\n", word);
  }

  /**
   * @param s is a given word that need be checked whether it is valid
   * @return true if the s only contains letters
   */
  private static boolean validWord(String s) {
    if (s.length() == 0) {
      return false;
    }
    for (char c : s.toCharArray()) {
      if (!Character.isLetter(c)) {
        return false;
      }
    }
    return true;
  }
}
