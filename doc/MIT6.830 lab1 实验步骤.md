# MIT6.830 lab1 实验步骤

## Exercise1		

### Illustrate

**Implement the skeleton methods in:**

------

- src/java/simpledb/storage/TupleDesc.java
- src/java/simpledb/storage/Tuple.java

------

At this point, your code should pass the unit tests TupleTest and TupleDescTest. At this point, modifyRecordId() should fail because you havn't implemented it yet.

### 任务描述

本实验需要实现两个类

- src/java/simpledb/storage/TupleDesc.java
- src/java/simpledb/storage/Tuple.java

并且需要通过两个测试`TupleTest`以及`TupleDescTest`。

### 相关知识

#### 知识点一：Tuple

主要包含三个属性

+ `TupleDesc td`：为该元组的描述信息，在`simbledb`中，每一个元组可以看做一条记录，有多个字段，每个字段的属性通过`TupleDesc`来存储，值通过`Field`来获取，`Field`表示一条记录中的每一个单元，每条记录都有一个对应的`RecoredId`来记录。
+ `RecordId rid`：该记录的Id
+ `Private Field[]`：该元组的`Field List`;

#### 知识点二：TupleDesc

`TupleDesc`通过`TDItem`来表示一个字段的描述信息，其中包含`String fieldName`字段名称和`Type fieldType`字段类型，其中`Type`是`simbledb`中的类型枚举类，类型的详细描述信息都在其中。各种方法的需求可通过注释进行完善方法。

### 编程要求

利用java语言完善相应代码中的内容

## Exercise2

[TOC]

### 任务描述

第二关主要实现`Catalog.java`类，该类用于进行表的管理，通过对应属性找到该表，你需要在该类中实现描述一个表的类，以下`Mytable`类作为一个例子提供参考:
```java
public class Mytable{
    DbFile file;// 存储该表的文件
    String name;// 表名称
    String pkeyField;// 表主键
    public Mytable(DbFile file, String name, String pkeyField) {
        this.file = file;
        this.name = name;
        this.pkeyField = pkeyField;
    }
    public DbFile getFile() {
        return file;
    }

    public void setFile(DbFile file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkeyField() {
        return pkeyField;
    }

    public void setPkeyField(String pkeyField) {
        this.pkeyField = pkeyField;
    }

    @Override
    public String toString() {
        return "Mytable{" +
            "file=" + file +
            ", name='" + name + '\'' +
            ", pkeyField='" + pkeyField + '\'' +
            '}';
    }
}
```
本关需要补充的文件为：
src/java/simpledb/common/Catalog.java

并且需要通过单元测试`CatalogTest`

### 相关知识

#### 知识点一：Dbfile

每个表都应该有一个`TupleDesc`属性来表述该表的元组类型，在该类中，可通过`dbFile.getTupleDesc()`类来获取`TupleDesc`

#### 知识点二：Catalog

一个Database有多个Catalog，一个Catalog有多个schema，一个schema有多张表。在SimpleDB中并没有多个Catalog的概念，你只需要关心Catalog是用于管理table的，表述数据库中多张table，你需要在Catalog类中选择合适的数据结构来存放Table，如`Map`等。

### 编程要求
你需要完成 Catalog 文件中的构造函数 `Catalog()` 以及类中的 `get`,`set` 方法。
除此之外，你还需外完成`addTable`函数，用于添加一张table，`getDatabaseFile(int tableid)`根据`tableId`来获取对应的`DbFile`,`tableIdIterator`用于返回存储table的迭代器。

利用 java 语言完善相应代码中的内容

### 测试说明
本实验会根据你所编写的代码进行相应的单元测试。

## Exercise3

### 任务描述

第三关首先需要实现`BufferPool.java`类中的`getPage()`方法。

本关需要补充的文件为：

- src/java/simpledb/storage/BufferPool.java

本关没有为 BufferPool 提供单元测试。您实现的功能将在下面的 HeapFile 实现中进行测试，您应该使用 DbFile.readPage 方法来访问 DbFile 的页面。

### 相关知识

#### 知识点一：缓冲池

为了实现更加快速的磁盘访问， 在内存空间中创建一块缓冲池，用于将近期读写较为频繁的页面留在内存中，用以加快访问速度。

