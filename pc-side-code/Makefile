JAVA_TUKMAKE=takatuka/loader/loadingFormat/StartMeLF

all: tukheader tukmake

CFLAGS=-Wall -O2
CC=gcc

PREFIX=/usr/local

           # implicit ;)
tukheader: tools/tukheader

clean:
	find src -iname "*.class" | xargs rm -f

tukmake: clean
	(cd src; javac -classpath ".:$(CLASSPATH)" $(JAVA_TUKMAKE).java)

install:
	echo "Main-Class: takatuka.loader.loadingFormat.StartMeLF" > src/MANIFEST.MF
	(cd src; jar cfm ../takatuka_loader.jar MANIFEST.MF $$(find . -iname "*.class"))

