# MIT lab4 文档

## Exercise1

### 任务描述

在 BufferPool 中编写获取和释放锁的方法。

假设您正在使用页面级锁定，您将需要完成以下操作： 

+ 修改 getPage() 以在返回页面之前阻止并获取所需的锁。 

+ 实现 unsafeReleasePage()。此方法主要用于测试和交易结束时。 
+ 实现 holdLock() 以便练习 2 中的逻辑可以确定页面是否已被事务锁定。

除此之外，你需要定义一个负责维护有关事务和锁的状态的 LockManager 类很有帮助，但设计决定取决于您。

第六关首先需要实现`Predicate`,`JoinPredicate`,`Filter.java`,`Join`类

本关需要补充的文件为：

- ------

  - src/java/simpledb/execution/BufferPool.java
  - src/java/simpledb/transaction/LockManager.java
  - src/java/simpledb/transaction/PageLock.java



### 相关知识

#### 知识点一： 共享锁和互斥锁

您将需要实现共享锁和排他锁；请记住

+ 在事务可以读取一个对象之前，它必须拥有一个共享锁。 
+ 在一个事务可以写一个对象之前，它必须有一个排他锁。 
+ 多个事务可以在一个对象上拥有一个共享锁。 
+ 只有一个事务可能对一个对象具有排他锁。
+  如果事务 t 是唯一持有对象 o 共享锁的事务，则 t 可以将其对 o 的锁升级为排他锁。

#### 知识点二：ACID

+ 原子性：严格的两阶段锁定和仔细的缓冲区管理确保原子性。 
+ 一致性：数据库凭借原子性是事务一致的。 SimpleDB 中没有解决其他一致性问题（例如，关键约束）。
+  隔离：严格的两相锁定提供隔离。
+  持久性：FORCE 缓冲区管理策略可确保持久性。

### 编程要求

+ PageLock

  您仅需要完成完成构造函数以及`getter`与`setter`函数。

+ LockManager

  + `public synchronized Boolean acquireLock(TransactionId tid, PageId pageId, Permissions permissions)`

    该函数请求锁，根据上述的共享锁和排他锁的原则来实现，实现原则如下：

    锁管理器中没有任何锁或者该页面没有被任何事务加锁，可以直接加读/写锁；

    如果t在页面有锁，分以下情况讨论：

    + 加的是读锁：直接加锁；

    + 加的是写锁：如果锁数量为1，进行锁升级；如果锁数量大于1，会死锁，抛异常中断事务；

    如果所再页面无锁，分以下情况讨论：

    + 加的是读锁：如果锁数量为1，这个锁是读锁则可以加，是写锁就wait；如果锁数量大于1，说明有很多读锁，直接加；

    + 加的是写锁：不管是多个读锁还是一个写锁，都不能加，wait

  + `public synchronized void releaseLock(TransactionId tid,PageId pageId)`

    该函数负责释放页面中所有tid事务的锁

  + `public synchronized void releaseAllLock(TransactionId transactionId)`

    该函数负责释放事务tid在所有页面中的锁

  + `public synchronized boolean holdsLock(TransactionId tid,PageId pid)`

    该函数判断页面pid是否持有事务tid的锁

+ BufferPool

  + `public  Page getPage(TransactionId tid, PageId pid, Permissions perm)`

    在该函数中获取页面时进行请求锁

  + `public  void unsafeReleasePage(TransactionId tid, PageId pid)`

    释放页面pid中所有事务tid的锁

  + `public void transactionComplete(TransactionId tid)`

    释放所有页面中事务tid的锁

  + `public boolean holdsLock(TransactionId tid, PageId p)`

    判断当前页面pid是否持有事务tid的锁

### 测试说明

您需要通过 `LockUnit`单元测试