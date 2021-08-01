package com.fiveysa.mongodb.crud.sync;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Main class
 *
 * @author wangcymy@gmail.com(wangcong) 2021/8/1 下午5:19
 */
public class Application {

  public static void main(String[] args) {

    String connectionString = "mongodb://192.168.1.19:27017";
    try (MongoClient client = MongoClients.create(connectionString)) {

//      insertDoc(client);

//      selectDoc(client);

//      updateDoc(client);
//      updateAndInsertDoc(client);

      deleteDoc(client);

    }

  }


  private static void insertDoc(MongoClient client) {

    /*
     * 如果库test和collection col_test1 不存在时，在第一次插入数据的时候，mongodb会自动创建。
     */

    // 所用操作在 test 库上完成
    MongoDatabase test = client.getDatabase("test");

    // 操作的collection为 col_test1, 相当于 rdbs 中的 表
    MongoCollection<Document> col_test1 = test.getCollection("col_test1");

    // 插入数据
    InsertOneResult insertResult = col_test1.insertOne(
        new Document().append("x", 1)
    );
    System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
        insertResult.getInsertedId());

    insertResult = col_test1.insertOne(
        new Document()
            .append("x", 2)
            .append("y", 1)
    );
    System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
        insertResult.getInsertedId());

    insertResult = col_test1.insertOne(
        new Document()
            .append("x", 3)
            .append("y", 2)
            .append("z", 1)
    );
    System.out.printf("insert status: %s, document id: %s %n", insertResult.wasAcknowledged(),
        insertResult.getInsertedId());
    // mongosh output
      /*
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
      { "_id" : ObjectId("61067014ecec5b16cdb8816d"), "x" : 1 }
      { "_id" : ObjectId("61067015ecec5b16cdb8816e"), "x" : 2, "y" : 1 }
      { "_id" : ObjectId("61067015ecec5b16cdb8816f"), "x" : 3, "y" : 1, "z" : 1 }
       */
  }

  private static void selectDoc(MongoClient client) {

    MongoDatabase test = client.getDatabase("test");

    // 操作的collection为 col_test1, 相当于 rdbs 中的 表
    MongoCollection<Document> col_test1 = test.getCollection("col_test1");

    // 查询一条
    Document doc = col_test1.find(eq("z", 1)).first();
    if (doc == null) {
      System.out.println("No results found.");
    } else {
      System.out.println(doc.toJson());
    }

    // 查询多条有指定返回字段和排序

    Bson fields = Projections.fields(
        Projections.include("x", "y"),
        Projections.exclude()
    );
    MongoCursor<Document> cursor = col_test1.find(lte("x", 3))
        .projection(fields)
        .sort(Sorts.descending("x")).iterator();
    try {
      while (cursor.hasNext()) {
        System.out.println(cursor.next().toJson());
      }
    } finally {
      cursor.close();
    }

    // output
    //    {"_id": {"$oid": "61067a288d7c2147ad2aedac"}, "x": 3, "y": 2}
    //    {"_id": {"$oid": "61067a288d7c2147ad2aedab"}, "x": 2, "y": 1}
    //    {"_id": {"$oid": "61067a278d7c2147ad2aedaa"}, "x": 1}

  }

  private static void updateDoc(MongoClient client) {

    MongoDatabase test = client.getDatabase("test");

    // 操作的collection为 col_test1, 相当于 rdbs 中的 表
    MongoCollection<Document> col_test1 = test.getCollection("col_test1");

    // 更新条件
    Bson where = eq("y", 2);

    // 更新内容
    Bson updates = Updates.combine(
        Updates.set("z", 2)
    );

    col_test1.updateOne(where, updates);
  }

  private static void updateAndInsertDoc(MongoClient client) {

    MongoDatabase test = client.getDatabase("test");

    // 操作的collection为 col_test1, 相当于 rdbs 中的 表
    MongoCollection<Document> col_test1 = test.getCollection("col_test1");

    // 更新条件
    Bson where = eq("z", 2);

    // 将 z=2 还原成 z=1
    Bson updates = Updates.combine(
        Updates.set("z", 1)
    );
    col_test1.updateOne(where, updates);

    // 此时 collection 中没有 z=2 的文档， update方法第三参数 upsert=ture时，库中没有匹配的文档时就会插入一条与条件匹配的文档。
    updates = Updates.combine(
        Updates.setOnInsert("x", 4),
        Updates.setOnInsert("y", 3)
    );
    col_test1.updateOne(where, updates, new UpdateOptions().upsert(true));


//    > db.col_test1.find()
//    { "_id" : ObjectId("61067a278d7c2147ad2aedaa"), "x" : 1 }
//    { "_id" : ObjectId("61067a288d7c2147ad2aedab"), "x" : 2, "y" : 1 }
//    { "_id" : ObjectId("61067a288d7c2147ad2aedac"), "x" : 3, "y" : 2, "z" : 1 }
//    { "_id" : ObjectId("6106b1e85f4475fba93685ce"), "z" : 2, "x" : 4, "y" : 3 }
  }


  private static void deleteDoc(MongoClient client) {

    MongoDatabase test = client.getDatabase("test");

    // 操作的collection为 col_test1, 相当于 rdbs 中的 表
    MongoCollection<Document> col_test1 = test.getCollection("col_test1");

    // 更新条件
    Bson where = lte("x", 3);

    DeleteResult deleteResult = col_test1.deleteOne(where);
    System.out.printf("status: %s, count: %s %n",
        deleteResult.wasAcknowledged(),
        deleteResult.getDeletedCount());
  }
}
