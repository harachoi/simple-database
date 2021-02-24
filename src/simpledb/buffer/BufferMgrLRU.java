package simpledb.buffer;

import java.util.*;

import simpledb.file.*;
import simpledb.log.LogMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
public class BufferMgrLRU extends BufferMgr {
    private int bufferPointer = 0;

    /**
     * Creates a buffer manager having the specified number
     * of buffer slots.
     * This constructor depends on a {@link FileMgr} and
     * {@link simpledb.log.LogMgr LogMgr} object.
     * @param numbuffs the number of buffer slots to allocate
     */
    public BufferMgrLRU(FileMgr fm, LogMgr lm, int numbuffs) {
        super(fm, lm, numbuffs);
    }

    protected Buffer chooseUnpinnedBuffer() {
      for (Buffer buff : bufferpool)
         if (!buff.isPinned())
            return buff;
      return null;
   }


    /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   public synchronized int available() {
    return numAvailable;
   }

   /**
   * Flushes the dirty buffers modified by the specified transaction.
   * @param txnum the transaction's id number
   */
   public synchronized void flushAll(int txnum) {
      for (Buffer buff : bufferpool)
         if (buff.modifyingTx() == txnum)
         buff.flush();
   }

   /**
   * Unpins the specified data buffer. If its pin count
   * goes to zero, then notify any waiting threads.
   * @param buff the buffer to be unpinned
   */
   public synchronized void unpin(Buffer buff) {
      //System.out.println();
      //System.out.println("UNPIN!!! " + buff.block());
      buff.unpin();
      if (pinnedMap.containsKey(buff.block())) {
         int index = pinnedMap.get(buff.block());
         //System.out.println("Check full size: " + pinnedList.size());
         //System.out.println("index::::::::::::::::::::::::::::::::::::::::::: " + index);
         pinnedMap.remove(buff.block());
         /*if (pinnedList.get(index) == buff) {
            pinnedList.remove(index);
            pinnedMap.remove(buff.block());
         }*/
      }
      if (pinnedSet.contains(buff.block())) {
         pinnedSet.remove(buff.block());
      }
      
      if (!buff.isPinned()) {
         numAvailable++;
         //unpinnedList.add(buff);
         notifyAll();
      }
   }
 
   /**
   * Pins a buffer to the specified block, potentially
   * waiting until a buffer becomes available.
   * If no buffer becomes available within a fixed 
   * time period, then a {@link BufferAbortException} is thrown.
   * @param blk a reference to a disk block
   * @return the buffer pinned to that block
   */
   public synchronized Buffer pin(BlockId blk) {
      try {

         //System.out.println();
         //System.out.println("STARTTING BLOCK::::::::::::::::: " + blk);
         long timestamp = System.currentTimeMillis();
         Buffer buff = tryToPin(blk);

         if (buff == null)
            throw new BufferAbortException();
         return buff;
      }
      catch(Exception e) {
         throw new BufferAbortException();
      }
   }  
 
   protected boolean waitingTooLong(long starttime) {
      return System.currentTimeMillis() - starttime > MAX_TIME;
   }
   
   /**
    * Tries to pin a buffer to the specified block. 
   * If there is already a buffer assigned to that block
   * then that buffer is used;  
   * otherwise, an unpinned buffer from the pool is chosen.
   * Returns a null value if there are no available buffers.
   * @param blk a reference to a disk block
   * @return the pinned buffer
   */


   protected Buffer tryToPin(BlockId blk) {
      Buffer buff = findExistingBuffer(blk);
      //Buffer buff = null;
      if (buff != null) {
      //if (pinnedSet.contains(blk)) {
         //System.out.println("???????????????????????????????????");
         //bufferpool contains the page
         //update least recently used value
         pinnedMap.remove(blk);
         pinnedMap.put(blk, index++);
         //return null;
         
         //System.out.println("seeeeeee " + buff.block());
      } else {
      //if (buff == null) { //no matching available page in bufferpool
      
         buff = chooseUnpinnedBuffer();
         if (buff == null) { //no space in bufferpool
            Iterator<BlockId> iterator = pinnedSet.iterator();
            int lru = Integer.MAX_VALUE;
            BlockId minBlock = null;
            while (iterator.hasNext()) {
               BlockId block = iterator.next();
               if (pinnedMap.get(block) < lru) {
                  lru = pinnedMap.get(block);
                  minBlock = block;
               }
            }
            for (int i = 0; i < bufferpool.length; i++) {
               if (bufferpool[i].block().equals(minBlock)) {
                  buff = bufferpool[i];
                  break;
               }
            }
            pinnedSet.remove(minBlock);
            pinnedMap.remove(minBlock);
            pinnedSet.add(blk);
            pinnedMap.put(blk, index++);
            /*
            for(int i = 0; i < pinnedList.size(); i++) {
               System.out.println("see pinnedlist: " + pinnedList.get(i).block());
            }
            */
            //return null;
         } else { //space available in bufferpool
            pinnedSet.add(blk);
            pinnedMap.put(blk, index++);
         }
         buff.assignToBlock(blk);
      }
      if (!buff.isPinned()) {
         numAvailable--;
      }
      buff.pin();
      

      return buff;   
   }
/*
 protected Buffer tryToPin(BlockId blk) {
   //Buffer buff = findExistingBuffer(blk);
   Buffer buff = null;
   if (pinnedSet.contains(blk)) {
      //bufferpool contains the page
      //update least recently used value
      pinnedMap.remove(blk);
      pinnedMap.put(blk, index++);
   } else {
   //if (buff == null) { //no matching page found
      System.out.println("NO MATCHNG PAGE FOUND");

      buff = chooseUnpinnedBuffer();
      System.out.println("check 2222222222222222222222 " + buff.block());
      for(int i = 0; i < pinnedList.size(); i++) {
         System.out.println("see pinnedlist: " + pinnedList.get(i).block());
      }


      if (buff == null) {
         System.out.println("Has space 11111111");
         if (pinnedList.size() > 0) {
            buff = pinnedList.getFirst();
            
            pinnedList.removeFirst();

            pinnedMap.remove(buff);
            //System.out.println("buff that is least recently used:       " + buff.block());
            return buff;
         }
         return null;
      } 
      buff.assignToBlock(blk);
   }
   if (!buff.isPinned()) {
      System.out.println("333333333333333333333333333333333333333333");
      numAvailable--;
   }

   buff.pin();
   if (pinnedList.contains(buff)) {
      if (pinnedMap.containsKey(buff)) {
         int index = pinnedMap.get(buff);
         pinnedList.remove(index);
         pinnedMap.remove(buff);
      }
   }
   if (!pinnedMap.containsKey(buff)) {
      pinnedList.add(buff);
      pinnedMap.put(buff, pinnedList.size() - 1);
   }
   return buff;
}
*/
 
 protected Buffer findExistingBuffer(BlockId blk) {
    if (pinnedMap.containsKey(blk)) {
       for (Buffer buff : bufferpool) {
         BlockId b = buff.block();
         if (b != null && b.equals(blk)) { 
            //the block(page) exist in the bufferpool
            //System.out.println("WHE>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + b);
            return buff;
        }
      }
    }
    return null;
    
    /*
    for (Buffer buff : bufferpool) {
       BlockId b = buff.block();
       if (b != null && b.equals(blk)) { 
          //the block(page) exist in the bufferpool
          System.out.println("WHE>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + b);
          return buff;
      }
    }
    return null;
    */
 }

 public int getNumBuffs() {
    return bufferpool.length;
 }
}
