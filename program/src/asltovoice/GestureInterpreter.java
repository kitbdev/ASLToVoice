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
    
    // enum for classifier types
    enum ClassificationType {
        IBk,
        MultilayerPerceptron,
        J48,
        NaiveBayes,
        LinearRegression, // will this even work?
        SMO, // SupportVectorMachine
    }
    private ClassificationType classficationType = ClassificationType.IBk;
    public Classifier classifier;
    public Instances trainingData = null;
    
    GestureInterpreter() {
        SetClassificationType(classficationType);
    }
    
    ClassificationType GetClassificationType() {
        return classficationType;
    }
    
    void SetClassificationType(ClassificationType ct) {
        switch (ct) {
            case IBk:
                classifier = new IBk();
                break;
            default:
                classifier = new IBk();
                System.out.println("classifier not implemented!");
        }
//        classifier.buildClassifier(trainingData); 
       classficationType = ct;
    }
    
    // TODO make sure this works
    boolean IsSignOver(FrameData frame) {
        float minMovement = 0.05f;
        boolean isMoving = false;
        if (frame.handVel.magnitude() > minMovement) {
            isMoving = true;
        }
        for (int i=0; i<5; i++) {
            if (frame.fingerVel[i].magnitude() > minMovement) {
                isMoving = true;
            }
        }
        return !isMoving;
    }
    
    
    void ClassifyGesture(SignData gesture) {
       
    }
}
