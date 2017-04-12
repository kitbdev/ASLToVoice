package asltovoice;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import weka.classifiers.lazy.IBk;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;


// uses WEKA to determine the results of a gesture
// identifies when a gestures is finished
public class GestureInterpreter {
    
    // sensitivity to end gesture
    public float minMovementAmount = 125f;
    // time required to end gesture
    public int maxNoMovementFrames = 15;
    
    int numContinuousNoMovementFrames = 0;
    public boolean needsRebuilding = true;
    public boolean hasData = false;
    
    // enum for classifier types
    enum ClassificationType {
        IBk,
        MultilayerPerceptron,
        J48,
        SMO, // SupportVectorMachine
        NaiveBayes,
        LinearRegression, // will this even work?
    }
    private ClassificationType classficationType = ClassificationType.IBk;
    public Classifier classifier;
    public Instances trainingData = null;
    
    GestureInterpreter() {
        SetClassificationType(classficationType);
        hasData = false;
    }
    
    void LoadData(String fileLoc) {
        try {
            ConverterUtils.DataSource src = new ConverterUtils.DataSource(fileLoc);
            System.out.println("loaded file ");
            trainingData = src.getDataSet();
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        if (trainingData.numAttributes() != 323) {
            System.out.println("Unrecognized number of attributes: " + trainingData.numAttributes());
        }
        // set class index because this is not an ARFF
        if (trainingData.classIndex() == -1) {
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
        }
        // remove id attribute
        trainingData.deleteAttributeAt(0);
        needsRebuilding = true;
        hasData = true;
        System.out.println("Data loaded from file. ");
    }
    
    void BuildModel() {
        System.out.println("Building model with " + classficationType.toString() + "...");
        try {
            classifier.buildClassifier(trainingData);
            if (false) {
                Evaluation eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);
                String summary = eval.toSummaryString();
                System.out.println(summary);
            }
            needsRebuilding = false;
            System.out.println("Finished building model.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    ClassificationType GetClassificationType() {
        return classficationType;
    }
    String GetClassificationTypeString() {
        return classficationType.toString();
    }
    
    public void SetClassificationType(int ct) {
        if (ct==0) {
            SetClassificationType(ClassificationType.IBk);
        }
        if (ct==1) {
            SetClassificationType(ClassificationType.MultilayerPerceptron);
        }
        if (ct==2) {
            SetClassificationType(ClassificationType.J48);
        }
        if (ct==3) {
            SetClassificationType(ClassificationType.SMO);
        }
        if (ct==4) {
            SetClassificationType(ClassificationType.NaiveBayes);
        }
    }
    
    void SetClassificationType(ClassificationType ct) {
        switch (ct) {
            case IBk:
                classifier = new IBk();
                break;
            case MultilayerPerceptron:
                MultilayerPerceptron mp = new MultilayerPerceptron();
                mp.setLearningRate(0);
                mp.setMomentum(0.2);
                mp.setTrainingTime(2000);
                mp.setHiddenLayers("3");
                classifier = mp;
                break;
            case J48:
                classifier = new J48();
                break;
            case SMO:
                classifier = new SMO();
                break;
            case NaiveBayes:
                classifier = new NaiveBayes();
                break;
            default:
                System.out.println("ERROR: classifier not implemented!");
        }
        classficationType = ct;
        needsRebuilding = true;
    }
    // TODO make sure this works
    // TODO operate on entire current sign?
    boolean IsSignOver(FrameData frame) {
        float totalMovement = 0.0f;
        boolean isMoving = false;
        totalMovement+=frame.handVel.magnitude();
        if (frame.handVel.magnitude() > minMovementAmount) {
            isMoving = true;
        }
        for (int i=0; i<5; i++) {
            totalMovement+=frame.fingerVel[i].magnitude();
            if (frame.fingerVel[i].magnitude() > minMovementAmount) {
                isMoving = true;
            }
        }
        //TODO: test total movement instead of individual movement?
        System.out.println(totalMovement);
        if (isMoving) {
            numContinuousNoMovementFrames = 0;
        } else {
            numContinuousNoMovementFrames++;
        }
        return numContinuousNoMovementFrames >= maxNoMovementFrames;
    }
    
    String ClassifyGesture(double[] newData) {
        if (!hasData) {
            System.out.println("load data");
            return "ERROR: load data";
        }
        
        try {
            DenseInstance di = new DenseInstance(trainingData.numAttributes());
            
            for (int i = 0; i < trainingData.numAttributes()-1; i++) {
                di.setValue(i, newData[i]);
            }
            di.setDataset(trainingData);
            
            int chooseni = (int) classifier.classifyInstance(di);
            String choosen = trainingData.classAttribute().value(chooseni);
            double[] choosenDistribution = classifier.distributionForInstance(di);
            double choosenprob = choosenDistribution[chooseni]*100;
            System.out.print("choosen sign: "+choosen);
            System.out.print("probability: "+((int)(choosenprob*100))/100.0+"%");
            
            return choosen;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "ERROR: classification failed";
    }
}
