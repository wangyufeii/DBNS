/usr/local/spark/bin/spark-submit \
--class "LogStreamingAdvancedSparkCluster" \
--master spark://spark-master:7077 \
/usr/local/DBNS/target/scala-2.10/simple-project_2.10-1.0.jar \
15 20
