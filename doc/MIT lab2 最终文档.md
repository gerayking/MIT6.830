# MIT lab2 最终文档

## Exercise1

### 任务描述

第六关首先需要实现`Predicate`,`JoinPredicate`,`Filter.java`,`Join`类

本关需要补充的文件为：

- ------

  - src/java/simpledb/execution/Predicate.java
- src/java/simpledb/execution/JoinPredicate.java
- src/java/simpledb/execution/Filter.java
  - src/java/simpledb/execution/Join.java

------

该操作符从构造函数中的 tableid 指定的表的页面中顺序扫描所有元组。此运算符应通过 DbFile.iterator() 方法访问元组。 

您需要通过 ScanTest 系统测试。

### 相关知识

#### 知识点一： *Filter*

此运算符仅返回满足在其构造函数中指定的 Predicate 的元组。因此，它过滤掉任何与谓词不匹配的元组。

#### 知识点二：*Join*

此运算符根据作为其构造函数的一部分传入的 JoinPredicate 连接来自其两个子项的元组。我们只需要一个简单的嵌套循环连接，但您可以探索更有趣的连接实现。

### 编程要求

+ Predicate

  您仅需要完成完成构造函数以及`getter`与`setter`函数以及`filter(Tuple t)`函数，该方法判断该谓词是否满足操作符`op`与操作数`operand`之间的关系。

+ JoinPredict

  您仅需要完成完成构造函数以及`getter`与`setter`函数以及`filter(Tuple t)`函数，该方法判断元组`t1`与`t2`是否满足操作符`op`之间的关系，可以通过调用`Field.compare()`函数来实现。

+ Filter

  您需要实现该函数的构造函数以及其余迭代器方法，如`open()`该函数将获取数据的前置条件进行处理，将`child`中符合条件的过滤出来。`close`关闭该迭代器，`rewind()`重置该迭代器,`fetchNext()`为获取数据的关键函数，在`SimpleDB`中，迭代器中的`next`以及`hasNext`都通过`fetchNext()`实现，该函数需要返回下一个满足过滤条件的元组。

+ Join

  该类继承了`Operator`,同样需要实现迭代器方法，其中 `open`函数需要做好数据预处理，实现`child1``child2`通过谓词`p`来连接所得到的`tuple`。

### 测试说明

您需要通过 PredicateTest,	JoinPredicateTest,FilterTest,JoinTest 单元测试以及`FilterTest `,`JoinTest`系统测试。

## Exercise2

### 任务描述

第六关首先需要实现`IntegerAggregator`,`StringAggregator`,`Aggregate类

本关需要补充的文件为：

- ------

  - src/java/simpledb/execution/IntegerAggregator.java
- src/java/simpledb/execution/StringAggregator.java
- src/java/simpledb/execution/Aggregate.java

------

该操作符从构造函数中的 tableid 指定的表的页面中顺序扫描所有元组。此运算符应通过 DbFile.iterator() 方法访问元组。 

您需要通过 ScanTest 系统测试。

### 相关知识

#### 知识点一： group by 与 聚合函数

在数据库中会有group by 和聚合函数搭配使用，如在下表中

```
mysql> select * from access_log;
+-----+---------+-------+---------------------+
| aid | site_id | count | date                |
+-----+---------+-------+---------------------+
|   1 |       1 |   250 | 2022-05-14 00:00:00 |
|   2 |       3 |   100 | 2022-05-13 00:00:00 |
|   3 |       1 |   230 | 2022-05-14 00:00:00 |
|   4 |       2 |    10 | 2022-05-14 00:00:00 |
+-----+---------+-------+---------------------+
4 rows in set (0.00 sec)
```

输入查询site_i，sum并group by site_id 可以得到下面结果

```sql
SELECT site_id, SUM(access_log.count) AS nums
FROM access_log GROUP BY site_id;

