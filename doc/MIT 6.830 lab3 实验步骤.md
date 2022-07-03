### MIT 6.830 lab3 实验步骤

### 优化器整体结构

在实验开始之前需要对优化器整体有一个了解，解析器和优化器的整理流程如图所示：

<p align="center">
<img width=400 src="controlflow.png"><br>
<i>Figure 1: Diagram illustrating classes, methods, and objects used in the parser</i>
</p>




在本实验中，需要实现图中具有双边框的组件，下面对部分进行解释。

`Parser.java`: 其中`tableStatsMap`结构存储表的状态信息，然后等待输出查询语句，然后调用`parseQuery`方法。

`parseQuery.java`: 首先构造一个`LogicPlan`表示已经解析的查询，然后在其`LogicPlan`实例上调用`physicalPlan`,`PhysicalPlan`返回一个可以迭代的`DBIterator`对象

### 查询成本估算

该实验需要完成图中`estimateSelecvity`功能，首先需要实现的是统计估算的工作，也就是计算访问成本，以便于更好的进行查询。

#### 整体计划成本

在多表连接，如`p = t1 join t2 join .. tn`的情况中,访问成本可表示为

```
scancost(t1) + scancost(t2) + joincost(t1 join t2) +
scancost(t3) + joincost((t1 join t2) join t3) +
... 
```

其中,`	scancost(t1)`可以理解为访问`t1`的`I/O`时间，`joincost(t1,t2)`表示连接`t1,t2`表的CPU时间，为了让`I/O`时间和`CPU`时间具有可比性，通常使用一个恒定的比例因子，例如：

```
cost(predicate application) = 1
cost(pageScan) = SCALING_FACTOR x cost(predicate application)
```

#### Join cost

Join的cost可以很简单的表达出来

```
joincost(t1 join t2) = scancost(t1) + ntups(t1) x scancost(t2) //IO cost
                       + ntups(t1) x ntups(t2)  //CPU cost
```

#### Filter selectivity

`ntups`用来表示表中的元组数目，但涉及到范围查询等谓词时则需要预估查询成本，可以使用预估成本的一种方法是采用直方图来进行预估。

+ 首先计算表中每一个属性的最大值`max`和最小值`min`
+ 以固定的大小将其分成桶，例如桶的大小为10，则将其分成1-10,11~20,...。
+ 扫描整个表，将其分配在每个桶中，仅维护其数量
+ 当我们估计等值运算的选择性时，例如`f=const`时，计算它的成本为当前桶的(h/w)/ntups,此处假设桶内的分布是均匀的，则h/w为桶内`f=const`的个数，再除以`ntups`则表示查询成本
+ 当我们估算非等值运算的选择性时，例如`f>const`,如图二，我们需要计算的是右侧阴影部分的贡献，假设`const`所在的桶为b,宽度为`w_b`,高度为`h_b`,它包含的总元组占全部元组的百分数为`b_f = h_b / ntups`,其中`>const`部分所占的比例为`b_part = (b_right - const) / b_width`,则在桶`b`内的贡献值为`b_f * b_part`，再加上右侧的元组数`cnt+=b[i] i in b+1,b+2...`除以`ntups`即为总的贡献，`b_f * b_part + cnt/ntups`
+ `f<const`的情况可等价于`1 - f>const - f==const`的情况，其他情况也可类比

<p align="center">
<img width=400 src="lab3-hist.png"><br>
<i>Figure 2: Diagram illustrating the histograms you will implement in Lab 5</i>
</p>

#### Exercise1.

实验一需要我们完善`IntHistogram.java`文件，如上所说的，该文件维护Int类型的直方图，下面将会对其要实现的方法进行分析

+ 构造函数`IntHistogram(int buckets, int min, int max)`

  该函数传入表中属性的最大值和最小值以及要分成几个桶`buckets`,在该类中需要一个长度为`bucket`的数组来维护每个桶中有多少个`Tuple`以便于后面的判断和计算

+ 添加值`public void addValue(int v)`：

  该函数将v添加到桶中，需要判断v属于哪个桶，如果不在范围内则直接返回

+ 评估选择性`public double estimateSelectivity(Predicate.Op op, int v)`

  该函数是最为重要的函数，其中op得实现`EQUALS`,`NOT_EQUALS`,`GREATER_THAN`,`LESS_THAN`,`LESS_THAN_OR_EQ`,`GREATER_THAN_OR_EQ`等运算符，只需要实现`EQUALS`和``GREATER_THAN``按照上述的文档，其他的可以使用类比的方法进行转换。

