package asltovoice;
     
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MachineLearning {
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
