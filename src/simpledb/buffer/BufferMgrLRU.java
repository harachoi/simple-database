package simpledb.buffer;


import simpledb.file.*;
import simpledb.log.LogMgr;

public class BufferMgrLRU extends BufferMgr {

    
    private int bufferPointer = 0;

    public BufferMgrLRU(FileMgr fm, LogMgr lm, int numbuffs) {
        super(fm, lm, numbuffs);
    }

    protected Buffer chooseUnpinnedBuffer() {
        if (numAvailable == 0) {
            return null;
        }
        Buffer found = null;
        while (found == null) {
            Buffer buf = bufferpool[bufferPointer];
            if (!buf.isPinned()) {
                if (buf.getReference() == 0) {
                    found = buf;
                }
                buf.resetReference();
            }
            bufferPointer = (bufferPointer + 1) % bufferpool.length;
        }
        return found;
    }

    
}