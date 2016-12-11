package asltovoice;
     

import java.util.HashMap;
import weka.classifiers.Classifier;

import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class MachineLearning {
    
    public static Classifier classifier;
    public static Instances trainingData = null;
    //classifier = new J48();
    //classifier = new IBk();
    public static boolean hasModel = false;
    
    void LoadTrainingData(String fileLoc) {
        try {
            ConverterUtils.DataSource src = new ConverterUtils.DataSource(fileLoc);
            System.out.println("loaded file ");
            trainingData = src.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // set class index because this is not an ARFF
        if (trainingData.classIndex() == -1) {
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
        }
        // TODO: one instance includes multiple frames
        // remove id, time, cur and total frames
        trainingData.deleteAttributeAt(3);
        trainingData.deleteAttributeAt(2);
        trainingData.deleteAttributeAt(1);
        trainingData.deleteAttributeAt(0);
        // TODO: any other preprocessing?
    }
    
    public void Classify(float[] data) {
        
        DenseInstance di = new DenseInstance(trainingData.numAttributes());
        //classifier.getCapabilities().
        for (int i = 0; i < trainingData.numAttributes(); i++) {
            di.setValue(i, data[i]);
        }
        
        di.setDataset(trainingData);
        try {
            double n = classifier.classifyInstance(di);
            System.out.print("The sign you signed is: \n");
            Thread.sleep(1000);
            System.out.print(">" + (int)n + "<\n");
            Thread.sleep(1000);
            //TODO: get accuracy
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    
    public void BuildModel() {
        System.out.println("Building Model with " + classifier.toString() + "...");
            // TODO: check file to make sure there is enough data?
            if (trainingData == null) {
                System.out.println("No training data! Please load data with [d]");
                return;
            }
            try {
                classifier.buildClassifier(trainingData);
                Evaluation eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);
                String summ = eval.toSummaryString();
                System.out.println(summ);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("Finished training the model");
            // classifier.getCapabilities() instead of
            hasModel = true;
    }
    
    public void SaveModel(String modelSavePath) {
        System.out.printf("Saving model to %s...", modelSavePath);
        try {
            weka.core.SerializationHelper.write(modelSavePath, classifier);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Model Saved!");
    }
    
    public void LoadModel(String fileLoc) {
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(fileLoc);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        hasModel = true;
    }
    // performs k nearest neighbors to classify point
    public static int KNN(float[][] data, int[] dataClasses, float[] point, int k) {
        // find distances to all points
        float[] distances = new float[data.length];
        for (int i=0; i<data.length; i++) {
            distances[i] = 0;
            for (int j=0; j<point.length; j++) {
                distances[i] += Math.sqrt(data[i][j] * data[i][j] + point[j] * point[j]);
            }
        }
        // find nearest k points
        float[] nearestKDistances = new float[k];
        HashMap<Integer, Integer> classAmounts = new HashMap<Integer, Integer>();
        for (int i=0; i<k; i++) {
            nearestKDistances[i] = 9999999;
            int nearestIndex = -1;
            for (int j=0; j<data.length; j++) {
                if (distances[j] < nearestKDistances[i]) {
                    nearestKDistances[i] = distances[j];
                    nearestIndex = j;
                    distances[j] = 9999999;
                }
            }
            int kNearestClass = dataClasses[nearestIndex];
            // add one to the current amount of that key
            //classAmounts.putIfAbsent(kNearestClass, 0);
            classAmounts.put(kNearestClass, classAmounts.getOrDefault(kNearestClass, 0)+1);
        }
        // return the majority class index of the nearest k
        int majorityClass = -1;
        int majorityAmount = -1;
        for(HashMap.Entry<Integer, Integer> entry : classAmounts.entrySet()) {
            if (entry.getValue() > majorityAmount) {
                majorityAmount = entry.getValue();
                majorityClass = entry.getKey();
            }
        }
        return majorityClass;
    }

    
}