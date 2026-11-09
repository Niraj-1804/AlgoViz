package com.algoviz.visualization;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AlgorithmServiceTest {
    private final AlgorithmService service=new AlgorithmService();
    @Test void bubbleSortReturnsStepsAndSortedValues(){
        var result=service.run("bubble-sort",List.of(5,1,3),null);
        assertEquals(List.of(1,3,5),result.result());
        assertTrue(result.steps().stream().anyMatch(s->s.operation().equals("swap")));
    }
    @Test void linearSearchFindsTarget(){
        var result=service.run("linear-search",List.of(5,1,3),1);
        assertTrue(result.steps().stream().anyMatch(s->s.operation().equals("found")));
    }
}
