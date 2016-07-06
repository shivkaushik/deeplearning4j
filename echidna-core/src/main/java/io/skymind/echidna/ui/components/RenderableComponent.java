package io.skymind.echidna.ui.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(value={
        @JsonSubTypes.Type(value = RenderableComponentString.class, name = "RenderableComponentString"),
        @JsonSubTypes.Type(value = RenderableComponentLineChart.class, name = "RenderableComponentLineChart"),
        @JsonSubTypes.Type(value = RenderableComponentScatterPlot.class, name = "RenderableComponentScatterPlot"),
        @JsonSubTypes.Type(value = RenderableComponentTable.class, name = "RenderableComponentTable"),
        @JsonSubTypes.Type(value = RenderableComponentAccordionDecorator.class, name = "RenderableComponentAccordionDecorator"),
        @JsonSubTypes.Type(value = RenderableComponentHorizontalBarChart.class, name = "RenderableComponentBarChart"),
        @JsonSubTypes.Type(value = RenderableComponentStackedAreaChart.class, name = "RenderableComponentStackedAreaChart"),
        @JsonSubTypes.Type(value = RenderableComponentHistogram.class, name = "RenderableComponentHistogram")
})
@Data
public abstract class RenderableComponent {

    /** Component type: used by the Arbiter UI to determine how to decode and render the object which is
     * represented by the JSON representation of this object*/
    protected final String componentType;

    public RenderableComponent(String componentType){
        this.componentType = componentType;
    }

}
