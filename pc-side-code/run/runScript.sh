STAT_FOR_PAPER=~/TakaTuka/pc-side-code/output/statForPaper
rm -rf ${STAT_FOR_PAPER}
mkdir ${STAT_FOR_PAPER}

ANT_PATH=~/TakaTuka/mote-side-code/trunk
TEMP_PATH=~/TakaTuka/mote-side-code/trunk/temp



PLATFORM=mica2

java -Xmx1200m -classpath ../dist/pc-side-code.jar takatuka.tukFormat.StartMeLF cldc-javax ../output/noBC/javax -nooptim -nomain -removeDR
echo -------------------------------------- cldc-io ------------------------------
java -Xmx1200m -classpath ../dist/pc-side-code.jar takatuka.tukFormat.StartMeLF cldc-io ../output/noBC/java-io -nooptim -nomain -removeDR
echo -------------------------------------- cldc-util ------------------------------
java -Xmx1200m -classpath ../dist/pc-side-code.jar takatuka.tukFormat.StartMeLF cldc-util ../output/noBC/java-util -nooptim -nomain -removeDR
echo -------------------------------------- cldc-lang ------------------------------
java -Xmx1200m -classpath ../dist/pc-side-code.jar takatuka.tukFormat.StartMeLF cldc-lang ../output/noBC/java-lang -nooptim -nomain -removeDR
echo -------------------------------------- sunSpotDemos ------------------------------
java -Xmx1200m -classpath ../dist/pc-side-code.jar takatuka.tukFormat.StartMeLF sunSpotDemos ../output/noBC/ssDemos -nooptim -nomain -removeDR

echo cd ${ANT_PATH}
cd ${ANT_PATH}
ant -f ant/build.xml pc-side-code

echo -------------------------------------- null program ------------------------------
ant -f ant/build.xml -Dmain-class=nullProgram.Main -DARCH=mica2 -Dargs='-nooptim'
cp ${TEMP_PATH}/stat* ${STAT_FOR_PAPER}/NPstat_${PLATFORM}_nooptim.properties
echo -------------------------------------- hello world ------------------------------
ant -f ant/build.xml -Dmain-class=helloWorld.Main -DARCH=${PLATFORM} -Dargs='-nooptim'
cp ${TEMP_PATH}/stat* ${STAT_FOR_PAPER}/HWstat_${PLATFORM}_nooptim.properties
echo -------------------------------------- Quicksort ------------------------------
ant -f ant/build.xml -Dmain-class=quicksort.Main -DARCH=${PLATFORM} -Dargs='-nooptim'
cp ${TEMP_PATH}/stat* ${STAT_FOR_PAPER}/QSstat_${PLATFORM}_nooptim.properties
echo -------------------------------------- GPS ------------------------------
ant -f ant/build.xml -Dmain-class=GPS.GPS_BGR -DARCH=${PLATFORM} -Dargs='-nooptim'
cp ${TEMP_PATH}/stat* ${STAT_FOR_PAPER}/gps_${PLATFORM}_nooptim.properties
echo -------------------------------------- Dymo ------------------------------
ant -f ant/build.xml -Dmain-class=routing.dymo.Application -DARCH=${PLATFORM} -Dargs='-nooptim'
cp ${TEMP_PATH}/stat* ${STAT_FOR_PAPER}/dymo_${PLATFORM}_nooptim.properties

