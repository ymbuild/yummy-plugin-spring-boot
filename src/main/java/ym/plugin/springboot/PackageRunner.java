package ym.plugin.springboot;

import ym.api.TaskContext;
import ym.api.Project;

import java.nio.file.*;
import java.util.*;

/**
 * 独立入口，由 ym 通过 JVM 子进程调用。
 * 接收系统属性，执行 Spring Boot JAR 打包。
 *
 * 用法:
 *   java -cp <plugin.jar>:<ym-api.jar> \
 *     -Dym.project.dir=/path/to/project \
 *     -Dym.classes.dir=/path/to/out/classes \
 *     -Dym.resources.dir=/path/to/src/main/resources \
 *     -Dym.runtime.classpath=/path/a.jar:/path/b.jar \
 *     -Dym.main.class=com.example.App \
 *     -Dym.project.name=my-app \
 *     -Dym.project.version=1.0.0 \
 *     ym.plugin.springboot.PackageRunner
 */
public class PackageRunner {

    public static void main(String[] args) {
        var projectDir = Path.of(prop("ym.project.dir", "."));
        var classesDir = Path.of(prop("ym.classes.dir", projectDir.resolve("out/classes").toString()));
        var resourcesDir = Path.of(prop("ym.resources.dir", projectDir.resolve("src/main/resources").toString()));
        var mainClass = prop("ym.main.class", null);
        var projectName = prop("ym.project.name", projectDir.getFileName().toString());
        var projectVersion = prop("ym.project.version", "0.0.0");
        var loaderVersion = prop("ym.loader.version", "4.0.3");

        if (mainClass == null) {
            System.err.println("[spring-boot] ERROR: -Dym.main.class is required");
            System.exit(1);
        }

        // 解析 runtime classpath
        var cpStr = prop("ym.runtime.classpath", "");
        var separator = cpStr.contains(";") ? ";" : ":";
        var runtimeJars = cpStr.isEmpty()
                ? List.<Path>of()
                : Arrays.stream(cpStr.split(separator)).map(Path::of).toList();

        // 配置
        var ext = new SpringBootExtension();
        ext.mainClass().set(mainClass);
        ext.loaderVersion().set(loaderVersion);

        // 构建 TaskContext
        var ctx = new SimpleTaskContext(projectDir, projectName, projectVersion, classesDir, resourcesDir, runtimeJars);

        // 执行打包
        var task = new SpringBootJarTask(ext);
        task.execute(ctx);
    }

    private static String prop(String key, String defaultValue) {
        var v = System.getProperty(key);
        return (v != null && !v.isEmpty()) ? v : defaultValue;
    }

    /**
     * 简化的 TaskContext 实现，用于独立运行。
     */
    private record SimpleTaskContext(
            Path projectDir, String name, String version,
            Path classesDir, Path resourcesDir, List<Path> runtimeClasspath
    ) implements TaskContext {
        @Override
        public Project project() {
            // 返回一个最小 Project 实现
            return new MinimalProject(projectDir, name, version, runtimeClasspath);
        }
    }

    private record MinimalProject(
            Path dir, String projectName, String projectVersion, List<Path> runtimeCp
    ) implements Project {
        @Override public String name() { return projectName; }
        @Override public String version() { return projectVersion; }
        @Override public Path projectDir() { return dir; }
        @Override public Path classesDir() { return dir.resolve("out/classes"); }
        @Override public Path resourcesDir() { return dir.resolve("src/main/resources"); }
        @Override public Map<String, Object> config() { return Map.of(); }
        @Override public String env(String name) { return System.getenv(name); }
        @Override public <T extends ym.api.YmPlugin> void plugin(Class<T> t, java.util.function.Consumer<T> c) {}
        @Override public boolean hasPlugin(Class<? extends ym.api.YmPlugin> t) { return false; }
        @Override public void afterPlugins(Runnable a) {}
        @Override public <T> T createExtension(String n, Class<T> t) { return null; }
        @Override public <T> T extension(String n, Class<T> t) { return null; }
        @Override public ym.api.Task task(String n, java.util.function.Consumer<ym.api.Task> c) { return null; }
        @Override public <T extends ym.api.Task> T task(String n, Class<T> t, java.util.function.Consumer<T> c) { return null; }
        @Override public ym.api.Task task(String n) { return null; }
        @Override public List<Path> runtimeClasspath() { return runtimeCp; }
        @Override public List<Path> compileClasspath() { return List.of(); }
    }
}
