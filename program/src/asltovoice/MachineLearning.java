// package asltovoice;

// import java.util.HashMap;
// import weka.classifiers.Classifier;

// import weka.classifiers.Evaluation;
// import weka.classifiers.lazy.IBk;
// import weka.classifiers.functions.MultilayerPerceptron;
// import weka.core.DenseInstance;
// import weka.core.Instances;
// import weka.core.converters.ConverterUtils;

// public class MachineLearning {

    // public Classifier classifier;
    // public Instances trainingData = null;
    // //classifier = new J48();
    // //classifier = new IBk();
    // boolean hasModel = false;
    // public TTS tts;
    
    // boolean HasModel() {
        // return hasModel;
    // }
    
    // // loads the csv file with training data
    // void LoadTrainingData(String fileLoc) {
        // try {
            // ConverterUtils.DataSource src = new ConverterUtils.DataSource(fileLoc);
            // System.out.println("loaded file ");
            // trainingData = src.getDataSet();
        // } catch (Exception e) {
            // System.out.println(e);
            // return;
        // }
        // // set class index because this is a csv
        // if (trainingData.classIndex() == -1) {
            // trainingData.setClassIndex(trainingData.numAttributes() - 1);
        // }
        // // TODO: one instance will include multiple frame
        // // remove id, time, cur and total frames
        // trainingData.deleteAttributeAt(3);
        // trainingData.deleteAttributeAt(2);
        // trainingData.deleteAttributeAt(1);
        // trainingData.deleteAttributeAt(0);
        // // TODO: any other preprocessing?
    // }
    
    // // loads a file of data to be classified
    // // unused
    // public void Classify(String fileLoc) {
        // try {
            // ConverterUtils.DataSource src = new ConverterUtils.DataSource(fileLoc);
            // System.out.println("loaded file ");
            // Instances classifyData = src.getDataSet();
        // // set class index because this is not an ARFF
        // if (classifyData.classIndex() == -1) {
            // classifyData.setClassIndex(classifyData.numAttributes() - 1);
        // }
        // // TODO: one instance includes multiple frames
        // // remove id, time, cur and total frames
        // classifyData.deleteAttributeAt(3);
        // classifyData.deleteAttributeAt(2);
        // classifyData.deleteAttributeAt(1);
        // classifyData.deleteAttributeAt(0);
        
        // Classify(classifyData.get(0).toDoubleArray());
        
        // } catch (Exception e) {
            // System.out.println(e);
            // return;
        // }
    // }
    
    // // prints the sign using the built model and the training data on the given data
    // public void Classify(double[] data) {
        // // TODO: new data format
        // // TODO: relief algorithm on data
        // DenseInstance di = new DenseInstance(trainingData.numAttributes());
        // double[] classListD = trainingData.attributeToDoubleArray(trainingData.classIndex());
        // int[] classListI = new int[classListD.length];
        // double[] featureWeights = new double[trainingData.numAttributes()];
        // for (int i = 0; i < trainingData.numAttributes(); i++) {
            // di.setValue(i, data[i]);
            // featureWeights[i] = 1;
        // }
        
        // di.setDataset(trainingData);

        // double[][] dataset = new double[trainingData.numInstances()][];
        // for (int i = 0; i < trainingData.numInstances(); i++) {
            // dataset[i] = trainingData.get(i).toDoubleArray();
            // //dataset[i][dataset.length-1] = 0;
            // //if (trainingData.checkForAttributeType(i))
            // classListI[i] = (int) classListD[i];
        // }
        
        // try {
            // // classify with KNN
            // double[] knnclassprob = KNN(dataset, classListI, featureWeights, data, 31);
            // int knnclass = (int) knnclassprob[0];
            // double knnprob = knnclassprob[1]*100;
            // // classify with MLP
            // int mlpclass = (int) classifier.classifyInstance(di);
            // double[] mlpdist = classifier.distributionForInstance(di);
            // double mlpprob = mlpdist[mlpclass]*100;
            // // print mlp distributions
            // for (int i=0; i<mlpdist.length; i++){
                // System.out.print(trainingData.classAttribute().value(i)+": "+(float)((int)(mlpdist[i]*10000))/100+"%, ");
            // }
            // System.out.println();
            
            // // print the result
            // System.out.print("The sign you signed is: \n");
            
            // System.out.print("KNN: " + trainingData.classAttribute().value(knnclass) + "");
            // System.out.print(", " + knnprob + "% of nearest classes \n");
            
            // System.out.print("MLP: " + trainingData.classAttribute().value(mlpclass) + "!\n");
            // System.out.print(", " + mlpprob + "%\n");
            // // voice the output of the highest probability
            // tts.speak("You signed "+trainingData.classAttribute().value(mlpprob>knnprob?mlpclass:knnclass));
        // } catch (Exception e) {
            // System.out.println(e);
            // return;
        // }
    // }

    // // build a MLP model with weka 
    // public void BuildModel() {
        // System.out.println("Building Model with Multilayer Perceptron...");// + classifier.toString() + "...");
        // // TODO: check file to make sure it is correct?
        // if (trainingData == null) {
            // System.out.println("No training data! Please load data with [d]");
            // return;
        // }
        // try {
            // MultilayerPerceptron mp = new MultilayerPerceptron();
            // mp.setLearningRate(0);
            // mp.setMomentum(0.2);
            // mp.setTrainingTime(2000);
            // mp.setHiddenLayers("3");
            // classifier = mp;
            // classifier.buildClassifier(trainingData);
            // Evaluation eval = new Evaluation(trainingData);
            // eval.evaluateModel(classifier, trainingData);
            // String summ = eval.toSummaryString();
            // System.out.println(summ);
        // } catch (Exception e) {
            // System.out.println(e);
        // }
        // System.out.println("Finished training the model");
        // hasModel = true;
    // }

    // public void SaveModel(String modelSavePath) {
        // System.out.printf("Saving model to %s...", modelSavePath);
        // try {
            // weka.core.SerializationHelper.write(modelSavePath, classifier);
        // } catch (Exception e) {
            // System.out.println(e);
            // return;
        // }
        // System.out.println("Model Saved!");
    // }

    // public void LoadModel(String fileLoc) {
        // try {
            // classifier = (Classifier) weka.core.SerializationHelper.read(fileLoc);
        // } catch (Exception e) {
            // System.out.println(e);
            // return;
        // }
        // hasModel = true;
    // }
    
    // // performs k nearest neighbors to classify point
    // public  double[] KNN(double[][] data, int[] dataClasses, double[] featureWeights, double[] point, int k) {
        // // find distances to all points
        // double[] distances = new double[data.length];
        // for (int i = 0; i < data.length; i++) {
            // distances[i] = 0;
            // for (int j = 0; j < point.length; j++) {
                // double t = data[i][j] - point[j];
                // distances[i] += featureWeights[j] * (t*t);
            // }
            // distances[i] = Math.sqrt(distances[i]);
        // }
        // // find nearest k points
        // double[] nearestKDistances = new double[k];
        // HashMap<Integer, Integer> classAmounts = new HashMap<>();
        // int majorityClass = -1;
        // int majorityAmount = -1;
        // for (int i = 0; i < k; i++) {
            // nearestKDistances[i] = 9999999;
            // int nearestIndex = -1;
            // for (int j = 0; j < distances.length; j++) {
                // if (distances[j] < nearestKDistances[i]) {
                    // nearestKDistances[i] = distances[j];
                    // nearestIndex = j;
                // }
            // }
            // if (nearestIndex == -1){
                // //no more classes
                // break;
            // }
            // distances[nearestIndex] = 9999999;
            
            // int kNearestClass = dataClasses[nearestIndex];
            // // add one to the current amount of that key
            // //classAmounts.putIfAbsent(kNearestClass, 0);
            // classAmounts.put(kNearestClass, classAmounts.getOrDefault(kNearestClass, 0) + 1);
        // //}
        // // return the majority class index of the nearest k
            // int entryVal = classAmounts.get(kNearestClass);
            // // TODO: print out the percentages, not the count
            // System.out.print(trainingData.classAttribute().value(kNearestClass)+":"+entryVal+", ");
        // //for (HashMap.Entry<Integer, Integer> entry : classAmounts.entrySet()) {
            // if (entryVal > majorityAmount) {
                // majorityAmount = entryVal;
                // majorityClass = kNearestClass;
            // }
        // }
        // System.out.println();
        // double prob = (double)classAmounts.get(majorityClass) / k;
        // double[] classProb = {majorityClass, prob};
        // return classProb;
    // }
// }