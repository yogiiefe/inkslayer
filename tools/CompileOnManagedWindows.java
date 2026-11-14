import javax.tools.ToolProvider;
import java.nio.file.*;
import java.util.*;

/** Optional build-host workaround; see docs/TESTING.md. Standard Gradle remains the normal build. */
class CompileOnManagedWindows {
    public static void main(String[] args) throws Exception {
        var sources = new ArrayList<String>();
        try(var paths=Files.walk(Path.of(args[0]))) { paths.filter(p->p.toString().endsWith(".java")).forEach(p->sources.add(p.toString())); }
        Files.createDirectories(Path.of(args[1]));
        var compiler=ToolProvider.getSystemJavaCompiler();
        var manager=compiler.getStandardFileManager(null,null,java.nio.charset.StandardCharsets.UTF_8);
        var units=manager.getJavaFileObjectsFromStrings(sources);
        boolean success=compiler.getTask(null,manager,null,List.of("-proc:none","-encoding","UTF-8","--release","21","-classpath",args[2],"-d",args[1]),null,units).call();
        // This managed host denies ZipFS.toRealPath on close despite allowing JAR reads.
        // Leave the file manager to process teardown; do not suppress compilation diagnostics.
        if(!success) System.exit(1);
        System.out.println("Compiled "+sources.size()+" sources into "+args[1]);
    }
}
