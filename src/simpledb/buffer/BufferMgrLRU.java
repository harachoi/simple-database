  
package simpledb.buffer;

import simpledb.file.BlockId;
import simpledb.file.FileMgr;
import simpledb.log.LogMgr;
import java.util.HashSet;

public class BufferMgrLRU extends BufferMgr {
    HashSet<Buffer> unpinnedBuffers;

    public BufferMgrLRU (FileMgr fm, LogMgr lm, int numbuffs) {
        super(fm, lm, numbuffs);

        this.unpinnedBuffers = new HashSet<>();
        for (int i = 0; i < bufferpool.length; i++) {
            unpinnedBuffers.add(bufferpool[i]);
        }
    }

    @Override
    protected Buffer tryToPin(BlockId blk) {
        Buffer buff = findExistingBuffer(blk);
        if (buff == null) {
            buff = chooseUnpinnedBuffer();
            if (buff == null)
                return null;
            buff.assignToBlock(blk);
        }
        if (!buff.isPinned())
            numAvailable--;
        buff.pin();
        return buff;
    }

    @Override
    protected Buffer findExistingBuffer(BlockId blk) {
        for (Buffer buff : bufferpool) {
            BlockId b = buff.block();
            if (b != null && b.equals(blk)) {
                unpinnedBuffers.remove(buff);
                return buff;
            }
        }
        return null;
    }

    @Override
    protected Buffer chooseUnpinnedBuffer() {
        if (unpinnedBuffers.size() == 0) {
            return null;
        }
        Buffer buffer = (Buffer) unpinnedBuffers.toArray()[0];
        unpinnedBuffers.remove(buffer);
        return buffer;
    }

    @Override
    public synchronized void unpin(Buffer buff) {
        buff.unpin();
        if (!buff.isPinned()) {
            numAvailable++;
            unpinnedBuffers.add(buff);
            notifyAll();
        }
    }
}