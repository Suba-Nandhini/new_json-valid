import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.everit.json.schema.ValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Validator using Everit.
 */
public class JsonValidator {
    public static void main(String[] args) {
        try {
            // JSON schema file
            File schemaFile = new File("src/main/java/Json-schema-example.json");
            JSONTokener schemaData = new JSONTokener(new FileInputStream(schemaFile));
            JSONObject jsonSchema = new JSONObject(schemaData);

            // JSON data file
            File jsonData = new File("src/main/java/Json-schema-example-data.json");
            JSONTokener jsonDataFile = new JSONTokener(new FileInputStream(jsonData));
            JSONObject jsonObject = new JSONObject(jsonDataFile);

            // Map to store validation issues
            Map<String, List<String>> validationIssues = new HashMap<>();

            // Validate schema
            Schema schemaValidator = SchemaLoader.load(jsonSchema);
            try {
                schemaValidator.validate(jsonObject);  // Throws ValidationException if invalid
                System.out.println("JSON is valid.");
            } catch (ValidationException e) {
                System.out.println("JSON validation failed:");

                // Process validation errors
                List<ValidationException> causes = e.getCausingExceptions();
                for (ValidationException cause : causes) {
                    String keyword = cause.getKeyword();
                    String message = cause.getMessage();

                    if (keyword.equals("additionalProperties")) {
                        // Extract the extraneous keys
                        String extraneousKey = message.substring(message.indexOf("[") + 1, message.indexOf("]"));
                        validationIssues.computeIfAbsent("additionalProperties", k -> new ArrayList<>()).add(extraneousKey);
                    } else {
                        validationIssues.computeIfAbsent(keyword, k -> new ArrayList<>()).add(message);
                    }
                }

                // Simplified output
                validationIssues.forEach((key, value) -> {
                    if (key.equals("additionalProperties")) {
                        System.out.println("Missing: " + String.join(", ", value));
                    } else {
                        System.out.println(key + ":");
                        value.forEach(issue -> System.out.println("  - " + issue));
                    }
                });
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
