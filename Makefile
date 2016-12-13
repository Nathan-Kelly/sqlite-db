JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ClientStatistics.java \
	DatabaseReceiver.java \
	DatabaseTest.java \
	HeadScanner.java \
	IntervalList.java \
	table_operations.java \
	WindowedSequenceList.java \
	TestClient.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
