package io.skymind.echidna.spark.join;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.canova.api.writable.Writable;

import java.io.Serializable;
import java.util.List;

/**
 * Simple helper class for executing joins
 */
@AllArgsConstructor @Data
public class JoinedValue implements Serializable {

    private final boolean haveLeft;
    private final boolean haveRight;
    private final List<Writable> values;

}
