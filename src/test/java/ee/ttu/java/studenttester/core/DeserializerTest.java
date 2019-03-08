package ee.ttu.java.studenttester.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ttu.java.studenttester.core.annotations.Identifier;
import ee.ttu.java.studenttester.core.models.TesterContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeserializerTest {

    @Test
    private void testContextDeserialize() throws Exception {
        var json = getClass().getResourceAsStream("/example.json").readAllBytes();
        var mapper = new ObjectMapper();
        var obj = mapper.readValue(json, TesterContext.class);
        Assert.assertEquals(obj.results.size(), 5);
    }
}
