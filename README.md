# CS 448 Project 1 Important Changes

## BufferMgr.java
- changed access modifiers of `private` member to `protected`
- added method 
    ```java 
    public int getNumBuffs()
    ```
    - returns the length of `bufferpool`
    
## SimpleDB.java
- added method
    ```java
    public BufferMgr setBufferMgr(String className)
    ```
    - set the buffer manager of the databse to the specified `className`
    - new buffer manager has to `extends BufferMgr`
    
## Buffer.java
- added variable
    ```java
    private int reference = 0;
    ```
    - used in `BufferMgrClock`
    
- added method
    ```java 
    public int getReference()
    ```
    - used in `BufferMgrClock`
    
- added method
    ```java
    public void resetReference()
    ```
    - used in `BufferMgrClock`