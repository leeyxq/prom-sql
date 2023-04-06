# Prometheus SQL辅助工具

## 一、PromHelper功能

1.  支持单个或多个metric查询（暂不支持多个metric一起运算，如 `instance_memory_limit_bytes - instance_memory_usage_bytes） `)
2.  解决手动拼接复杂promSql的痛点

## 二、使用说明

#### 1.引入依赖

```xml 
 <!--引入最新版本maven依赖-->
<dependency>
    <groupId>io.github.leeyxq</groupId>
    <artifactId>prom-sql</artifactId>
    <scope>compile</scope>
    <version>0.0.2</version>
</dependency>
```

#### 2.使用示例

```java


//http_requests_total{job=~".*server"}
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .regex("job", ".*server")
        .build();
        
//sum by(__name__)({__name__=~"cpu_total|cpu_allocated|cpu_used", user="24", cluster="Test01"})
String promSql = PromHelper.sqlBuilder()
        .metric("cpu_total", "cpu_allocated", "cpu_used")
        .eq("user", "24")
        .eq("cluster", "Test01")
        .sum("__name__")
        .build();
        
//instance_cpu_time_ns{app="lion", proc="web", rev="34d0f99", env="prod", job!="cluster-manager"}
String promSql = PromHelper.sqlBuilder()
        .metric("instance_cpu_time_ns")
        .eq("app", "lion")
        .eq("proc", "web")
        .eq("rev", "34d0f99")
        .eq("env", "prod")
        .notEq("job", "cluster-manager")
        .eq(false, "instance", "")
        .notEq(false, "grade", "")
        .build();
        
//instance_cpu_time_ns{app!="", proc=""}
String promSql = PromHelper.sqlBuilder()
        .metric("instance_cpu_time_ns")
        .notEmpty("app")
        .empty("proc")
        .notEmpty(false, "aa")
        .empty(false, "bb")
        .build();
        
//absent(nonexistent{job="myjob", instance=~".*"})
String promSql = PromHelper.sqlBuilder()
        .metric("nonexistent")
        .eq("job", "myjob")
        .regex("instance", ".*")
        .absent()
        .build();
        

//http_requests_total{job=~".*server"}
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .regex("job", ".*server")
        .regex(false, "url", ".*query")
        .build();
        
//absent(nonexistent{job="myjob", instance!~".*"})
String promSql = PromHelper.sqlBuilder()
        .metric("nonexistent")
        .eq("job", "myjob")
        .notRegex("instance", ".*")
        .notRegex(false, "aa", ".*")
        .absent()
        .build();
        
//min by(type)(avg_over_time(cpu_used{user="001"}[300s] offset 5m))
String promSql = PromHelper.sqlBuilder()
        .metric("cpu_used")
        .eq("user", "001")
        .fn("avg_over_time", "300s")
        .offset("5m")
        .min("type")
        .build();
        
//absent_over_time(nonexistent{job="myjob", instance=~".*"}[1h])
String promSql = PromHelper.sqlBuilder()
        .metric("nonexistent")
        .eq("job", "myjob")
        .regex("instance", ".*")
        .fn("absent_over_time", "1h")
        .build();
        
//absent_over_time(nonexistent{job="myjob", instance=~".*"}[1h])
String promSql = PromHelper.sqlBuilder()
        .metric("nonexistent")
        .eq("job", "myjob")
        .regex("instance", ".*")
        .fn("absent_over_time", "1h")
        .build();
        
//sum without(instance)(http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .sum(false, "instance")
        .build();
        
//sum(http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .sum()
        .build();
        

// sum by(application, group)(http_requests_total)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .sum("application", "group")
        .build();
        

// sum without (instance) (http_requests_total)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .sum(false, "instance")
        .build();
        

//sum by(job)(rate(http_requests_total[5m]))
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .rate("5m")
        .sum("job")
        .build();
        
//avg(http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .avg()
        .build();
        
//count(http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .count()
        .build();
        
//count_values("job", http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .countValues("job")
        .build();
        
//topk(10, http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .topk(10)
        .build();
        
        
//bottomk(10, http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .bottomk(10)
        .build();
        
//sum(http_requests_total{method="GET"} offset 5m)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .eq("method", "GET")
        .offset("5m")
        .sum()
        .build();

//rate(http_requests_total[5m] offset 1w)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .rate("5m")
        .offset("1w")
        .build();

//rate(http_requests_total[5m] offset -1w)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .rate("5m")
        .offset("-1w")
        .build();
        
//delta(cpu_temp_celsius{host="zeus"}[2h])
String promSql = PromHelper.sqlBuilder()
        .metric("cpu_temp_celsius")
        .delta("2h")
        .build();
//max(cpu_temp_celsius{host="zeus"})
String promSql = PromHelper.sqlBuilder()
        .metric("cpu_temp_celsius")
        .eq("host", "zeus")
        .max()
        .build();

//max by(host)(cpu_temp_celsius{host!="zeus"})
promSql = PromHelper.sqlBuilder()
        .metric("cpu_temp_celsius")
        .notEq("host", "zeus")
        .max("host")
        .build();

//avg without(cpu)(rate(node_cpu_seconds_total{mode="idle"}[5m]))
promSql = PromHelper.sqlBuilder()
        .metric("node_cpu_seconds_total")
        .eq("mode", "idle")
        .rate("5m")
        .avg(false, "cpu")
        .build();
//irate(http_requests_total{job="api-server"}[5m])
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .eq("job", "api-server")
        .irate("5m")
        .build();
        
//abs(http_requests_total)
String promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .abs()
        .build();

//ceil(http_requests_total)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .ceil()
        .build();

//floor(http_requests_total)
promSql = PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .floor()
        .build();



//instance_memory_usage_bytes / 1024 / 1024
String promSql = PromHelper.sqlBuilder()
        .metric("instance_memory_usage_bytes")
        .last(" / 1024 / 1024")
        .build();

//sum by (app, proc)(instance_memory_usage_bytes) / 1024 / 1024
promSql = PromHelper.sqlBuilder()
        .metric("instance_memory_usage_bytes")
        .sum("app", "proc")
        .last(" / 1024 / 1024")
        .build();
        
// 所支持条件查询方法有以下：
        PromSqlBuilder eq(boolean condition, final String label, final String value)： 可选参数condition – 是否执行，以下类似
        PromSqlBuilder notEq(boolean condition, final String label, final String value)
        PromSqlBuilder empty(boolean condition, String label)
        PromSqlBuilder notEmpty(boolean condition, String label)
        PromSqlBuilder regex(boolean condition, String label, String value)
        PromSqlBuilder notRegex(boolean condition, String label, String value)

```
