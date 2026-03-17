package ym.plugin.springboot;

import ym.api.*;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Spring Boot packaging plugin for ym.
 * Replaces flat fat JAR with Spring Boot executable JAR (BOOT-INF layout).
 */
public class SpringBootPlugin implements YmPlugin {

    private SpringBootExtension extension;

    @Override
    public void apply(Project project) {
        // 1. 注册配置扩展
        extension = project.createExtension("spring-boot", SpringBootExtension.class);

        // 2. 从 ym.json 的 main 字段读取默认主类
        var config = project.config();
        if (config.containsKey("main")) {
            extension.mainClass().convention((String) config.get("main"));
        }

        // 3. 注册 bootJar task
        project.task("bootJar", task -> {
            task.dependsOn("compileJava", "processResources");
            task.inputs().dir(project.classesDir());
            task.inputs().property("mainClass", extension.mainClass().get());
            task.inputs().property("loaderVersion", extension.loaderVersion().get());
            task.doLast(ctx -> {
                new SpringBootJarTask(extension).execute(ctx);
            });
        });

        // 4. 替换默认打包：package 依赖 bootJar
        project.task("package", task -> {
            task.dependsOn("bootJar");
        });
    }

    public SpringBootExtension getExtension() {
        return extension;
    }

    // 便捷配置方法，供 ym.config.java 使用
    public void mainClass(String mainClass) {
        extension.mainClass().set(mainClass);
    }

    public void loaderVersion(String version) {
        extension.loaderVersion().set(version);
    }

    public void layers(boolean enabled) {
        extension.layers().set(enabled);
    }
}
