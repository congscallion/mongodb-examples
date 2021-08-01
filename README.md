# MongoDB 基于Java或shell的应用示例



现代应用随着数据的剧烈增长， `RDBS` 设计、开发、扩展、维护都越来越吃力。反而是`NoSQL`是这些操作在大数量理变得越来越简便。因此将自己在学习和日常中使用的一些示例记录下，方便以后查漏补缺和复习。



> mongodb 版本: 5.0.1
>
> Java drivers version: 4.3.0
>
> Java version: 1.8



### MongoDB Java驱动的CRUD

```java
String connectionString = "mongodb://192.168.1.19:27017";
    try (MongoClient client = MongoClients.create(connectionString)) {

      /*
       * 如果库test和collection col_test1 不存在时，在第一次插入数据的时候，mongodb会自动创建。
       */

      // 指定数据库为 test
      MongoDatabase test = client.getDatabase("test");

      // 指定collection为 col_test1, 相当于 rdbs 中的 表
      MongoCollection<Document> col_test1 = test.getCollection("col_test1");
	
	  // 插入文档 {x:1}
      InsertOneResult insertResult = col_test1.insertOne(
          new Document().append("x", 1)
      );
      System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
          insertResult.getInsertedId());
	
	  // 插入文档 {x:1，y:1}
      insertResult = col_test1.insertOne(
          new Document()
              .append("x", 2)
              .append("y", 1)
      );
      System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
          insertResult.getInsertedId());
          
      // 插入文档 {x:1，y:1,z:1}
      insertResult = col_test1.insertOne(
          new Document()
              .append("x", 3)
              .append("y", 2)
              .append("z", 1)
      );
      System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
          insertResult.getInsertedId());
    }
```

`output`

```shell
> show dbs
admin   0.000GB
config  0.000GB
local   0.000GB
mock    0.048GB

> show dbs
admin   0.000GB
config  0.000GB
local   0.000GB
mock    0.048GB
test    0.000GB

> use test
switched to db test

> db.col_test1.find()
{ "_id" : ObjectId("61067a278d7c2147ad2aedaa"), "x" : 1 }
{ "_id" : ObjectId("61067a288d7c2147ad2aedab"), "x" : 2, "y" : 1 }
{ "_id" : ObjectId("61067a288d7c2147ad2aedac"), "x" : 3, "y" : 2, "z" : 1 }
```

这里就可以看 mongodb 相较于mysql等 rdbs 数据库的灵活性了。 同一个 collection （表）中不要求每个文档（记录）都要有相同的字段 。上面示例中插入了三个文档:

```json
{"x":1}
{"x":2，"y":1}
{"x":3，"y":2,"z":1}
```

后一个文档在前一个文档 基础上增加了一个字段 ， 这被 称为mongodb的无模式（schemaless）特性。 es 也如此。当业务变化的比较频繁时，这种特性相较于rdbs会更有利于开发。

