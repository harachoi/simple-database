## To Run simple database:
- open and run CompareTime.java
- Gives three output files in project1out/tests
    - _testLoopRead-BufferMgrLRU.txt
    - _testPinSame-BufferMgrLRU.txt
    - _testRandom-BufferMgrLru.txt

## Result of te data in to graph
- Open graphing.py
- Set TESTNAME with the test you want to try out
    - testRandom
    - testPinSame
    - testLoopRead
- run the script via
  ```shell
  python graphing.py
  ```
  
# graphing.py
- python version 3
- required packages
  - `matplotlib`
  - `numpy`
  - `pandas`
