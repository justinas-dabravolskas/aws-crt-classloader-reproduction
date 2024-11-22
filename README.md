# aws-crt-classloader-reproduction

This Gradle project simulates the `ClassNotFoundException` that occurs when running a CRT client within a Tomcat server,
where the `XmlInputFactory` is loaded by the Tomcat class loader instead of the default system class loader.

To run the project, update the bucket and credentials in the `build.gradle` file.