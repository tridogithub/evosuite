package org.evosuite.ga;

import org.apache.commons.lang3.SerializationUtils;
import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TLBOAlgorithmV2<T extends Chromosome<T>> implements SearchAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);

    /**
     * Fitness function to rank individuals
     */
    protected List<FitnessFunction<T>> fitnessFunctions = new ArrayList<>();

    /**
     * Current population
     */
    protected List<T> population = new ArrayList<>();

    /**
     * Generator for initial population
     */
    protected ChromosomeFactory<T> chromosomeFactory;

    /**
     * Age of the population
     */
    protected int currentIteration = 0;
    protected T currentTeacher;
    protected List<T> currentStudents = new ArrayList<>();
    /**
     * Listeners
     */
    protected transient Set<SearchListener<T>> listeners = new HashSet<>();

    public TLBOAlgorithmV2(ChromosomeFactory<T> chromosomeFactory) {
        this.chromosomeFactory = chromosomeFactory;
    }

    @Override
    public void generateSolution() {
        if (population.isEmpty()) {
            initializePopulation();
            assert !population.isEmpty() : "Could not create any test";
        }
        TestSuiteChromosome test = (TestSuiteChromosome) population.get(0);
        TestChromosome testChromosome = test.getTestChromosomes().get(0);
        testChromosome.getTestCase().getCoveredGoals().size();
//        System.out.println(testChromosome.getFitness());
//        System.out.println(testChromosome);
        while (currentIteration <= Properties.ITERATION) {
            System.out.println("Iteration: " + currentIteration);
            //TODO add mutation for each phase
            //teaching phase
            teachingPhase();
            //learning phase
            learningPhase();
            //checking stoppedCondition in each iteration

            //calculate fitness and sort population
            sortPopulation();
            currentIteration++;
        }
    }

    private void teachingPhase() {
        currentTeacher = population.get(0);
        currentStudents = population.subList(1, population.size());
        for (int i = 1; i < currentStudents.size(); i++) {
            T student = currentStudents.get(i);
            T tmpStudent = SerializationUtils.clone(student);

            learnFromBetterTestSuite(currentTeacher, tmpStudent);
            //remove old fitness value
            tmpStudent.getFitnessValues().clear();
            double newFitness = tmpStudent.getFitness(fitnessFunctions.get(0));

            if (newFitness < student.getFitness()) {
                tmpStudent.mutate();
                tmpStudent.getFitness(fitnessFunctions.get(0));
                currentStudents.set(i, tmpStudent);
            }
        }
    }

    private void learningPhase() {
        for (int i = 0; i < population.size(); i++) {
            T student = population.get(i);
            int randomIndex = new Random().nextInt(50);
            T randomStudent = population.get(randomIndex);
            if (student.getFitness() > randomStudent.getFitness()) {
                T tmpStudent = SerializationUtils.clone(student);

                learnFromBetterTestSuite(randomStudent, tmpStudent);
                //remove old fitness value
                tmpStudent.getFitnessValues().clear();
                double newFitness = tmpStudent.getFitness(fitnessFunctions.get(0));

                if (newFitness < student.getFitness()) {
                    tmpStudent.mutate();
                    tmpStudent.getFitness(fitnessFunctions.get(0));
                    population.set(i, tmpStudent);
                }
            }
        }
    }

    private void learnFromBetterTestSuite(T betterChromosome, T currentChromosome) {
        //Assume that all works is based on TestSuiteChromosome
        TestSuiteChromosome betterTestSuiteChromosome = (TestSuiteChromosome) betterChromosome;
        TestSuiteChromosome currentTestSuiteChromosome = (TestSuiteChromosome) currentChromosome;

        //move forward the better chromosome Xnew = Xold + r(Xbetter - X)
        List<TestChromosome> betterTestChromosomes = betterTestSuiteChromosome.getTestChromosomes();
        List<TestChromosome> currentTestChromosomes = currentTestSuiteChromosome.getTestChromosomes();


        if (betterTestChromosomes.size() > currentTestChromosomes.size()) {
            int different = betterTestChromosomes.size() - currentTestChromosomes.size();
            List<TestChromosome> bestTestChromosomes = getBestTestChromosomeFromTestSuite(betterTestSuiteChromosome, different);
            // add these best test chromosomes of teacher to current test suite of student
            currentTestSuiteChromosome.addTests(bestTestChromosomes);
        } else {
            int different = currentTestChromosomes.size() - betterTestChromosomes.size();
            // replace a portion of student by a number of best chromosomes of teacher
            if (different != 0) {
                int numberOfBestTestCase = Math.min(different, betterChromosome.size());
                List<TestChromosome> bestTestChromosomes = getBestTestChromosomeFromTestSuite(betterTestSuiteChromosome, numberOfBestTestCase);
                currentTestChromosomes.sort((o1, o2) -> o2.getTestCase().getCoveredGoals().size() - o1.getTestCase().getCoveredGoals().size());
                for (int i = currentTestChromosomes.size() - numberOfBestTestCase - 1; i < currentTestChromosomes.size(); i++) {
                    currentTestChromosomes.remove(i);
                }
//                System.out.println("Size after remove: " + currentTestChromosomes.size());
                currentTestChromosomes.addAll(bestTestChromosomes);
//                System.out.println("Size after add best chromosomes: " + currentTestChromosomes.size());
            }
        }
//        System.out.println(betterTestSuiteChromosome.getTestChromosomes().size() + "_" + currentTestSuiteChromosome.getTestChromosomes().size());
    }

    private List<TestChromosome> getBestTestChromosomeFromTestSuite(TestSuiteChromosome testSuiteChromosome, int numberOfTestCases) {
        testSuiteChromosome.getTestChromosomes().sort((o1, o2) -> o2.getTestCase().getCoveredGoals().size() - o1.getTestCase().getCoveredGoals().size());
        return testSuiteChromosome.getTestChromosomes().subList(0, numberOfTestCases);
    }

    public void initializePopulation() {
//        notifySearchStarted();
        currentIteration = 0;

        // Set up initial population
        population.addAll(getRandomPopulation(Properties.POPULATION));
        logger.debug("Calculating fitness of initial population");
        calculateFitnessAndSortPopulation();

//        this.notifyIteration();
    }

    /**
     * Calculate fitness for all individuals and sort them
     */
    protected void calculateFitnessAndSortPopulation() {
        this.calculateFitness();
        // Sort population
        this.sortPopulation();
    }

    protected void calculateFitness() {
        logger.debug("Calculating fitness for " + population.size() + " individuals");

        for (T c : this.population) {
            fitnessFunctions.forEach(ff -> {
                ff.getFitness(c);
            });
        }
    }

    protected boolean isMaximizationFunction() {
        return fitnessFunctions.get(0).isMaximizationFunction();
    }

    protected void sortPopulation() {
        if (Properties.SHUFFLE_GOALS)
            Randomness.shuffle(population);

        if (isMaximizationFunction()) {
            population.sort(Collections.reverseOrder());
        } else {
            Collections.sort(population);
        }
    }

    //Get random population
    protected List<T> getRandomPopulation(int population_size) {
        logger.debug("Creating random population");

        List<T> newPopulation = new ArrayList<>(population_size);

        for (int i = 0; i < population_size; i++) {
            T individual = chromosomeFactory.getChromosome();
            fitnessFunctions.forEach(individual::addFitness);
            newPopulation.add(individual);
            //logger.error("Created a new individual");
//            if (isFinished())
//                break;
        }
        logger.debug("Created " + newPopulation.size() + " individuals");

        return newPopulation;
    }

    /**
     * Return the individual with the highest fitChromosomeess
     *
     * @return a {@link Chromosome} object.
     */
    public T getBestIndividual() {

        if (population.isEmpty()) {
            return this.chromosomeFactory.getChromosome();
        }

        // Assume population is sorted
        return population.get(0);
    }

    public List<FitnessFunction<T>> getFitnessFunctions() {
        return fitnessFunctions;
    }

    public void setFitnessFunctions(List<FitnessFunction<T>> fitnessFunctions) {
        this.fitnessFunctions = fitnessFunctions;
    }

    public int getAge() {
        return currentIteration;
    }

