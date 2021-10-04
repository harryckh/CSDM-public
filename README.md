# CSDM-public

Notes
=======================
    1. This code is developed by Harry Kai-Ho Chan (kai-ho@ruc.dk).
    2. This code is written in Java.
    3. The jar is built in Java 14.
    4. This code is tested on the macOS 10.15.7 environment.

Usage
=======================

Step 1: Generate the synthetic data (A sample dataset is provided, you need to run this step only if you want to generate your own synthetic data)

    java -jar dataGen.jar [totalLifecycle] [numOfObjPerFloor] [numOfFloors] [numOfObjs]

The details of the input parameters can be found in Appendix A.

The following files will be generated.
    Par.txt       = the list of partitions
    Door.txt      = the list of doors
    P2D.txt       = Par.txt
    D2P.txt       = Door.txt
    D2DMatrix.txt = the door to door distance matrix
    P2PMatrix.txt = the partition to partition door matrix
    objsSize[numOfObjs].txt = the number of objects in each time stamp
    Tracjectories[numOfObjs].txt = the location of the objects 

The sample dataset is generated by the following parameters.

    java -jar dataGen.jar 60 500 20 10000


Step 2: Run SDM

    java -jar exp.jar [statFilename] [numOfTrials] [totalLifecycle] [distribution] [numOfObjPerFloor] [numOfFloors] [numOfObjs] [epsilon] [dia] [batchProcess] [betaPrune] [T_p] [T_Min] [T_Max]

An example parameter setting is shown as follows.

    java -jar exp.jar example.txt 5 60 0 500 20 10000 3 2 1 1 10 5 20


The explanation of the input parameters can be found in Appendix B.

It will read the files generated from Step 1.
The monitoring result file "result[statFilename]" will be generated.
The statistic file [statFilename] will also be generated.

Step 3: Collect the monitoring results and running statistics 
[you can ignore this step if you don't want to collect the information of
monitoring results and running statistics]

The monitoring results output in "result[statFilename]"
which format is explained in Appendix C.

The running statistics are stored in [statFilename]
which format is explained in Appendix D.


Appendix A. Explanation of the input parameters in Step 1
============================
	[totalLifecycle] = total number of seconds 
	[numOfFloors] = total number of floors
	[numOfObjPerFloor] = total number of objects per floor
	[numOfObjs] = total number of objects 


Appendix B. Explanation of the input parameters in Step 2
============================

    [statFilename] = the name of the output stat file
    [numOfTrials] = number of trials to run and to be averaged
    [totalLifecycle] = total number of seconds 
    [distribution] = the distribution or the distance decay function used
        0 = Guassian Distribution
        1 = Constant Law
        2 = Linear Decay Law
        3 = Inverse 1st Power Law
        4 = Inverse 2nd Power Law
        5 = Exponential Decay Law
    [numOfFloors] = total number of floors
    [numOfObjPerFloor] = total number of objects per floor
    [numOfObjs] = total number of objects 
    [epsilon] = the distance threshold parameter
    [radius] = the radius parameter
    [batchProcess] = 1 = with batch processing; 0 = no
    [betaPrune] = 1 = with prbaboility-based pruning;0=no
    [T_p] = the T_{FP} parameter
    [T_Min] = the T_{Min} parameter
    [T_Max] = the T_{Max} parameter


Appendix C. The format of monitoring result
=============================
 
    t=0  
    #updateObjs: <Number of objects updated its location>
    #newObjs: <Number of new objects inserted>
    #expiredObjs: <Number of objects with tl expired in OIPT>
    Result:
    <t0> [o1/o2:dist1, o3/o4vdist2,...]
    <t1> [o4/o5:dist3]
    ...

    t=1
    #updateObjs: <Number of objects updated its location>
    #newObjs: <Number of new objects inserted>
    #expiredObjs: <Number of objects with tl expired in OIPT>
    Result:
    ...


where

    t0 is current time X,
    o1/o2:dist1 indicate a pair (o1,o2) is in contact at t0 with dist(o1,o2,t0) = dist1
    o3/o4vdist2 indicate a pair (o3,o4) is in contact at t0 by probability-based UB
    o3/o4udist2 indicate a pair (o3,o4) is in contact at t0 by pruning in batch processing

Appendix D. The format of statistic file
=============================
    <Average running time per update (ns)>
    <Average memory usage per update (KB)>
    <Average number of results per update>
    <Number of updates>

    <epsilon>
    <diameter>
    <batchProcess>
    <betaPrune>

    <totalLifecycle>

    <T_{FP}>
    <T_{Min}>
    <T_{Max}>
   
    <Number of floors>
    <Number of objects in each floor>
    <Total number of objects>

    <Number of trials>










