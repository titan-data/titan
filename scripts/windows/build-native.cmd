@echo off
set /p Version=<VERSION
call "C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\SetEnv.cmd" /Release
native-image -cp target\titan-%Version%-jar-with-dependencies.jar -H:Name=titan -H:Class=io.titandata.titan.Cli -H:+ReportUnsupportedElementsAtRuntime -H:ReflectionConfigurationFiles=config\reflect-config.json -H:ResourceConfigurationFiles=config\resource-config.json -H:JNIConfigurationFiles=config\jni-config.json -H:+AddAllCharsets --allow-incomplete-classpath --enable-http