缓冲池（SimpleDB 中的 `BufferPool` 类）负责在内存中缓存最近从磁盘读取的页面。所有操作员都通过缓冲池从磁盘上的各种文件中读取和写入页面。它由固定数量的页面组成，由 `BufferPool` 构造函数的 `numPages` 参数定义。在后面的实验中，您将实施驱逐策略。对于本实验，您只需要实现 SeqScan 操作符使用的构造函数和 `BufferPool.getPage()` 方法。 BufferPool 最多可以存储 numPages 个页面。对于本实验，如果对不同页面发出的请求超过 `numPages`，则可能会抛出 DbException，而不是实施淘汰策略。在以后的实验中，您将需要实施淘汰策略。

### 编程要求

你需要完成`BufferPool.java`类中的`getPage()`方法。

### 测试说明

本关没有测试。

## Exercise4



### 任务描述

第三关首先需要实现`HeapPageId`,`RecordId`,`HeapPage`类

本关需要补充的文件为：

- src/java/simpledb/storage/HeapPageId.java
- src/java/simpledb/storage/RecordId.java
- src/java/simpledb/storage/HeapPage.java

您的代码应该通过 HeapPageIdTest、RecordIDTest 和 HeapPageReadTest 中的单元测试。

实现 HeapPage 后，您将在本实验中为 HeapFile 编写方法来计算文件中的页面数并从文件中读取页面。然后，您将能够从存储在磁盘上的文件中获取元组。

### 相关知识

#### 知识点一：堆访问方法

访问方法提供了一种从以特定方式排列的磁盘读取或写入数据的方法。常见的访问方法包括堆文件（未排序的元组文件）和 B-tree；

对于这个实验，您将只实现一个堆文件访问方法，我们已经为您编写了一些代码。 一个 HeapFile 对象被排列成一组页面，每个页面由固定数量的字节组成，用于存储元组（由常量 BufferPool.DEFAULT_PAGE_SIZE 定义），包括一个标题。在 SimpleDB 中，数据库中的每个表都有一个 HeapFile 对象。 HeapFile 中的每个页面都被安排为一组槽，每个槽可以容纳一个元组（SimpleDB 中给定表的元组都具有相同的大小）。除了这些槽之外，每个页面都有一个标题，该标题由一个位图组成，每个元组槽一个位。如果某个元组对应的位为1，则表示该元组有效；如果为 0，则元组无效（例如，已被删除或从未初始化。） HeapFile 对象的页面属于实现 Page 接口的 HeapPage 类型。页面存储在缓冲池中，但由 HeapFile 类读取和写入。 

#### 知识点二：每页元组计算方法

SimpleDB 将堆文件存储在磁盘上的格式与它们存储在内存中的格式大致相同。每个文件由磁盘上连续排列的页面数据组成。每个页面由一个或多个表示标题的字节组成，后面是实际页面内容的_page size_字节。每个元组的内容需要元组大小 * 8 位，标题需要 1 位。因此，可以容纳在单个页面中的元组数为： 

+ 每页元组_ = floor((_page size_ * 8) / (_tuple size_ * 8 + 1)) 

其中 tuple size 是页面中元组的大小（以字节为单位）。这里的想法是每个元组都需要在标头中额外存储一位。我们计算页面中的位数（通过将页面大小乘以 8），然后将该数量除以元组中的位数（包括这个额外的标题位）以获得每页的元组数。

 floor 操作向下舍入到最接近的元组整数（我们不想在页面上存储部分元组！） 一旦我们知道每页的元组数，存储标头所需的字节数很简单： 

+ headerBytes = ceil（tupsPerPage/8） 

ceil操作向上舍入到最接近的整数字节数（我们从不存储少于一个完整字节的标头信息。） 每个字节的低（最低有效）位表示文件中较早的插槽的状态。因此，第一个字节的最低位表示页面中的第一个槽是否在使用中。第一个字节的第二低位表示页面中的第二个槽是否正在使用，依此类推。另外，请注意最后一个字节的高位可能与文件中实际存在的槽不对应，因为槽的数量可能不是 8 的倍数。另请注意，所有 Java 虚拟机都是大端存储的.

