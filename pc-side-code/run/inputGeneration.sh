cp ../opcode*.prop* .
cp ../src/xml/*.xml .
cp ../*.txt .
mkdir cldc-lang
mkdir cldc-io
mkdir cldc-util
mkdir cldc-javax
mkdir sunSpotDemos
cp ../../publications/inputJars/*.jar .

#sunspot Demos
mv *Spot*.jar sunSpotDemos/
cd sunSpotDemos
jar xf AllSunSpotDemos.jar
cd ..

#cldc
jar xf cldc_classes.jar
mv java/io cldc-io
mv java/lang cldc-lang
mv java/util cldc-util
mv javax cldc-javax