+ 平均选择性`public double avgSelectivity()`

  返回每个桶的平均选择性。

##### related file

+ IntHistogram.java

#### Exercise2.

在上述实验中，我们完成了`IntHistogram.java`的编写并且通过了测试，当前实验则需要完成对整个表建立直方图，实验中已经实现了`StringHistogram.java`文件，该类与`IntHistograml`类似。本实验需要完成`TableStat.java`文件，完成该类。

+ 构造函数

```java
public TableStats(int tableid, int ioCostPerPage) {
    // For this function, you'll have to get the
    // DbFile for the table in question,
    // then scan through its tuples and calculate
    // the values that you need.
    // You should try to do this reasonably efficiently, but you don't
    // necessarily have to (for example) do everything
    // in a single scan of the table.
    // some code goes here
}
```

​	如上，该构造函数不仅需要对属性进行赋值，还需要获取当前表的内容，可以通过`Database.getCatalog().getDatabaseFile()`接口来获取整个表的内容，包括`TupleDesc`以及`Tuple`的迭代器，然后使用合适的数据结构来存储每个字段的`Histogram`,再将每个`tuple`的值插入直方图中即可。

+ 选择因子

  ```java
  /**
       * This method returns the number of tuples in the relation, given that a
       * predicate with selectivity selectivityFactor is applied.
       * 
       * @param selectivityFactor
       *            The selectivity of any predicates over the table
       * @return The estimated cardinality of the scan with the specified
       *         selectivityFactor
       */
  public int estimateTableCardinality(double selectivityFactor) {
      // some code goes here
      return null;
  }
  ```

  如注释所说，需要返回扫描的预估数量，统计该表中的记录数，乘以该因子即可

+ 平均选择性

  ```java
  /**
       * The average selectivity of the field under op.
       * @param field
       *        the index of the field
       * @param op
       *        the operator in the predicate
       * The semantic of the method is that, given the table, and then given a
       * tuple, of which we do not know the value of the field, return the
       * expected selectivity. You may estimate this value from the histograms.
       * */
  public double avgSelectivity(int field, Predicate.Op op) {
      // some code goes here
  }
  ```

  该方法返回字段的平均选择性，可以通过实验一中实现的方法进行实现。

+ 预估选择性

  ```java
  public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
      // some code goes here
      if(tupleDesc.getFieldType(field).equals(Type.INT_TYPE)){
          IntField intField = (IntField) constant;
          return intHistogramMap.get(field).estimateSelectivity(op,intField.getValue());
      }else{
          StringField stringField = (StringField) constant;
          return stringHistogramMap.get(field).estimateSelectivity(op,stringField.getValue());
      }
  }
  ```

  该方法返回预估选择性大小，可根据字段类型以及`IntHistogram`和`StringHistogram`中的方法来实现。

##### related file

+ IntHistogram.java
+ TableStat.java
+ StringHistogram.java

#### **Exercise 3: **

+ Join Cost Estimation

The class <tt>JoinOptimizer.java</tt> includes all of the methods
for ordering and computing costs of joins.  In this exercise, you
will write the methods for estimating the selectivity and cost of
a join, specifically:

*  Implement <tt>
   estimateJoinCost(LogicalJoinNode j, int card1, int card2, double
   cost1, double cost2)</tt>:  This method estimates the cost of
   join j, given that the left input is of cardinality card1, the
   right input of cardinality card2, that the cost to scan the left
   input is cost1, and that the cost to access the right input is
   card2.  You can assume the join is an NL join, and apply
   the formula mentioned earlier.
*  Implement <tt>estimateJoinCardinality(LogicalJoinNode j, int
   card1, int card2, boolean t1pkey, boolean t2pkey)</tt>: This
   method estimates the number of tuples output by join j, given that
   the left input is size card1, the right input is size card2, and
   the flags t1pkey and t2pkey that indicate whether the left and
   right (respectively) field is unique (a primary key).

After implementing these methods, you should be able to pass the unit
tests <tt>estimateJoinCostTest</tt> and <tt>estimateJoinCardinality</tt> in <tt>JoinOptimizerTest.java</tt>.

