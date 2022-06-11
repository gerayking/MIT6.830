package simpledb.optimizer;

import java.util.Arrays;
import simpledb.execution.Predicate;
import simpledb.execution.Predicate.Op;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

    class MyGram{
        double left;
        double right;
        int h;
        double w;

        public MyGram(double left, double right) {
            this.left = left;
            this.right = right;
            this.w = right-left;
        }
        public boolean inRange(int v){
            if (v >= left && v < right) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "MyGram{" +
                "left=" + left +
                ", right=" + right +
                ", h=" + h +
                ", w=" + w +
                '}';
        }
    }
    private int min;
    private int max;
    private int buckets;
    private int ntups;
    private double avgwidth;
    private MyGram[] myGrams;
    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        this.buckets = buckets;
        this.min = min;
        this.max = max;
        this.avgwidth = Math.max(1.0,(double)(max-min)/buckets);
        this.ntups = 0;
        myGrams = new MyGram[buckets];
        for(int i=0;i<buckets;i++){
            myGrams[i] = new MyGram(min+i*avgwidth,min+(i+1)*avgwidth);
        }

    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
        if(v<min||v>max){
            System.out.println(v);
            return ;
        }
        if(v==max){
            myGrams[buckets-1].h++;
            ntups++;
            return ;
        }
        for(int i=0;i<buckets;i++){
           if(myGrams[i].inRange(v)){
               myGrams[i].h++;
               ntups++;
               return ;
           }
        }
    }
    private int getIndex(int v){
        for(int i=0;i<buckets;i++){
            if(myGrams[i].inRange(v))return i;
        }
        return -1;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {
        int index = getIndex(v);
        switch (op){
            case EQUALS:
                for(int i=0;i<buckets;i++){
                    if(myGrams[i].inRange(v)){
                        return (1.0*myGrams[i].h/myGrams[i].w)/(double)ntups;
                    }
                }
                return 0.0;
            case NOT_EQUALS:
                return 1 - estimateSelectivity(Op.EQUALS,v);
            case GREATER_THAN:
                if(v <= min)return 1.0;
                if(v >= max)return 0.0;
                int cnt = 0;
                for(int i = index + 1;i<buckets;i++){
                    cnt += myGrams[i].h;
                }
                // b_f = h_b / ntups
                double b_f = (double)myGrams[index].h / ntups;
                double b_part = (myGrams[index].right-v)/myGrams[index].w;
                double b_ctr = b_f * b_part +1.0*cnt/ntups;
                return b_ctr;
            case LESS_THAN:
                if(v<= min)return 0.0;
                if(v>= max)return 1.0;
                return 1 - estimateSelectivity(Op.GREATER_THAN,v);
            case LESS_THAN_OR_EQ:
                if(v<=min)return 0.0;
                if(v>=max)return 1.0;
                return estimateSelectivity(Op.LESS_THAN,v) + estimateSelectivity(Op.EQUALS, v);
            case GREATER_THAN_OR_EQ:
                if(v<=min)return 1.0;
                if(v>=max)return 0.0;
                return estimateSelectivity(Op.GREATER_THAN, v) + estimateSelectivity(Op.EQUALS,v);
        }
    	// some code goes here
        return -1.0;
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        return this.avgwidth;
        // some code goes here
    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    @Override
    public String toString() {
        return "IntHistogram{" +
            "min=" + min +
            ", max=" + max +
            ", buckets=" + buckets +
            ", ntups=" + ntups +
            ", avgwidth=" + avgwidth +
            ", myGrams=" + Arrays.toString(myGrams) +
            '}';
    }
}
