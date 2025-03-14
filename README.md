# cse150proj1

# Make sure java verion is correct
```export JAVA_HOME=$(/usr/libexec/java_home -v 11```

To run:
1. Go to whatever proj folder (proj0, proj1, proj2)
2. Compile using this:
```
make clean
make
```
4. Run this:
```java nachos.machine.Machine```

# Running Test Cases: 
1. Canvas contains the test cases for each function. 
2. Follow the instructions in that file & add the test case into the class
3. Go into nachos > threads > ThreadedKernel.java
4. Search for the selfTest() function, and add your test function to the end
5. To run Nachos: make sure /bin/nachos is in your PATH for the command to work
6. pwd : This finds the correct path when run within nachos CLI
7. export PATH=$PATH:/insertpath : insert the pwd location after the path.
```
pwd     
export PATH=$PATH:/insertpath      
```



