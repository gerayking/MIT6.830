package simpledb.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import simpledb.common.Type;
import simpledb.storage.Field;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.storage.TupleIterator;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbField;
    private Type gbFieldType;
    private int aField;
    private Op aggOp;
    private TupleDesc tupleDesc;
    private Map<Field, List<Field>> group;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        gbField = gbfield;
        gbFieldType = gbfieldtype;
        aField = afield;
        aggOp = what;
        group = new HashMap<>();
        if(gbfield != -1){
            Type[] types = new Type[2];
            types[0] = gbfieldtype;
            types[1] = Type.INT_TYPE;
            tupleDesc = new TupleDesc(types);
        }else{
            Type[] types = new Type[1];
            types[0] = Type.INT_TYPE;
            tupleDesc = new TupleDesc(types);
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field field = tup.getField(aField);
        Field groupField = null;
        if(gbField != -1){
            groupField = tup.getField(gbField);
        }
        if(group.containsKey(groupField)){
            group.get(groupField).add(field);
        }else{
            ArrayList<Field> fields = new ArrayList<>();
            fields.add(field);
            group.put(groupField, fields);
        }
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
        ArrayList<Tuple> tuples = new ArrayList<>();
        switch (aggOp){
            case COUNT:
                for (Field field : group.keySet()) {
                    Tuple tuple = new Tuple(tupleDesc);
                    tuple.setField(0,field);
                    if(field != null){
                        tuple.setField(1,new IntField(group.get(field).size()));
                    }else{
                        tuple.setField(0,new IntField(group.get(field).size()));
                    }
                    tuples.add(tuple);
                }
                break;
        }
        return new TupleIterator(tupleDesc,tuples);
    }

}