//    /**
//     * Add a new search listener
//     *
//     * @param listener a {@link org.evosuite.ga.metaheuristics.SearchListener}
//     *                 object.
//     */
//    public void addListener(SearchListener<T> listener) {
//        listeners.add(listener);
//    }
//
//    /**
//     * Remove a search listener
//     *
//     * @param listener a {@link org.evosuite.ga.metaheuristics.SearchListener}
//     *                 object.
//     */
//    public void removeListener(SearchListener<T> listener) {
//        listeners.remove(listener);
//    }
//
//    /**
//     * Notify all search listeners of search start
//     */
//    protected void notifySearchStarted() {
//        listeners.forEach(l -> l.searchStarted(this));
//    }
//
//    /**
//     * Notify all search listeners of search end
//     */
//    protected void notifySearchFinished() {
//        listeners.forEach(l -> l.searchFinished(this));
//    }
//
//    /**
//     * Notify all search listeners of iteration
//     */
//    protected void notifyIteration() {
//        listeners.forEach(l -> l.iteration(this));
//    }
//
//    /**
//     * Notify all search listeners of fitness evaluation
//     *
//     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
//     */
//    protected void notifyEvaluation(T chromosome) {
//        listeners.forEach(l -> l.fitnessEvaluation(chromosome));
//    }
//
//    /**
//     * Notify all search listeners of a mutation
//     *
//     * @param chromosome a {@link org.evosuite.ga.Chromosome} object.
//     */
//    protected void notifyMutation(T chromosome) {
//        listeners.forEach(l -> l.modification(chromosome));
//    }

}
