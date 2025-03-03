
all:
	gradlew build

data:
	rm -rf bin/main
	gradlew runData

push:
	./push.sh

clean:
	gradlew clean
