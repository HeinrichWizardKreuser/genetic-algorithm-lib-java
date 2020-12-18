import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Random;
import java.util.LinkedHashMap;
import static java.util.Arrays.copyOf;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;


public class GeneticAlgorithmLib {

  private static Random r;

  /**
   * Evolves the given population using a general Genetic Algorithm
   * 
   * @param population the array of agents that must be evolved.
   * @param cost a Function that takes in the population and returns an array
   *        representing the cost of each agent. The order is the same as the
   *        order of the given population
   * @param mutate a Function that takes in an agent and returns a new, mutated
   *        agent.
   * @param mutationRate the probability at which each new child will be mutated
   * @param reverse if set to true, then the cost function is interpreted as a
   *        fitness function, meaning that the algorithm will attempt to 
   *        maximize the cost of the population instead of minimizing it
   * @param popSize the desired size of the population.
   *        If this is greater than the size of the given population, then the
   *        population will be filled up with mutants of random agents from the
   *        original population.
   *        If this is lower than the size of the given population, then the 
   *        population will be culled to be size popSize.
   *        If this is the same as the size of the given population, nothing 
   *        occurs.
   * @param topK the amount of the top performing agents to keep while the rest
   *        of the population is culled.
   * @param generations the number of generations to evolve the given population
   *        before the population is returned to the caller.
   * 
   * @return LinkedHashMap of the latest generation's agents mapped to their
   *         performance (gotten from the cost function)
   */
  @SuppressWarnings("unchecked")
  public static <T> LinkedHashMap<T, Double> evolve(T[] population,
                                                    Function<T[], double[]> cost, 
                                                    Function<T, T> mutate, 
                                                    BiFunction<T, T, T> reproduce,
                                                    double mutationRate,
                                                    boolean reverse,
                                                    int popSize,
                                                    int topK,
                                                    int generations) {
    r = new Random();
    int ln = population.length;
    if (popSize == -1) {
      popSize = ln;
    } else {
      if (popSize > ln) {
        T[] newPop = (T[])(new Object[popSize]);
        for (int i = 0; i < ln; i++) {
          newPop[i] = population[i];
        }
        for (int i = ln; i < popSize; i++) {
          newPop[i] = mutate.apply(population[r.nextInt(ln)]);
        }
        population = newPop;
      } else if (popSize < ln) {
        population = copyOf(population, popSize);
      }
      popSize = ln;
    }
    if (topK < 2) {
      topK = 2;
    }
    int generation = 0;
    // run simulation
    while (true) {
      // sort population by cost
      double[] costArr = cost.apply(population);
      LinkedHashMap<T, Double> agent2cost = new LinkedHashMap<T, Double>();
      for (int i = 0; i < popSize; i++) {
        agent2cost.put(population[i], costArr[i]);
      }
      agent2cost = sort(agent2cost, reverse);
        // return if generation exceeds
      generation += 1;
      if (generation > generations) {
        return agent2cost;
      }
      int k = 0;
      // cull population
      for (T agent : agent2cost.keySet()) {
        if (k < topK) {
          population[k++] = agent;
        } else {
          break;
        }
      }
      // create the rest of the population from the top_k
      for (; k < popSize; k++) {
        // select two parents
        T[] parents = chooseParents(population, topK);
        // reproduce to create child
        T child = reproduce.apply(parents[0], parents[1]);
        // mutate with probability
        if (r.nextFloat() < mutationRate) {
          child = mutate.apply(child);
        }
        population[k] = child;
      }
    }
  }

  /**
   * Method used to sort the given LinkedHashMap, representing the cost of each
   * agent in the population
   *
   * @param toSort the original LinkedHashMap to sort
   * @param reverse whether or not it must be sorted in reverse order.
   */
  private static <T> LinkedHashMap<T, Double> sort(
    LinkedHashMap<T, Double> toSort, boolean reverse) {
    if (reverse) {
      return toSort
        .entrySet()
        .stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(toMap(
          Map.Entry::getKey, 
          Map.Entry::getValue, 
          (e1, e2) -> e2, 
          LinkedHashMap::new)
        );
    }
    return toSort
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByValue())
      .collect(toMap(
        Map.Entry::getKey, 
        Map.Entry::getValue, 
        (e1, e2) -> e2, 
        LinkedHashMap::new)
      );
  }

  /**
   * Standard procedure to choose 2 parents from the given population, amongst
   * the given topK.
   * 
   * @param population the population to get a sample 2 parents from
   * @param topK the bounds under which the parents must be gotten. 
   *        Example: if the popSize is 10 and topK is 3, then only 2 of the top
   *        3 agents in the given population can be chosen.
   * @return array containing the mother and the father.
   */
  @SuppressWarnings("unchecked")
  private static <T> T[] chooseParents(T[] population, int topK) {
    int p1 = r.nextInt(topK);
    int p2 = r.nextInt(topK-1);
    if (p2 == p1) {
      p2 += topK-1;
    }
    return (T[])(new Object[]{population[p1], population[p2]});
  }


}
