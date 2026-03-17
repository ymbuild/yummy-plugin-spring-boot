package ym.plugin.springboot;

import ym.api.Property;

/**
 * Configuration for the Spring Boot packaging plugin.
 */
public class SpringBootExtension {

    private final Property<String> mainClass = Property.of(String.class);
    private final Property<String> loaderVersion = Property.of(String.class, "4.0.3");
    private final Property<Boolean> layers = Property.of(Boolean.class, true);

    public Property<String> mainClass() {
        return mainClass;
    }

    public Property<String> loaderVersion() {
        return loaderVersion;
    }

    public Property<Boolean> layers() {
        return layers;
    }
}