mysql> SELECT site_id,SUM(access_log.count) AS nums FROM access_log GROUP BY site_id;
+---------+------+
| site_id | nums |
+---------+------+
|       1 |  480 |
|       3 |  100 |
|       2 |   10 |
+---------+------+
3 rows in set (0.00 sec)
```

该实验即实现group by功能以及聚合函数(`COUNT`, `SUM`, `AVG`, `MIN`,`MAX`)等功能知识点二：*Join*

此运算符根据作为其构造函数的一部分传入的 JoinPredicate 连接来自其两个子项的元组。我们只需要一个简单的嵌套循环连接，但您可以探索更有趣的连接实现。

### 编程要求

+ IntegerAggregator	

  该类负责int类型的聚合，其中要实现的方法主要有：

  + IntegerAggregator的构造方法

  ```java
  public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
      // some code goes here
  }
  ```

  其中各个字段含义为：

  `gbfield`： 利用tuple中的哪个字段进行分组，在上述例子中字段为`site_id`则gbfield = 1（若以aid分组则 gbfield = 0）。

  `gbfieldtype`: 该字段的类型在该类中为Type.INT_TYPE

  `afield`:aggregate field id， 也就是进行聚合的字段为tuple中的第几个， 上述例子中`afield = 2`

  `what`：聚合函数是什么，上述例子为`SUM`

  + mergeTupleIntoGroup，分组方法

  ```java
      /**
       * Merge a new tuple into the aggregate, grouping as indicated in the
       * constructor
       * 
       * @param tup
       *            the Tuple containing an aggregate field and a group-by field
       */
      public void mergeTupleIntoGroup(Tuple tup) {
      }
  ```

  该方法将tup进行分组，例如上述例子中根据`site_id`进行分组，每个tup对应一条记录，则需要对相等的`field`进行合并，存储在`Map<Field,ArrayList<Tuple>>`数据结构中。

  + iterator 方法， 返回该聚合的迭代器，聚合后返回的结果是`TupleIterator`类型，需要`TupleDesc`以及`ArrayList<Tuple>`,在例子中`TupleDesc`为`{site_id,nums}`,Tuple则有3个数据记录。

    ```shell
    +---------+------+
    | site_id | nums |
    +---------+------+
    |       1 |  480 |
    |       3 |  100 |
    |       2 |   10 |
    +---------+------+
    ```

    ```java
    public OpIterator iterator() {
            // some code goes here
    }
    ```

  

+ StringAggregator

  该聚合器同`IntegerAggregator`类似，但仅需实现`Count`聚合函数

+ Aggregate

  该类使用`IntegreAggregator`类与`StringAggregator`，继承于`Operator`，提供获取数据的迭代器接口, 该类将两个聚合器封装成迭代器供使用。

  + Aggregate构造函数方法

    ```java
    public Aggregate(OpIterator child, int afield, int gfield, Aggregator.Op aop) {
        // some code goes here
    }
    ```

    + `child`  数据的迭代器，能够获取到tuple
    + `afield`同上，为aggregate field id
    + `gfield`同上, 为`group by field id`
    + `aop`， 为聚合函数类型

  + Open()，打开当前迭代器，将`child`中的tuple加入聚合器后，再使用IntegreAggregator`或`StringAggregator`中的迭代器

  + fetchNext(), 获取下一个`tuple`, open获得迭代器后直接通过迭代器获取即可

  + rewind(), 不要忘记child的rewind()。

  + close(), 不要漏了child的close()。

### 测试说明

测试需要运行的测试文件为IntegerAggregatorTest, StringAggregatorTest, and AggregateTest，以及AggregateTest system test.

## Exercise3

### 任务描述

第六关首先需要完善`BufferPool`,`HeapPage`,`HeapFile`类

本关需要补充的文件为：

+ src/simpledb/BufferPool.java
  * insertTuple()
  * deleteTuple()

* src/java/simpledb/storage/HeapPage.java
* src/java/simpledb/storage/HeapFile.java<br>
  (Note that you do not necessarily need to implement writePage at this point).

------

要实现 HeapPage，您需要修改 insertTuple() 和 deleteTuple() 等方法。您可能会发现我们要求您在实验 1 中实现的 getNumEmptySlots() 和 isSlotUsed() 方法是有用。请注意，提供了一个 markSlotUsed 方法作为抽象来修改页眉中元组的填充或清除标记。

### 相关知识

#### 知识点一： 删除元组

要删除元组，您需要实现 deleteTuple。元组包含允许您找到它们所在的页面的 RecordID，因此这应该像定位元组所属的页面并适当地修改页面的标题一样简单。

#### 知识点二：添加元组

HeapFile.java 中的 insertTuple 方法负责将元组添加到Heapfile文件中。要将新元组添加到 HeapFile，您必须找到具有空槽的页面。如果 HeapFile 中不存在此类页面，则需要创建一个新页面并将其附加到磁盘上的物理文件中。您需要确保元组中的 RecordID 已正确更新。

### 编程要求

+ HeapPage

  需要实现四个函数，分别为`markSlotUsed`、`markDirty`,`isDirty`,`insertTuple`、`deleteTuple`

  + `markSlotUsed`

    在lab1中，将每一页氛围很多个槽，每个槽是否被占用采用的一个byte数组来维护，该函数需要对该数组进行维护

  + `markDirty`

    将该页标记为脏页并且记录下`transactionId`

  + `isDirty` 返回此页是否为脏页

  + `insertTuple`

    将`tuple`添加到该页中，主要的步骤:

    + 判断该页是否为空槽
    + 找到空的槽
    + 为当前插入tuple设置`RecoredId`后插入当前槽中
    + 标记该槽已使用

  + `deleteTuple`

    该函数负责删除`Tuple`，主要步骤为：

    + 遍历所有槽
    + 跳过空的
    + 对非空的进行判断是否相等

    + 相等则删除该槽的内容并且标志该槽未使用

