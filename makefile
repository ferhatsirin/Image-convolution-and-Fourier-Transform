CWD:=$(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../)
all : main

main : ImageConvolutionTest

ImageConvolutionTest : ImageConvolution.java ImageConvolutionTest.java
	javac -cp .:$(CWD)/lib/* ImageConvolution.java ImageConvolutionTest.java

run :
	java -cp .:$(CWD)/lib/* ImageConvolutionTest

clean :
	rm *.class 

