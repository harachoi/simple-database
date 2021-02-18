package simpledb.buffer;

import simpledb.file.*;
import simpledb.log.LogMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
public class BufferMgrClock extends BufferMgr {
    private int bufferPointer = 0;

    /**
     * Creates a buffer manager having the specified number
     * of buffer slots.
     * This constructor depends on a {@link FileMgr} and
     * {@link simpledb.log.LogMgr LogMgr} object.
     * @param numbuffs the number of buffer slots to allocate
     */
    public BufferMgrClock(FileMgr fm, LogMgr lm, int numbuffs) {
        super(fm, lm, numbuffs);
    }

    protected Buffer chooseUnpinnedBuffer() {
        if (numAvailable == 0) {
            return null;
        }
        Buffer found = null;
        for (int i = 0; i < bufferpool.length * 2; i++) {
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