+ HeapFile

  该类对应的是文件的读写，同样需要完成`insertTuple`和`deleteTuple`以及`WritePage`函数，接下来对每个函数进行逐一的讲解.

  + WritePage(Page page)

    对当前`file`写入新的一页，通过page获取pageId来获取PageNumber后得到当前文件的偏移量，后写入。写入方法通过java的`RandomAccessFile`类来进行写入。

  + insertTuple(TransactionId tid, Tuple t)

    插入一个`Tuple`，首先得判断当前的页面是否都是满的， 若都是满的则需要创建新的页面，在新的页面中进行写入。

  + deleteTuple(TransactionId tid, Tuple t)

    删除一个Tuple，通过`t.getRecoreId().getPageId()`来获取到存储该`Tuple`的页，调用`HeapPage`中的`deleteTuple`即可，返回一个脏页的`ArrayList<Page>`

+ BufferPool

  缓冲池，同样需要实现`insetTuple`和`deleteTuple`两个函数，该函数实现比较简单，只需要调用底层的`HeapFile`即可。

  + insertTuple(TransactionId tid, int tableId, Tuple t)

    能够通过`tableId`获取到`Dbfile`，只需要通过`Dbfile`中的`insertTuple`进行插入即可，同时还得把返回的脏页加入lab1中自己实现的cache中，表示为脏页

  + deleteTuple(TransactionId tid, Tuple t)

    实现如上`insertTuple`类似，此处不再赘述

### 测试说明

您需要通过 HeapPageWriteTest ,	HeapFileWriteTest,BufferPoolWriteTest测试。

## Exercise4

### 任务描述

第六关首先需要实现`Insert`,`Delete`,`Filter.java`,`Join`类

本关需要补充的文件为：

- ------

  - src/java/simpledb/execution/Insert.java
- src/java/simpledb/execution/Delete.java

------

该实验需要实现数据库的`Insert`和`Delete`，在`exercise3`中实现的是缓冲池->文件->页层次的插入tuple和删除tuple，该实验则将其包装成提供给用户的接口。

### 相关知识

#### 知识点一： *Insert*

此运算符将从其子运算符读取的元组添加到其构造函数中指定的 tableid。它应该使用 BufferPool.insertTuple() 方法来执行此操作。

#### 知识点二：Delete

此运算符从其构造函数中指定的 tableid 中删除它从其子运算符读取的元组。它应该使用 BufferPool.deleteTuple() 方法来执行此操作。

### 编程要求

+ Insert

  Simpledb 的迭代器中的`hasNext()`和`next()`都是通过`fetchNext()`来实现，因此我们只需要实现`fetchNext()`即可。

  ```java
  public boolean hasNext() throws DbException, TransactionAbortedException {
      if (!this.open)
          throw new IllegalStateException("Operator not yet open");
      if (next == null)
          next = fetchNext();
      return next != null;
  }
  
  public Tuple next() throws DbException, TransactionAbortedException,
  NoSuchElementException {
      if (next == null) {
          next = fetchNext();
          if (next == null)
              throw new NoSuchElementException();
      }
  
      Tuple result = next;
      next = null;
      return result;
  }
  ```

  `OpIterator child`:从中读取要插入的元组的子运算符,从中获取要插入的元组。

  `tableId`： 插入元组的表的`Id`

  `TransactionId t`: 事务Id 

  该类需要实现以下方法：

  + fetchNext()

    需要将`child`中的元组插入到表中，可使用`Database.getBufferPool().insertTuple()`方法插入，然后返回的元组为受影响的行数目。

+ *Delete*

  与`insert`类似，不再赘述。

### 测试说明

您需要通过 InsertTest单元测试以及`InsertTest  `,`DeleteTest `系统测试。

## Exercise5

### 任务描述

需要完善实现`BufferPool`类。

填写flushPage()方法和额外的辅助方法来实现页面淘汰：

本关需要补充的文件为：

- ------

  - src/java/simpledb/storage/BufferPool.java

------

如果您没有在上面的 HeapFile.java 中实现 writePage()，那么您也需要在此处执行此操作。

最后，您还应该实现discardPage() 以从缓冲池中删除一个页面而不将其刷新到磁盘。我们不会在这个实验中测试discardPage()，但它对于以后的实验是必要的。 

此时，您的代码应该通过了 EvictionTest 系统测试。 因为我们不会检查任何特定的淘汰策略，所以这个测试通过创建一个 16 页的 BufferPool 来工作（注意：虽然 DEFAULT_PAGES 是 50，我们正在用更少的页面初始化 BufferPool！），扫描一个超过 16 页的文件，并查看 JVM 的内存使用量是否增加了 5 MB 以上。如果您没有正确实施驱逐策略，您将无法驱逐足够多的页面，并且会超出大小限制，从而导致测试失败。

### 相关知识

#### 知识点一： 页面淘汰策略

在实验中实现了`bufferPool`，该实验需要实现一个缓冲池淘汰算法，比较常见的有FIFO,LRU等创建的替换算法，具体实现可自己选择， 然后需要实现`bufferPool`中的`flushPage`方法，该方法将缓冲池中的页面也到物理文件中，即`Dbfile`中，涉及的文件有`bufferPool.java`

### 编程要求

+ flushPage

  实现该方法的途径之一是可通过自定义一个内部类实现，如LRUCache或者FIFOCache，存储键值对为{PageId:Page}的方式，以便进行换出的时候写入。

### 测试说明

您需要通过 `EvictionTest  `系统测试。