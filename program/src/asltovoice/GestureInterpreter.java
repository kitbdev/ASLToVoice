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
    
    public int maxNoMovementFrames = 40;
    public int numContinuousNoMovementFrames = 0;
    
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
        // set class index because this is not an ARFF
        if (trainingData.classIndex() == -1) {
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
        }
    }
    
    ClassificationType GetClassificationType() {
        return classficationType;
    }
    
    void SetClassificationType(ClassificationType ct) {
        switch (ct) {
            case IBk:
                classifier = new IBk();
                break;
            case MultilayerPerceptron:
                classifier = new MultilayerPerceptron();
                break;
            case J48:
                classifier = new J48();
                break;
            case SMO:
                classifier = new SMO();
                break;
            default:
                System.out.println("ERROR: classifier not implemented!");
        }
//        classifier.buildClassifier(trainingData); 
       classficationType = ct;
    }
    
    // TODO make sure this works
    // TODO operate on entire current sign?
    boolean IsSignOver(FrameData frame) {
        float minMovement = 50f;
        float totalMovement = 0.0f;
        boolean isMoving = false;
        totalMovement+=frame.handVel.magnitude();
        if (frame.handVel.magnitude() > minMovement) {
            isMoving = true;
        }
        for (int i=0; i<5; i++) {
            totalMovement+=frame.fingerVel[i].magnitude();
            if (frame.fingerVel[i].magnitude() > minMovement) {
                isMoving = true;
            }
        }
        System.out.println(totalMovement);
//        return !isMoving;
        if (isMoving) {
            numContinuousNoMovementFrames = 0;
        } else {
            numContinuousNoMovementFrames++;
        }
        return numContinuousNoMovementFrames >= maxNoMovementFrames;
    }
    
    void ClassifyGesture(SignData gesture) {
        DenseInstance di = new DenseInstance(trainingData.numAttributes());
        try {
            int classIndex = (int) classifier.classifyInstance(di);
            double[] probDist = classifier.distributionForInstance(di);
            trainingData.classAttribute().value(classIndex);
            double choosenDist = probDist[classIndex]*100;
//            for (int i=0; i<probDist.length; i++){
//                //System.out.println((float)((int)(mlpDist[i]*10000))/100+"%");
//            }
            // get class name
            
            System.out.print("prob: "+((int)(choosenDist*100))/100.0+"%");
        } catch (Exception e) {
            System.out.println(e);
            return;
        }        
    }
}
