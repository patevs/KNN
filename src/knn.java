import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class knn {
	
	static final int numAttr = 4;
	
	/** Checks the command arguments to ensure they are valid
	 * 
	 * @param args
	 */
	private static void checkArgs(String[] args){
		//checking two files names entered as arguments
		if (args.length != 3) {
			System.out.println("please enter two files and K value to run program");
			System.exit(1);
		}
		
		File file = new File("./" + args[0]);
		File file2 = new File("./" + args[1]);

		// check the files exists
		if (!file.exists()||!file2.exists()) {
			System.out.println("Files must exist in current directory.");
			System.exit(2);
		}
		// check files type
		if (!args[0].toLowerCase().endsWith(".txt")||!args[1].toLowerCase().endsWith(".txt")) {
			System.out.println("Only .txt files is accepted.");
			System.exit(3);
		}
	}
	
	public static void main(String[] args){
		checkArgs(args);
		new knn(args[0],args[1],Integer.parseInt(args[2]));
	}

	/** Runs the K Nearest Neighbor algorithm
	 * 
	 */
	private knn(String trainingFile, String testFile, int K) {
		System.out.println("Starting knn");
		
		final long startTime = System.currentTimeMillis();
		
		// make sure the K value is legal
		if(K <= 0){
			System.out.println("K should be greater than 0!");
			return;
		}
		
		// get the testing and training files
		File train = new File("./" + trainingFile);
		File test = new File("./" + testFile);
		
		// Store each test and training instance from the files
		List<TrainInstance> trainList = readTrainFile(train);
		Set<TestInstance> testSet = readTestFile(test);
		
		// Find the K nearest neighbours of each test instance and classify
		for(TestInstance testInst: testSet){
			TrainInstance[] neighbours = findKNearestNeighbours(trainList, testInst, K);
			// Get prediction from neighbours
			String predictedLabel = classify(neighbours);
			testInst.setPredictedLabel(predictedLabel);
		}
	
		// Calculate accuracy
		int numCorrect = 0;
		for(TestInstance testInst: testSet){
			if(testInst.getPredictedLabel().equals(testInst.label)){
				numCorrect++;
				// Print prediction
				System.out.println("Prediction: " + testInst.getPredictedLabel() + " Actual: " + testInst.label);
			}else{
				// Print prediction
				System.err.println("Prediction: " + testInst.getPredictedLabel() + " Actual: " + testInst.label);
			}
		}
		// Print the accuracy
		System.out.println("Accuracy: " + ((float)numCorrect/testSet.size())*100 + "%");
		//print the total execution time
		final long endTime = System.currentTimeMillis();
		System.out.println("Total excution time: " + (endTime - startTime) / (double)1000 + " seconds.");
	}
	
	/** Find the K nearest neighbours of a test instance from within a given list of training instances
	 * 
	 * @param trainList
	 * @param testInst
	 * @param K
	 * @return
	 */
	private TrainInstance[] findKNearestNeighbours(List<TrainInstance> trainList, TestInstance testInst, int K) {
		// creating neighbours array and distance measure 
		TrainInstance[] neighbours = new TrainInstance[K];
		EuclideanDistance ed = new EuclideanDistance();
		
		// First, put K training instances into neighbours array
		for(int i=0; i<K; i++){
			trainList.get(i).distance = ed.getDistance(trainList.get(i), testInst);
			neighbours[i] = trainList.get(i);
		}
		// Iterate over remaining instances finding K nearest neighbours 
		for(int i=K; i<trainList.size(); i++){
			trainList.get(i).distance = ed.getDistance(trainList.get(i), testInst);
			// get the index of the neighbour with the largest distance to testInst
			int max = 0;
			for(int j=1; j<K; j++){
				if(neighbours[j].distance > neighbours[max].distance) max = j;
			}
			// add the current training instance if the distance is smaller than the max neighbour
			if(neighbours[max].distance > trainList.get(i).distance){
				neighbours[max] = trainList.get(i);
			}
		}
		// return the neighbours
		return neighbours;
	}

	/** Returns the most likely class label from a set of given neighbours
	 * 
	 * @param neighbours
	 * @return
	 */
	private String classify(TrainInstance[] neighbours) {	
		// Construct a map of <labels,weights>
		HashMap <String,Float> map = new HashMap<String,Float>();
		for(int i=0; i<neighbours.length; i++){
			TrainInstance temp = neighbours[i];
			String key = temp.label;
			// if label doesn't exist in map, add it with 1/temp.distance
			if(!map.containsKey(key)){
				map.put(key, 1/temp.distance);
			// else update the map by adding weight to the associated key
			}else{
				float val = map.get(key);
				val += 1/temp.distance;
				map.put(key, val);
			}
		}
		// Find most likely label
		float max = 0;
		String returnLabel = "";
		Set<String> labelSet = map.keySet();
		// Iterate over all labels and find the label with the highest weight
		for(String s: labelSet){
			float val = map.get(s);
			if(val > max){
				max = val;
				returnLabel = s;
			}
		}
		// return the most likely label
		return returnLabel;
	}

	/** Reads the training file and returns a list of all training instances
	 * 
	 * @param file
	 * @return list of training instances
	 */
	private static List<TrainInstance> readTrainFile(File file){
		// create an empty list to fill
		List<TrainInstance> trainList = new ArrayList<TrainInstance>();
		
		// read the training file and build training set of instances
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			//count the number of lines read
			int count = 0;
			// read each line of the file
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	Scanner lineScanner = new Scanner(line);
		    	// getting attributes and class label
		    	float[] attrs = new float[numAttr];
		    	String label = null;
		    	for(int i=0;i<numAttr;i++){
		    		if(lineScanner.hasNext()) attrs[i] = lineScanner.nextFloat();
		    	}
		    	if(lineScanner.hasNext()) label = lineScanner.next();
		    	if(label==null){break;}
		    	// creating and adding a new training instance
		    	trainList.add(new TrainInstance(attrs, label));
		    	// close scanner resource
		    	lineScanner.close();
		    	// for debugging
		    	//System.out.println(line);
		    	count++;
		    }
		    System.out.println("training file line count: " + count);
		    System.out.println("training set size: " + trainList.size());
		    // line is not visible here.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return the list
		return trainList;
	}
	
	/** Reads the test file and returns a set of all the training instances
	 * 
	 * @param file
	 * @return set of test instances
	 */	
	private static Set<TestInstance> readTestFile(File file) {
		// create an empty set to fill
		Set<TestInstance> testSet = new HashSet<TestInstance>();
		
		// read the training file and build training set of instances
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			// count the number of line read
			int count = 0;
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	Scanner lineScanner = new Scanner(line);
		    	// getting attributes and class label
		    	float[] attrs = new float[numAttr];
		    	String label = null;
		    	for(int i=0;i<numAttr;i++){
		    		if(lineScanner.hasNext()) attrs[i] = lineScanner.nextFloat();
		    	}
		    	if(lineScanner.hasNext()) label = lineScanner.next();
		    	// creating and adding a training instance
		    	if(label==null){break;}
		    	testSet.add(new TestInstance(attrs, label));
		    	// close scanner resource
		    	lineScanner.close();
		    	// for debugging
		    	//System.out.println(line);
		    	count++;
		    }
		    System.out.println("test file line count: " + count);
		    System.out.println("test set size: " + testSet.size());
		    // line is not visible here.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return the set
		return testSet;
	}
}
