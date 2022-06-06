package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    private File file;
    private TupleDesc td;


    class MyIterator implements DbFileIterator {
        TransactionId tid;
        Permissions permissions;
        BufferPool bufferPool = Database.getBufferPool();
        Iterator<Tuple> iterator;
        int num = 0;

        public MyIterator(TransactionId id, Permissions permissions) {
            this.tid = id;
            this.permissions = permissions;
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            num = 0;
            HeapPageId heapPageId = new HeapPageId(getId(), num);
            HeapPage page = (HeapPage) bufferPool.getPage(tid, heapPageId, permissions);
            if (page == null) {
                throw new DbException("null");
            } else {
                iterator = page.iterator();
            }

        }

        public boolean nextPage() throws TransactionAbortedException, DbException {
            while (true) {
                num = num + 1;
                if (num >= numPages()) {
                    return false;
                }
                // 跳过空的槽
                HeapPageId heapPageId = new HeapPageId(getId(), num);
                HeapPage page = (HeapPage) bufferPool.getPage(tid, heapPageId, permissions);
                if (page == null) {
                    continue;
                }
                iterator = page.iterator();
                if (iterator.hasNext()) {
                    return true;
                }
            }
        }
        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if (iterator == null) {
                return false;
            }
            if (iterator.hasNext()) {
                return true;
            } else {
                return nextPage();
            }
        }

        @Override
        public Tuple next()
            throws DbException, TransactionAbortedException, NoSuchElementException {
            if (iterator == null) {
                throw new NoSuchElementException();
            }
            return iterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            open();
        }

        @Override
        public void close() {
            iterator = null;
        }
    }
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return this.file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        FileInputStream fileInputStream = null;
        HeapPage heapPage= null;
        int size = BufferPool.getPageSize();
        byte[] buf = new byte[size];
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
            randomAccessFile.seek((long)pid.getPageNumber()*size);
            if(randomAccessFile.read(buf)==-1){
                return null;
            }
            heapPage = new HeapPage((HeapPageId) pid, buf);
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return heapPage;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
        byte[] pageData = page.getPageData();
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek((long)page.getId().getPageNumber()*BufferPool.getPageSize());
            randomAccessFile.write(pageData);
            randomAccessFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int) file.length() / BufferPool.getPageSize();
        // some code goes here
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        ArrayList<Page> dirtyPages = new ArrayList<>();
        for(int i=0;i<numPages();i++){
            PageId pid = new HeapPageId(getId(), i);
            HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
            if(page.getNumEmptySlots()==0)continue;
            page.insertTuple(t);
            dirtyPages.add(page);
            return dirtyPages;
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, true));
        byte[] emptyPageData = HeapPage.createEmptyPageData();
        bufferedOutputStream.write(emptyPageData);
        bufferedOutputStream.close();
        HeapPageId heapPageId = new HeapPageId(getId(), numPages() - 1);
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, heapPageId, Permissions.READ_WRITE);
        page.insertTuple(t);
        dirtyPages.add(page);
        return dirtyPages;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        HeapPage page = (HeapPage) Database.getBufferPool()
            .getPage(tid, t.getRecordId().getPageId(), Permissions.READ_WRITE);
        page.deleteTuple(t);
        final ArrayList<Page> pages = new ArrayList<>();
        pages.add(page);
        return pages;
        // some code goes here
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new MyIterator(tid, Permissions.READ_ONLY);
    }

}

