import simpledb.buffer.BufferMgr;
import simpledb.server.SimpleDB;

public class CompareTime {
    public static void main(String[] args) {
        SimpleDB db = new SimpleDB("buffermgrtest", 400, 3); // only 3 buffers
    }
}
