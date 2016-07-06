package io.skymind.echidna.api.sequence.split;

import org.canova.api.writable.Writable;
import io.skymind.echidna.api.ColumnType;
import io.skymind.echidna.api.schema.Schema;
import io.skymind.echidna.api.sequence.SequenceSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Split a sequence into multiple sequences, based on the separation of time steps in a time column.
 * For example, suppose we have a sequence with a gap of 1 day between two blocks of entries: we can use
 * SequenceSplitTimeSeparation to split this data into two separate sequences.
 *
 * More generally, split the sequence any time the separation between consecutive time steps exceeds a specified
 * value.
 *
 * @author Alex Black
 */
public class SequenceSplitTimeSeparation implements SequenceSplit {

    private final String timeColumn;
    private final long timeQuantity;
    private final TimeUnit timeUnit;
    private final long separationMilliseconds;
    private int timeColumnIdx = -1;
    private Schema schema;

    /**
     * @param timeColumn      Time column to consider when splitting
     * @param timeQuantity    Value/amount (of the specified TimeUnit)
     * @param timeUnit        The unit of time
     */
    public SequenceSplitTimeSeparation(String timeColumn, long timeQuantity, TimeUnit timeUnit){
        this.timeColumn = timeColumn;
        this.timeQuantity = timeQuantity;
        this.timeUnit = timeUnit;

        this.separationMilliseconds = TimeUnit.MILLISECONDS.convert(timeQuantity,timeUnit);
    }

    @Override
    public List<List<List<Writable>>> split(List<List<Writable>> sequence) {

        List<List<List<Writable>>> out = new ArrayList<>();

        long lastTimeStepTime = Long.MIN_VALUE;
        List<List<Writable>> currentSplit = null;

        for(List<Writable> timeStep : sequence){
            long currStepTime = timeStep.get(timeColumnIdx).toLong();
            if(lastTimeStepTime == Long.MIN_VALUE || (currStepTime-lastTimeStepTime) > separationMilliseconds){
                //New split
                if(currentSplit != null) out.add(currentSplit);
                currentSplit = new ArrayList<>();
            }
            currentSplit.add(timeStep);
            lastTimeStepTime = currStepTime;
        }

        //Add the final split to the output...
        out.add(currentSplit);

        return out;
    }

    @Override
    public void setInputSchema(Schema inputSchema) {
        if(!inputSchema.hasColumn(timeColumn)) throw new IllegalStateException("Invalid state: schema does not have column "
            + "with name \"" + timeColumn + "\"");
        if(inputSchema.getMetaData(timeColumn).getColumnType() != ColumnType.Time){
            throw new IllegalStateException("Invalid input schema: schema column \"" + timeColumn + "\" is not a time column." +
                " (Is type: " + inputSchema.getMetaData(timeColumn).getColumnType() + ")");
        }

        this.timeColumnIdx = inputSchema.getIndexOfColumn(timeColumn);
        this.schema = inputSchema;
    }

    @Override
    public Schema getInputSchema() {
        return schema;
    }

    @Override
    public String toString(){
        return "SequenceSplitTimeSeparation(timeColumn=\"" + timeColumn + "\",timeQuantity=" + timeQuantity + ",timeUnit=" + timeUnit + ")";
    }
}
