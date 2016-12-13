package asltovoice;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;

import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class MachineLearning {

    public Classifier classifier;
    public Instances trainingData = null;
    //classifier = new J48();
    //classifier = new IBk();
    boolean hasModel = false;

    boolean HasModel() {
        return hasModel;
    }
    
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
    
    public void Classify(String fileLoc) {
        try {
            ConverterUtils.DataSource src = new ConverterUtils.DataSource(fileLoc);
            System.out.println("loaded file ");
            Instances classifyData = src.getDataSet();
        // set class index because this is not an ARFF
        if (classifyData.classIndex() == -1) {
            classifyData.setClassIndex(classifyData.numAttributes() - 1);
        }
        // TODO: one instance includes multiple frames
        // remove id, time, cur and total frames
        classifyData.deleteAttributeAt(3);
        classifyData.deleteAttributeAt(2);
        classifyData.deleteAttributeAt(1);
        classifyData.deleteAttributeAt(0);
        
        Classify(classifyData.get(0).toDoubleArray());
        
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    public void Classify(double[] data) {
        // TODO: pass in multiple rows of data for time
        // TODO: releif algorithm
        // to do do this somewhere else?
        DenseInstance di = new DenseInstance(trainingData.numAttributes());
        //classifier.getCapabilities().
        double[] classListD = trainingData.attributeToDoubleArray(trainingData.classIndex());
        int[] classListI = new int[classListD.length];
        double[] featureWeights = new double[trainingData.numAttributes()];
        for (int i = 0; i < trainingData.numAttributes(); i++) {
            di.setValue(i, data[i]);
            featureWeights[i] = 1;
        }
        
        di.setDataset(trainingData);

        // TODO call ANN and KNN here
        double[][] dataset = new double[trainingData.numInstances()][];
        for (int i = 0; i < trainingData.numInstances(); i++) {
            dataset[i] = trainingData.get(i).toDoubleArray();
            //dataset[i][dataset.length-1] = 0;
            //if (trainingData.checkForAttributeType(i))
            classListI[i] = (int) classListD[i];
        }
        
        double[] knnclassprob = KNN(dataset, classListI, featureWeights, data, 31);
        int knnclass = (int) knnclassprob[0];
        double knnprob = knnclassprob[1];
        try {
            int mlpclass = (int) classifier.classifyInstance(di);
            double[] mlpDist = classifier.distributionForInstance(di);
            System.out.print("The sign you signed is: \n");
            System.out.print("KNN: " + trainingData.classAttribute().value(knnclass) + "");
            System.out.print(", " + knnprob*100 + "% of nearest classes \n");
            System.out.print("MLP: " + trainingData.classAttribute().value(mlpclass) + "");
            System.out.print(", " + mlpDist[mlpclass]*100 + "%\n");
            //System.out.print("TODO: knn accuracy \n");
            for (int i=0; i<mlpDist.length; i++){
                // print mlp dists
                System.out.print(trainingData.classAttribute().value(i)+": "+(float)((int)(mlpDist[i]*10000))/100+"%\n");
            }
            //TODO: get accuracy
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void BuildModel() {
        System.out.println("Building Model with Multilayer Perceptron...");// + classifier.toString() + "...");
        // TODO: check file to make sure there is enough data?
        if (trainingData == null) {
            System.out.println("No training data! Please load data with [d]");
            return;
        }
        try {
            //TODO: check this
            MultilayerPerceptron mp = new MultilayerPerceptron();
            mp.setLearningRate(0);
            mp.setMomentum(0.2);
            mp.setTrainingTime(2000);
            mp.setHiddenLayers("3");
            classifier = mp;
            //mp.buildClassifier(trainingData);
            classifier.buildClassifier(trainingData);
            //TODO: do this here?
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
    public  double[] KNN(double[][] data, int[] dataClasses, double[] featureWeights, double[] point, int k) {
        // find distances to all points
        double[] distances = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            distances[i] = 0;
            for (int j = 0; j < point.length; j++) {
                distances[i] += featureWeights[j] * Math.sqrt(data[i][j] * data[i][j] + point[j] * point[j]);
            }
            //System.out.print(distances[i]+", ");
        }
//        System.out.print("\n");
        // find nearest k points
        double[] nearestKDistances = new double[k];
        HashMap<Integer, Integer> classAmounts = new HashMap<>();
        int majorityClass = -1;
        int majorityAmount = -1;
        for (int i = 0; i < k; i++) {
            nearestKDistances[i] = 9999999;
            int nearestIndex = -1;
            for (int j = 0; j < distances.length; j++) {
                if (distances[j] < nearestKDistances[i]) {
                    nearestKDistances[i] = distances[j];
                    nearestIndex = j;
                }
            }
            distances[nearestIndex] = 9999999;
            if (nearestIndex == -1){
                //no more classes
                //System.out.print("short\n ");
                break;
            }
            int kNearestClass = dataClasses[nearestIndex];
            // add one to the current amount of that key
            //classAmounts.putIfAbsent(kNearestClass, 0);
            classAmounts.put(kNearestClass, classAmounts.getOrDefault(kNearestClass, 0) + 1);
        //}
        // return the majority class index of the nearest k
            int entryVal = classAmounts.get(kNearestClass);
            //System.out.print(trainingData.classAttribute().value(kNearestClass)+":"+entryVal+" \n");
        //for (HashMap.Entry<Integer, Integer> entry : classAmounts.entrySet()) {
            if (entryVal > majorityAmount) {
                majorityAmount = entryVal;
                majorityClass = kNearestClass;
            }
        }
        //System.out.print("\n");
        double prob = (double)classAmounts.get(majorityClass) / k;
        double[] classProb = {majorityClass, prob};
        return classProb;
    }
}