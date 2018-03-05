#!/bin/bash
# Execute this script to build the strategy and create a valid JAR executable.
#
# Usage: ./build_script.sh
# -----------------------------------------------------------------------------
echo "Building aux-exp..."

#É importante verficiar antes se a pasta do repositorio local ja não esta preenchida...para nao precisar fazer isso novamente.
mvn deploy:deploy-file -Dfile=lib/mujava.jar -DgroupId=mujava -DartifactId=mujava -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/plume.jar -DgroupId=plume -DartifactId=plume -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/openjava.jar -DgroupId=openjava -DartifactId=openjava -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/tools.jar -DgroupId=com.sun -DartifactId=tools -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn deploy:deploy-file -Dfile=lib/jdollyIf.jar -DgroupId=jdolly -DartifactId=jdolly -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

#Verificar se ja nao existe um nimrod.jar na pasta lib/ e perguntar ao usuario se deseja substituir.
#echo "Downloading Nimrod..."
#wget -P lib/ "https://onedrive.live.com/download?cid=3E9EEBEE0AA4D1F5&resid=3E9EEBEE0AA4D1F5%2149435&authkey=APD2AuoVOL0IUw8" -q --show-progress
#mv "lib/download?cid=3E9EEBEE0AA4D1F5&resid=3E9EEBEE0AA4D1F5%2149435&authkey=APD2AuoVOL0IUw8" "lib/nimrod.jar"

mvn deploy:deploy-file -Dfile=lib/nimrod.jar -DgroupId=nimrod -DartifactId=nimrod -Dversion=1.0 -Dpackaging=jar -Durl=file:./maven-repository/ -DrepositoryId=maven-repository -DupdateReleaseInfo=true

mvn clean

mvn package

#echo "JAR file successfully generated."
#echo "Execute the following command: java -jar target/aum-exp-1.0-SNAPSHOT-shaded.jar <local-dir>"


