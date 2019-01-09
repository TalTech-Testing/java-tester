package ee.ttu.java.studenttester.core.models;

import ee.ttu.java.studenttester.core.models.reports.AbstractReport;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ResultContext extends ArrayList<AbstractReport> {

    public void putResult(AbstractReport reportElement) {
        this.add(reportElement);
    }

    public <T extends AbstractReport> T getResultByType(Class<T> type) {
        return this.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        if (this.size() != 0) {
            return String.format("Result context for %d runner(s): %s", this.size(),
                    this.stream().map(AbstractReport::getIdentifier).collect(Collectors.toList()));
        }
        return "Empty result context";
    }

    /*public static class Deserializer extends JsonDeserializer<ResultContext> {

        @Override
        public ResultContext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var resultTypes = new Reflections(ResultContext.class.getPackageName())
                    .getTypesAnnotatedWith(RunnerResult.class);
            var resultTypeMap = resultTypes.stream()
                    .collect(Collectors.toMap(
                            cls -> cls.getAnnotation(RunnerResult.class).identifier(),
                            Function.identity())
                    );
            var mapper = new ObjectMapper();
            var resultContext = new ResultContext();
            var obj = p.readValueAsTree();
            var fields = obj.fieldNames();

            while (fields.hasNext()) {
                String name = fields.next();
                var identifier = Identifier.valueOf(name);
                resultContext.putResult((ReportElement) (mapper.readValue(obj.get(name).toString(),
                        resultTypeMap.get(identifier))));
            }

            return resultContext;
        }
    }*/
}
