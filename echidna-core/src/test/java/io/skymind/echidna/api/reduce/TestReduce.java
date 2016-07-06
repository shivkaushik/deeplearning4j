package io.skymind.echidna.api.reduce;

import org.canova.api.io.data.DoubleWritable;
import org.canova.api.io.data.IntWritable;
import org.canova.api.io.data.LongWritable;
import org.canova.api.io.data.Text;
import org.canova.api.writable.Writable;
import io.skymind.echidna.api.ColumnType;
import io.skymind.echidna.api.ReduceOp;
import io.skymind.echidna.api.condition.Condition;
import io.skymind.echidna.api.condition.ConditionOp;
import io.skymind.echidna.api.condition.column.StringColumnCondition;
import io.skymind.echidna.api.metadata.ColumnMetaData;
import io.skymind.echidna.api.metadata.StringMetaData;
import io.skymind.echidna.api.schema.Schema;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Alex on 21/03/2016.
 */
public class TestReduce {

    @Test
    public void testReducerDouble(){

        List<List<Writable>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new DoubleWritable(0)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new DoubleWritable(1)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new DoubleWritable(2)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new DoubleWritable(2)));

        Map<ReduceOp,Double> exp = new LinkedHashMap<>();
        exp.put(ReduceOp.Min,0.0);
        exp.put(ReduceOp.Max,2.0);
        exp.put(ReduceOp.Range,2.0);
        exp.put(ReduceOp.Sum,5.0);
        exp.put(ReduceOp.Mean,1.25);
        exp.put(ReduceOp.Stdev,0.957427108);
        exp.put(ReduceOp.Count,4.0);
        exp.put(ReduceOp.CountUnique,3.0);
        exp.put(ReduceOp.TakeFirst,0.0);
        exp.put(ReduceOp.TakeLast,2.0);

        for(ReduceOp op : exp.keySet()){

            Schema schema = new Schema.Builder()
                    .addColumnString("key")
                    .addColumnDouble("column").build();

            Reducer reducer = new Reducer.Builder(op)
                    .keyColumns("key")
                    .build();

            reducer.setInputSchema(schema);

            List<Writable> out = reducer.reduce(inputs);

            assertEquals(2,out.size());

            assertEquals(out.get(0),new Text("someKey"));

            String msg = op.toString();
            assertEquals(msg, exp.get(op), out.get(1).toDouble(), 1e-5);
        }
    }

    @Test
    public void testReducerInteger(){

        List<List<Writable>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(0)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(1)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(2)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(2)));

        Map<ReduceOp,Double> exp = new LinkedHashMap<>();
        exp.put(ReduceOp.Min,0.0);
        exp.put(ReduceOp.Max,2.0);
        exp.put(ReduceOp.Range,2.0);
        exp.put(ReduceOp.Sum,5.0);
        exp.put(ReduceOp.Mean,1.25);
        exp.put(ReduceOp.Stdev,0.957427108);
        exp.put(ReduceOp.Count,4.0);
        exp.put(ReduceOp.CountUnique,3.0);
        exp.put(ReduceOp.TakeFirst,0.0);
        exp.put(ReduceOp.TakeLast,2.0);

        for(ReduceOp op : exp.keySet()){

            Schema schema = new Schema.Builder()
                    .addColumnString("key")
                    .addColumnInteger("column").build();

            Reducer reducer = new Reducer.Builder(op)
                    .keyColumns("key")
                    .build();

            reducer.setInputSchema(schema);

            List<Writable> out = reducer.reduce(inputs);

            assertEquals(2,out.size());

            assertEquals(out.get(0),new Text("someKey"));

            String msg = op.toString();
            assertEquals(msg, exp.get(op), out.get(1).toDouble(), 1e-5);
        }
    }

    @Test
    public void testReduceIntegerIgnoreInvalidValues(){

        List<List<Writable>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new Text("0")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new Text("1")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(2)));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new Text("ignore me")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new Text("also ignore me")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new Text("2")));


        Map<ReduceOp,Double> exp = new LinkedHashMap<>();
        exp.put(ReduceOp.Min,0.0);
        exp.put(ReduceOp.Max,2.0);
        exp.put(ReduceOp.Range,2.0);
        exp.put(ReduceOp.Sum,5.0);
        exp.put(ReduceOp.Mean,1.25);
        exp.put(ReduceOp.Stdev,0.957427108);
        exp.put(ReduceOp.Count,4.0);
        exp.put(ReduceOp.CountUnique,3.0);
        exp.put(ReduceOp.TakeFirst,0.0);
        exp.put(ReduceOp.TakeLast,2.0);

        for(ReduceOp op : exp.keySet()){
            Schema schema = new Schema.Builder()
                    .addColumnString("key")
                    .addColumnInteger("column").build();

            Reducer reducer = new Reducer.Builder(op)
                    .keyColumns("key")
                    .setIgnoreInvalid("column")
                    .build();

            reducer.setInputSchema(schema);

            List<Writable> out = reducer.reduce(inputs);

            assertEquals(2,out.size());

            assertEquals(out.get(0),new Text("someKey"));

            String msg = op.toString();
            assertEquals(msg, exp.get(op), out.get(1).toDouble(), 1e-5);
        }

        for(ReduceOp op : Arrays.asList(ReduceOp.Min, ReduceOp.Max, ReduceOp.Range, ReduceOp.Sum, ReduceOp.Mean, ReduceOp.Stdev)){
            //Try the same thing WITHOUT setIgnoreInvalid -> expect exception

            Schema schema = new Schema.Builder()
                    .addColumnString("key")
                    .addColumnInteger("column").build();

            Reducer reducer = new Reducer.Builder(op)
                    .keyColumns("key")
                    .build();
            reducer.setInputSchema(schema);

            try{
                reducer.reduce(inputs);
                fail("No exception thrown for invalid input: op=" + op);
            }catch(NumberFormatException e){
                //ok
            }
        }
    }


    @Test
    public void testCustomReductions(){

        List<List<Writable>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(1), new Text("zero")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(2), new Text("one")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(3), new Text("two")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(4), new Text("three")));

        List<Writable> expected = Arrays.asList((Writable)new Text("someKey"), new LongWritable(10), new Text("one"));


        Schema schema = new Schema.Builder()
                .addColumnString("key")
                .addColumnInteger("intCol")
                .addColumnString("textCol")
                .build();

        Reducer reducer = new Reducer.Builder(ReduceOp.Sum)
                .keyColumns("key")
                .customReduction("textCol",new CustomReduceTakeSecond())
                .build();

        reducer.setInputSchema(schema);

        List<Writable> out = reducer.reduce(inputs);

        assertEquals(3,out.size());
        assertEquals(expected, out);

        //Check schema:
        String[] expNames = new String[]{"key", "sum(intCol)", "myCustomReduce(textCol)"};
        ColumnType[] expTypes = new ColumnType[]{ColumnType.String, ColumnType.Long, ColumnType.String};
        Schema outSchema = reducer.transform(schema);

        assertEquals(3, outSchema.numColumns());
        for( int i=0; i<3; i++ ){
            assertEquals(expNames[i], outSchema.getName(i));
            assertEquals(expTypes[i], outSchema.getType(i));
        }
    }

    private static class CustomReduceTakeSecond implements ColumnReduction {

        @Override
        public Writable reduceColumn(List<Writable> columnData) {
            //For testing: let's take the second value
            return columnData.get(1);
        }

        @Override
        public String getColumnOutputName(String columnInputName) {
            return "myCustomReduce(" + columnInputName + ")";
        }

        @Override
        public ColumnMetaData getColumnOutputMetaData(ColumnMetaData columnInputMeta) {
            return new StringMetaData();
        }
    }



    @Test
    public void testConditionalReduction(){

        Schema schema = new Schema.Builder()
                .addColumnString("key")
                .addColumnInteger("intCol")
                .addColumnString("filterCol")
                .addColumnString("textCol")
                .build();

        List<List<Writable>> inputs = new ArrayList<>();
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(1), new Text("a"), new Text("zero")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(2), new Text("b"), new Text("one")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(3), new Text("a"), new Text("two")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(4), new Text("b"), new Text("three")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(5), new Text("a"), new Text("three")));
        inputs.add(Arrays.asList((Writable)new Text("someKey"), new IntWritable(6), new Text("b"), new Text("three")));

        Condition condition = new StringColumnCondition("filterCol", ConditionOp.Equal, "a");

        Reducer reducer = new Reducer.Builder(ReduceOp.Stdev)
                .keyColumns("key")
                .conditionalReduction("intCol","sumOfAs",ReduceOp.Sum, condition)   //Sum, only where 'filterCol' == "a"
                .countUniqueColumns("filterCol","textCol")
                .build();

        reducer.setInputSchema(schema);

        List<Writable> out = reducer.reduce(inputs);
        List<Writable> expected = Arrays.asList((Writable)new Text("someKey"), new LongWritable(1+3+5), new IntWritable(2), new IntWritable(4));

        assertEquals(4,out.size());
        assertEquals(expected, out);

        Schema outSchema = reducer.transform(schema);
        assertEquals(4, outSchema.numColumns());
        assertEquals(Arrays.asList("key","sumOfAs","countUnique(filterCol)", "countUnique(textCol)"), outSchema.getColumnNames());
        assertEquals(Arrays.asList(ColumnType.String, ColumnType.Long, ColumnType.Integer, ColumnType.Integer), outSchema.getColumnTypes());
    }
}
