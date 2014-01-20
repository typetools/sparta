#!/bin/bash
#Delete file filters if exists
rm filters
#For every file in apks/
#java -jar APKParser.jar ./apks/Gallery.apk > AndroidManifest.xml #Generates AndroidManifest.xml from App.apk.
#filters-table.jar = Runnable jar with main class ProcessAndroidManifest.java
java -jar filters-table.jar #Generates mapping between intent filters and components. Filename: filters
#end loop
#gen-component-map.jar = Runnable jar with main class ProcessEpicOutput.java
java -jar gen-component-map.jar #Generates component map. Filename: ComponentMap
