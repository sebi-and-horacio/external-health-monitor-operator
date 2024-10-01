# Step 1: **Set up the Project Using Quarkus CLI**

1. **Install Java SDK & Quarkus**  using [SDKman](https://sdkman.io/), (if you haven't already):

   ```bash
   curl -s "https://get.sdkman.io" | bash
   sdk install java 23-open
   sdk install quarkus
   ```

2. **Create a new Quarkus project** named `external-health-monitor-operator` using the Quarkus CLI:

   ```bash
   quarkus create app external-health-monitor-operator --java 21 --no-code
   ```

   This will create a new project with the following structure:

   ```
   external-health-monitor-operator/
   ├── src/
   ├── pom.xml
   └── ...
   ```

3. **Navigate to the project directory**:
   
   ```bash
   cd external-health-monitor-operator
   ```

4. **Add necessary extensions**:
   
   You'll need the `quarkus-operator-sdk` extension for building the operator with Quarkus. Run the following command to add it:
   
   ```bash
   quarkus ext add quarkus-operator-sdk
   ```

5. **Modify the `pom.xml`**:
   Quarkus should have already added the operator SDK dependencies, but you can double-check by opening `pom.xml` and ensuring it contains the following dependency for Java Operator SDK:

   ```xml
   <dependency>
       <groupId>io.quarkiverse.operatorsdk</groupId>
       <artifactId>quarkus-operator-sdk</artifactId>
   </dependency>
   ```

6. **Launch the Quarkus Dev Mode** to write your operator as it's running:

   ```bash
   quarkus dev
   ```

   This command should start the Quarkus development mode.
