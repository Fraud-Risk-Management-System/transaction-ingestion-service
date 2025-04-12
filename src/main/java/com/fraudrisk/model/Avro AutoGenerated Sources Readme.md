# Working with Avro in the Transaction Ingestion Service

## Understanding Avro Code Generation

The Transaction Ingestion Service uses Apache Avro for data serialization. Avro provides several advantages:

1. Schema evolution support
2. Compact binary format
3. Language-agnostic serialization
4. Integration with the Confluent Schema Registry

Avro requires Java classes to be generated from schema definition files (`.avsc`). These generated classes are used for serialization/deserialization.

## Avro Generated Classes

The following classes are automatically generated from Avro schemas during the Maven build process:

- `com.fraudrisk.model.Transaction`
- `com.fraudrisk.model.Metadata`
- `com.fraudrisk.model.Location`
- `com.fraudrisk.model.AlertStatus`
- `com.fraudrisk.model.AlertSeverity`
- `com.fraudrisk.model.TransactionDecision`
- `com.fraudrisk.model.Alert`
- `com.fraudrisk.model.ProcessedTransaction`

These classes are created from the `.avsc` files in `src/main/resources/avro/` by the `avro-maven-plugin`.

## IDE Integration

If you're seeing "cannot resolve import" errors in your IDE:

1. Run `mvn generate-sources` to generate the classes
2. Refresh your IDE's Maven project
3. Make sure the `target/generated-sources` directory is marked as a sources root
4. If you're using IntelliJ IDEA, you might need to:
    - Go to File > Project Structure > Modules
    - Find the `target/generated-sources/avro` directory
    - Mark it as "Sources"

## Working with Generated Code

Remember these key points when working with Avro-generated classes:

1. **Never modify generated classes directly** - They'll be overwritten when you rebuild the project
2. **Don't commit generated classes to source control** - They should be regenerated during the build
3. **When defining new Avro schemas**:
    - Follow the [Avro Schema Evolution Rules](https://docs.confluent.io/platform/current/schema-registry/avro.html)
    - Always add new fields as optional with defaults
    - Never remove or rename fields

## Avro Schema Registry

In production, this service integrates with Confluent Schema Registry to manage schema versions. The registry:

1. Stores and validates schemas
2. Manages schema compatibility checks
3. Enables schema evolution
4. Reduces message size by sending schema IDs instead of full schemas

## Debugging Avro Issues

If you encounter serialization issues:

1. Make sure the schemas are compatible
2. Check the schema registry is running
3. Review the Avro-generated classes
4. Validate the data against the schema
5. Check for any conversion issues between your domain model and the Avro model

## Development Placeholders

During development, we've included placeholder classes to help with compilation. These will be overridden by the actual generated classes during the build process.