如上，需要完成两个函数，两个函数分别为`estimateJoinCost`和`estimateJoinCardinality`,前者用来评估连接操作的cost，后者则用来评估连接操作的大致涉及的基数，都是为了更好的优化查询。

在前面已经表述过的连接cost应该在前者函数进行实现

```
joincost(t1 join t2) = scancost(t1) + ntups(t1) x scancost(t2) //IO cost
+ ntups(t1) x ntups(t2)  //CPU cost
```

而后者则通过如下方式进行实现

* For equality joins, when one of the attributes is a primary key, the number of tuples produced by the join cannot
  be larger than the cardinality of the non-primary key attribute.
* For equality joins when there is no primary key, it's hard to say much about what the size of the output
  is -- it could be the size of the product of the cardinalities of the tables (if both tables have the
  same value for all tuples) -- or it could be 0.  It's fine to make up a simple heuristic (say,
  the size of the larger of the two tables).
* For range scans, it is similarly hard to say anything accurate about sizes.
  The size of the output should be proportional to
  the sizes of the inputs.  It is fine to assume that a fixed fraction
  of the cross-product is emitted by range scans (say, 30%).  In general, the cost of a range
  join should be larger than the cost of a non-primary key equality join of two tables
  of the same size.

说明：

+ 对于等值连接，当属性之一是主键时，连接产生的元组数不能大于非主键属性的基数。
+ 对于没有主键的相等连接，很难说基数的大小是多少，它可能是表的基数乘积的大小（如果两个表都有
  所有元组的值相同），或者它可能是 0。组成一个简单的启发式方法很好（例如：两个表中较大者的大小）。
+ 对于范围扫描，同样很难准确地说出尺寸。输出的大小应与输入的大小。可以假设一个固定分数的叉积是由范围扫描发出的（例如，30%）。一般来说，一个范围的成本连接应该大于两个表的非主键相等连接的成本大小相同。

##### related file

+ JoinOptimizer.java

  根据上述简介在函数`estimateJoinCardinality`中写入对应的代码

#### Exercise 4:

现在我们已经实现了评估成本的方法，接下来我们就需要优化连接的顺序，每个连接都将其视为一个node，实验中给出的伪代码如下：

```
1. j = set of join nodes
2. for (i in 1...|j|):
3.     for s in {all length i subsets of j}
4.       bestPlan = {}
5.       for s' in {all length d-1 subsets of s}
6.            subplan = optjoin(s')
7.            plan = best way to join (s-s') to subplan
8.            if (cost(plan) < cost(bestPlan))
9.               bestPlan = plan
10.      optjoin(s) = bestPlan
11. return optjoin(j)
```

上述伪代码可能比较难理解，那么可以举一个例子，如果枚举所有的子集，那么当集合大小为10的时候，将会有176亿中排列，则需要优化。优化方式如书中所示如下

<p align="center">
<img width=800 src="joinsolve.png"><br>
<i></i>
</p>

我们需要实现

```java
 public List<LogicalJoinNode> orderJoins(
            Map<String, TableStats> stats,
            Map<String, Double> filterSelectivities, boolean explain)
```

该函数将需要连接的表进行重新排序获得更加优秀的连接顺序。

系统已经实现了`enumerateSubsets(List v, int size)` in `JoinOptimizer.java`方法来枚举所有子集，传入list和size即可得到该集合大小为size的所有子集。以及`computeCostAndCardOfSubplan`函数来帮助你实现该算法，该函数签名如下：

```java
private CostCard computeCostAndCardOfSubplan(Map<String, TableStats> stats, 
                                                Map<String, Double> filterSelectivities, 
                                                LogicalJoinNode joinToRemove,  
                                                Set<LogicalJoinNode> joinSet,
                                                double bestCostSoFar,
                                                PlanCache pc) 
```

该方法计算将`joinToRemove`节点添加到`joinset - {joinToremove}`节点中的最佳连接方式，`bestCostSoFar`为当前的最佳评估得分，pc为存储`Plan`的缓冲区，返回类型为`CostCard`其中包含了`Cost`，`List<LogicalJoinNode> Plan`,以及`Card`。

其中若`explain`为`true`则需要打印该`join`调用方法：

```java
private void printJoins(List<LogicalJoinNode> js, 
                       PlanCache pc,
                       Map<String, TableStats> stats,
                       Map<String, Double> selectivities)
```
##### related file

+ JoinOptimizer.java