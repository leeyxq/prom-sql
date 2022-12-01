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
    <version>0.0.1</version>
</dependency>
```

#### 2.使用示例

```java

//http_requests_total{job=~".*server"}
String promSql=PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .regex("job",".*server")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"http_requests_total{job=~\".*server\"}");

//sum by(job)(rate(http_requests_total[5m]))
        promSql=PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .rate("5m")
        .sum("job")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"sum by(job)(rate(http_requests_total[5m]))");

//count by(app)(instance_cpu_time_ns)
        promSql=PromHelper.sqlBuilder()
        .metric("instance_cpu_time_ns")
        .count("app")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"count by(app)(instance_cpu_time_ns)");

//instance_cpu_time_ns{app="lion", proc="web", rev="34d0f99", env="prod", job="cluster-manager"}
        promSql=PromHelper.sqlBuilder()
        .metric("instance_cpu_time_ns")
        .eq("app","lion")
        .eq("proc","web")
        .eq("rev","34d0f99")
        .eq("env","prod")
        .eq("job","cluster-manager")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"instance_cpu_time_ns{app=\"lion\", proc=\"web\", rev=\"34d0f99\", env=\"prod\", job=\"cluster-manager\"}");

//instance_memory_usage_bytes / 1024 / 1024
        promSql=PromHelper.sqlBuilder()
        .metric("instance_memory_usage_bytes")
        .last(" / 1024 / 1024")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"instance_memory_usage_bytes / 1024 / 1024");

//sum by (app, proc)(instance_memory_usage_bytes) / 1024 / 1024
        promSql=PromHelper.sqlBuilder()
        .metric("instance_memory_usage_bytes")
        .sum("app","proc")
        .last(" / 1024 / 1024")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"sum by(app, proc)(instance_memory_usage_bytes) / 1024 / 1024");


//sum by(__name__)({__name__=~"queue_cu_total|queue_cu_allocated|queue_cu_used", userId="24", clusterType="online"})
        promSql=PromHelper.sqlBuilder()
        .metric("queue_cu_total","queue_cu_allocated","queue_cu_used")
        .eq("userId","24")
        .eq("clusterType","online")
        .sum("__name__")
        .build();
        System.out.println("promSql = "+promSql);
        Assert.assertEquals(promSql,"sum by(__name__)({__name__=~\"queue_cu_total|queue_cu_allocated|queue_cu_used\", userId=\"24\", clusterType=\"online\"})");


//avg without(cpu)(rate(node_cpu_seconds_total{mode="idle"}[5m]))
        promSql=PromHelper.sqlBuilder()
        .metric("node_cpu_seconds_total")
        .eq("mode","idle")
        .rate("5m")
        .avg(false,"cpu")
        .build();


//rate(http_requests_total[5m] offset -1w)
        promSql=PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .rate("5m")
        .offset("-1w")
        .build();


//topk(10, http_requests_total)
        promSql=PromHelper.sqlBuilder()
        .metric("http_requests_total")
        .topk(10)
        .build();

```