### 编程要求

+ HeapPageId

  你需要实现该类的构造函数以及`get`,`set`函数

+ HeapPage

  `HeapPage(HeapPageId id, byte[] data)`构造函数，参数为每页的唯一Id以及数据构成

  `getNumTuples()`获取该页中的元组数量

  `getHeaderSize()`获取该页中的头部大小

  `getId`获取该页的Id

  `readNextTuple()`读取下一个元组，该函数用于读取文件中的下一个元组，编写代码时需要注意跳过空的槽，因为存储不是连续存储。

  `isSlotUsed`判断该槽是否被使用过，通过头部的存储信息来判断。

  `iterator`返回一个存储该页Tuple的迭代器，可以存储在某个数据结构中再返回。

  `insertTuple`与`deleteTuple`以及`markDirty`，`isDirty`无需在本实验中实现。

+ RecordId

  你需要实现该类的构造函数以及`get`,`set`函数

### 测试说明

您的代码应该通过 HeapPageIdTest、RecordIDTest 和 HeapPageReadTest 中的单元测试。

## Exercise5

### 任务描述

第三关首先需要实现`HeapFile`类

本关需要补充的文件为：

- src/java/simpledb/storage/HeapFile.java	

您的代码应该通过 HeapFileReadTest 中的单元测试。

### 相关知识

#### 知识点一：读取磁盘

要从磁盘读取页面，您首先需要计算文件中的正确偏移量。提示：您需要随机访问文件才能以任意偏移量读取和写入页面。从磁盘读取页面时不应调用 BufferPool 方法。

#### 知识点二：迭代器方法

您还需要实现 `HeapFile.iterator()` 方法，该方法应该遍历 HeapFile 中每个页面的元组。迭代器必须使用 `BufferPool.getPage()` 方法来访问 `HeapFile` 中的页面。此方法将页面加载到缓冲池中，最终将用于（在以后的实验中）实现基于锁定的并发控制和恢复。不要在 open() 调用时将整个表加载到内存中——这将导致非常大的表出现内存不足错误。

### 编程要求

+ `readPage(PageId pid)`函数

  该方法根据PageId去磁盘中读取数据，其中难点在于计算出该页在文件中的偏移量然后进行读取该页，计算出offset后使用seek移动指针然后开始读取内容，存储在字符数组中来构建一个`Page`类并返回。

+ `public DbFileIterator iterator(TransactionId tid)`

  该方法返回一个该文件的所有元组的迭代器，此处的实现应该从`BufferPool`中读取页面，如果直接调用`readPage`会造成OOM的错误。

## Exercise6

### 任务描述

第六关首先需要实现`SeqScan`类

本关需要补充的文件为：

- ------

  - src/java/simpledb/execution/SeqScan.java

  ------

该操作符从构造函数中的 tableid 指定的表的页面中顺序扫描所有元组。此运算符应通过 DbFile.iterator() 方法访问元组。 

您需要通过 ScanTest 系统测试。

### 相关知识

#### 知识点一： 操作符

operators负责查询计划的实际执行。它们实现了关系代数的运算。在 SimpleDB 中，操作符是基于迭代器的；每个运算符都实现 DbIterator 接口。 通过将较低级别的运算符传递给较高级别运算符的构造函数，即通过“将它们链接在一起”，将运算符连接在一起形成一个计划。

plan叶子节点中的特殊访问方法运算符负责从磁盘读取数据（因此在它们下面没有任何运算符）。 在plan的顶部，与 SimpleDB 交互的程序只需在根运算符上调用 getNext；然后这个操作符在它的子节点上调用 getNext，以此类推，直到这些叶子操作符被调用。它们从磁盘中获取元组并将它们向上传递（作为 getNext 的返回参数）；元组以这种方式向上传播plan，直到它们在根处输出或被plan中的另一个运算符组合或拒绝。 对于本实验，您只需实现一个 SimpleDB 运算符。

### 编程要求

+ SeqScan

  你需要实现该类的构造函数以及`getter,setter`函数。

  `getTupleDesc()`需要取修改每个字段的名称如：将`Fieldname`修改为`alias.FieldName`

### 测试说明

您需要通过 ScanTest 系统测试。