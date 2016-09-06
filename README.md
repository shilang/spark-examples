Spark SQL UDFs
=======

A collection of UDFs.

Requirements
------------

 * Maven 3 (to build)

## Building the UDFs

The UDFs can be build by running:

```bash   
$ mvn clean package -Pprd
```

## Using the UDFs

### For Spark SQL   
```sql   
add jar hdfs://nameservice1/tmp/spark-sql-udfs-0.1.0-SNAPSHOT-jar-with-dependencies.jar;                                          
create temporary function calculateAutoSimilarity as 'com.yhd.sql.udf.cb.sim.CalculateAutoSimilarity';       
create temporary function calculateSimilarity as 'com.yhd.sql.udf.cb.sim.CalculateSimilarity';                                       
SELECT
    product_id1,
    product_id2,
    calculateSimilarity (
        calculateAutoSimilarity (brand_name1, brand_name2),
        calculateAutoSimilarity (
            category_words1,
            category_words2
        ),
        calculateAutoSimilarity (
            attribute_words1,
            attribute_words2
        ),
        calculateAutoSimilarity (other_words1, other_words2),
        price_sim
    ) AS similarity
FROM
    pms.cate_product_pairs_all_with_name
```

### For hive(will add in the future)   
```sql                                      
```