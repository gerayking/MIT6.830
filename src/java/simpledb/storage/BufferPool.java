package simpledb.storage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import simpledb.common.Database;
import simpledb.common.Permissions;
import simpledb.common.DbException;
import simpledb.common.DeadlockException;
import simpledb.transaction.LockManager;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;

import java.util.concurrent.ConcurrentHashMap;
import sun.misc.LRUCache;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 * 
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /** Bytes per page, including header. */

    private int numPages;
    private FIFOCache<PageId,Page> FIFOCache;
    class FIFOCache<K,V>{
        private int size;
        private int capacity;
        private Map<K,V>cache = new ConcurrentHashMap<>();
        private Queue<K> queue = new LinkedList<>();
        public FIFOCache(int numPages){
            this.capacity = numPages;
            this.size = 0;
        }
        public Set<K> keySet(){
            return cache.keySet();
        }
        public int getSize() {
            return size;
        }
        public int getCapacity() {
            return capacity;
        }
        public synchronized  void remove(K key){
            if(!cache.containsKey(key))return;
            cache.remove(key);
            Stack<K>  tmp = new Stack<>();
            while(queue.size() != 0){
                final K poll = queue.poll();
                if(poll.equals(key)){
                    break;
                }
                tmp.add(poll);
            }
            while (!tmp.isEmpty()){
                queue.add(tmp.pop());
            }
            size--;
        }
        public synchronized V get(K key){
            if(!cache.containsKey(key))return null;
            return cache.get(key);
        }
        public synchronized void put(K key,V value){
            if(cache.containsKey(key)){
                return ;
            }
            if(size+1>capacity){
                final K poll = queue.poll();
                cache.remove(poll);
            }
            queue.add(key);
            cache.put(key,value);
            size++;
        }
    }
    private LockManager lockManager;

    private static final int DEFAULT_PAGE_SIZE = 4096;

    private static int pageSize = DEFAULT_PAGE_SIZE;
    
    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        FIFOCache = new FIFOCache<>(numPages);
        this.numPages = numPages;
        lockManager = new LockManager();
        // some code goes here
    }
    
    public static int getPageSize() {
      return pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
    	BufferPool.pageSize = pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void resetPageSize() {
    	BufferPool.pageSize = DEFAULT_PAGE_SIZE;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, a page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public  Page getPage(TransactionId tid, PageId pid, Permissions perm)
        throws TransactionAbortedException, DbException {
        boolean lockAcquired = false;
        long start = System.currentTimeMillis();
        long timeout = new Random().nextInt(2000);
        while(!lockAcquired){
            long now = System.currentTimeMillis();
            if(now-start>timeout){
                throw new TransactionAbortedException();
            }
            lockAcquired = lockManager.acquireLock(tid, pid, perm);
        }
        if(FIFOCache.get(pid)==null){
            DbFile databaseFile = Database.getCatalog().getDatabaseFile(pid.getTableId());
            Page page = databaseFile.readPage(pid);
            if(FIFOCache.getSize()>=numPages){
                evictPage();
            }
            FIFOCache.put(pid,page);
            return page;
        }else{
            return FIFOCache.get(pid);
        }
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public  void unsafeReleasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary for lab1|lab2
        lockManager.releaseLock(tid,pid);
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) {
        // some code goes here
        // not necessary for lab1|lab2
        transactionComplete(tid,true);
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here
        // not necessary for lab1|lab2
        return lockManager.holdsLock(tid,p);
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(TransactionId tid, boolean commit) {
        // some code goes here
        // not necessary for lab1|lab2
        if(commit){
            try {
                flushPages(tid);
            }catch (IOException e){
                final Set<PageId> pageIds = FIFOCache.keySet();
                for (PageId pageId : pageIds) {
                    final Page page = FIFOCache.get(pageId);
                    if(lockManager.holdsLock(tid,pageId)&&page.isDirty()!=null){
                        final DbFile dbFile =
                            Database.getCatalog().getDatabaseFile(pageId.getTableId());
                        final Page page1 = dbFile.readPage(pageId);
                        FIFOCache.put(pageId,page1);
                    }
                }
                e.printStackTrace();
            }
        }else {
            //roll back
            final Set<PageId> pageIds = FIFOCache.keySet();
            for (PageId pageId : pageIds) {
                final Page page = FIFOCache.get(pageId);
                if(lockManager.holdsLock(tid,pageId)&&page.isDirty()!=null){
                    FIFOCache.remove(pageId);
                    try {
                        final Page newPage =
                            Database.getBufferPool().getPage(tid, pageId, Permissions.READ_ONLY);
                        FIFOCache.put(pageId,newPage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        lockManager.releaseAllLock(tid);
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other 
     * pages that are updated (Lock acquisition is not needed for lab2). 
     * May block if the lock(s) cannot be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile databaseFile = Database.getCatalog().getDatabaseFile(tableId);
        List<Page> pages = databaseFile.insertTuple(tid, t);
        for (Page page : pages) {
            page.markDirty(true,tid);
            FIFOCache.put(page.getId(),page);
        }
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction deleting the tuple.
     * @param t the tuple to delete
     */
    public  void deleteTuple(TransactionId tid, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile databaseFile = Database.getCatalog().getDatabaseFile(t.getRecordId().getPageId().getTableId());
        List<Page> pages = databaseFile.deleteTuple(tid, t);
        for (Page page : pages) {
            page.markDirty(true,tid);
            FIFOCache.put(page.getId(),page);
        }
    }

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // some code goes here
        // not necessary for lab1
        for (Page page : FIFOCache.cache.values()) {
            flushPage(page.getId());
        }

    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
        
        Also used by B+ tree files to ensure that deleted pages
        are removed from the cache so they can be reused safely
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // not necessary for lab1
        FIFOCache.remove(pid);
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized  void flushPage(PageId pid) throws IOException {
        // some code goes here
        // not necessary for lab1
        final Page page = FIFOCache.get(pid);
        final DbFile dbFile = Database.getCatalog().getDatabaseFile(pid.getTableId());
        dbFile.writePage(page);
        page.markDirty(false,null);
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        final Set<PageId> pageIds = FIFOCache.keySet();
        for (PageId pageId : pageIds) {
            if(lockManager.holdsLock(tid,pageId)){
                flushPage(pageId);
            }
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws DbException {
        // some code goes here
        // not necessary for lab1
        boolean flag = false;
        final Set<PageId> pageIds = FIFOCache.keySet();
        for (PageId pageId : pageIds) {
            final Page page = FIFOCache.get(pageId);
            if (page.isDirty()!=null){
                continue;
            }else{
                FIFOCache.remove(pageId);
                flag=true;
                break;
            }
        }
        if(!flag){
            throw new DbException("all are dirty pages");
        }
    }

}
