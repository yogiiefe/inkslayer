param([string]$JdkPath = 'C:\Program Files\Java\jdk-21.0.12.1')
$ErrorActionPreference = 'Stop'
$project = (Get-Location).Path
$java = Join-Path $JdkPath 'bin/java.exe'
if(-not (Test-Path -LiteralPath $java)){throw 'Supply -JdkPath pointing to a JDK 21 installation.'}
function Run-Gradle([string[]]$Arguments) {
    & $java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain -g .gradle-local @Arguments --console=plain
    if($LASTEXITCODE -ne 0){throw "Gradle failed: $Arguments"}
}
function Compile-Sources([string]$Source,[string]$Output,[string]$Classpath) {
    & $java tools/CompileOnManagedWindows.java $Source $Output $Classpath
    if($LASTEXITCODE -ne 0){throw "Compilation failed: $Source"}
}
Run-Gradle @('-I','tools/managed-dependencies.init.gradle','resolveManagedBuildDependencies')
$dependencies = @(Get-ChildItem -LiteralPath '.gradle-local/caches/modules-2/files-2.1' -Recurse -Filter '*.jar' | ForEach-Object FullName)
$classpath = $dependencies -join ';'
Compile-Sources 'core/src/main/java' 'core/build/classes/java/main' $classpath
Run-Gradle @(':core:jar','-x',':core:compileJava')
$coreJar = Get-ChildItem -LiteralPath 'core/build/libs' -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName
$classpath = $classpath + ';' + $coreJar
Compile-Sources 'lwjgl3/src/main/java' 'lwjgl3/build/classes/java/main' $classpath
Compile-Sources 'core/src/test/java' 'core/build/classes/java/test' $classpath
Run-Gradle @(':core:test',':lwjgl3:jar','-x',':core:compileJava','-x',':core:compileTestJava','-x',':lwjgl3:compileJava')
Write-Output 'PASS: all sources compiled, JUnit tests executed, desktop JAR packaged.'
