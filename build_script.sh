#!/bin/bash
# Execute this script to build the strategy and create a valid JAR executable.
#
# Usage: ./build_script.sh
# -----------------------------------------------------------------------------
echo "Building aux-exp..."

mvn deploy:deploy-file -Dfile=lib/mujava.jar -DgroupId=mujava -DartifactId=mujava -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/openjava.jar -DgroupId=openjava -DartifactId=openjava -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/tools.jar -DgroupId=com.sun -DartifactId=tools -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/nimrod.jar -DgroupId=nimrod -DartifactId=nimrod -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/jdollyIf.jar -DgroupId=jdolly -DartifactId=jdolly -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn clean

mvn package

echo "JAR file successfully generated."
echo "Execute the following command: java -jar target/aum-exp-1.0-SNAPSHOT-shaded.jar <local-dir>"

