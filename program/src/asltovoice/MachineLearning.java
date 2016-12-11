package asltovoice;
     
import java.util.ArrayList;
import java.util.Arrays;
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
        //List<float[]> nearestK = new ArrayList();
        //int[] nearestKIndexes = new int[k];
        int[] nearestKClasses = new int[k];
        float[] nearestKDistances = new float[k];
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
            nearestKClasses[i] = dataClasses[nearestIndex];
        }
        // return the majority class index of the nearest k
        Arrays.sort(nearestKClasses);
        int majorityClass = nearestKClasses[k / 2];
        return majorityClass;
    }
}
