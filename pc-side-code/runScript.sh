 #sudo update-java-alternatives -s java-6-sun

 echo -------------------------------------- cldc-javax ------------------------------
java -cp build/classes -Xmx1200m takatuka.tukFormat.StartMeLF input/javax output/1/cldc-javax
gedit output/1/cldc-javax/stat.properties & 

 echo -------------------------------------- cldc-io ------------------------------
java -cp build/classes -Xmx1200m takatuka.tukFormat.StartMeLF input/java/io output/1/cldc-io
gedit output/1/cldc-io/stat.properties &

 echo -------------------------------------- cldc-lang ------------------------------
java -cp build/classes -Xmx1200m takatuka.tukFormat.StartMeLF input/java/lang output/1/cldc-lang
gedit output/1/cldc-lang/stat.properties & 

 echo -------------------------------------- cldc-util ------------------------------
java -cp build/classes -Xmx1200m takatuka.tukFormat.StartMeLF input/java/util output/1/cldc-util
gedit output/1/cldc-util/stat.properties & 

 echo -------------------------------------- sunSpotDemos LF ------------------------------
java -cp build/classes -Xmx1200m takatuka.tukFormat.StartMeLF input/org output/1/sunSpotDemosLC
gedit output/1/sunSpotDemosLC/stat.properties &


