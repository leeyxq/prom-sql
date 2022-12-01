package io.github.leeyxq.promsql;


import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lixiangqian
 * @since 2022/11/22 16:42
 */
@Slf4j
public class PromHelperTest {
    @Test
    public void testPromSqlBuilder() {
        //http_requests_total{job=~".*server"}
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .regex("job", ".*server")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("http_requests_total{job=~\".*server\"}", promSql);
    }

    @Test
    public void testMetric() {
        //sum by(__name__)({__name__=~"cpu_total|cpu_allocated|cpu_used", user="24", cluster="Test01"})
        String promSql = PromHelper.sqlBuilder()
                .metric("cpu_total", "cpu_allocated", "cpu_used")
                .eq("user", "24")
                .eq("cluster", "Test01")
                .sum("__name__")
                .build();

        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum by(__name__)({__name__=~\"cpu_total|cpu_allocated|cpu_used\", user=\"24\", cluster=\"Test01\"})", promSql);
    }

    @Test
    public void testEqAndNotEq() {
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
        log.info("promSql = {}", promSql);
        Assert.assertEquals("instance_cpu_time_ns{app=\"lion\", proc=\"web\", rev=\"34d0f99\", env=\"prod\", job!=\"cluster-manager\"}", promSql);
    }

    @Test
    public void testEmpty() {
        //instance_cpu_time_ns{app!="", proc=""}
        String promSql = PromHelper.sqlBuilder()
                .metric("instance_cpu_time_ns")
                .notEmpty("app")
                .empty("proc")
                .notEmpty(false, "aa")
                .empty(false, "bb")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("instance_cpu_time_ns{app!=\"\", proc=\"\"}", promSql);
    }

    @Test
    public void testRegex() {
        //absent(nonexistent{job="myjob", instance=~".*"})
        String promSql = PromHelper.sqlBuilder()
                .metric("nonexistent")
                .eq("job", "myjob")
                .regex("instance", ".*")
                .absent()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("absent(nonexistent{job=\"myjob\", instance=~\".*\"})", promSql);

        //http_requests_total{job=~".*server"}
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .regex("job", ".*server")
                .regex(false, "url", ".*query")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("http_requests_total{job=~\".*server\"}", promSql);
    }

    @Test
    public void testNotRegex() {
        //absent(nonexistent{job="myjob", instance!~".*"})
        String promSql = PromHelper.sqlBuilder()
                .metric("nonexistent")
                .eq("job", "myjob")
                .notRegex("instance", ".*")
                .notRegex(false, "aa", ".*")
                .absent()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("absent(nonexistent{job=\"myjob\", instance!~\".*\"})", promSql);
    }

    @Test
    public void testMin() {
        //min by(type)(avg_over_time(cpu_used{user="001"}[300s] offset 5m))
        String promSql = PromHelper.sqlBuilder()
                .metric("cpu_used")
                .eq("user", "001")
                .fn("avg_over_time", "300s")
                .offset("5m")
                .min("type")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("min by(type)(avg_over_time(cpu_used{user=\"001\"}[300s] offset 5m))", promSql);
    }

    @Test
    public void testFn() {
        //absent_over_time(nonexistent{job="myjob", instance=~".*"}[1h])
        String promSql = PromHelper.sqlBuilder()
                .metric("nonexistent")
                .eq("job", "myjob")
                .regex("instance", ".*")
                .fn("absent_over_time", "1h")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("absent_over_time(nonexistent{job=\"myjob\", instance=~\".*\"}[1h])", promSql);
    }

    @Test
    public void testAgg() {
        //absent_over_time(nonexistent{job="myjob", instance=~".*"}[1h])
        String promSql = PromHelper.sqlBuilder()
                .metric("nonexistent")
                .eq("job", "myjob")
                .regex("instance", ".*")
                .fn("absent_over_time", "1h")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("absent_over_time(nonexistent{job=\"myjob\", instance=~\".*\"}[1h])", promSql);
    }

    @Test
    public void testWithout() {
        //sum without(instance)(http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .sum(false, "instance")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum without(instance)(http_requests_total)", promSql);
    }

    @Test
    public void testSum() {
        //sum(http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .sum()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum(http_requests_total)", promSql);

        // sum by(application, group)(http_requests_total)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .sum("application", "group")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum by(application, group)(http_requests_total)", promSql);

        // sum without (instance) (http_requests_total)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .sum(false, "instance")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum without(instance)(http_requests_total)", promSql);

        //sum by(job)(rate(http_requests_total[5m]))
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .rate("5m")
                .sum("job")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum by(job)(rate(http_requests_total[5m]))", promSql);
    }

    @Test
    public void testAvg() {
        //avg(http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .avg()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("avg(http_requests_total)", promSql);
    }

    @Test
    public void testCount() {
        //count(http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .count()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("count(http_requests_total)", promSql);
    }

    @Test
    public void testCountValues() {
        //count_values("job", http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .countValues("job")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("count_values(\"job\", http_requests_total)", promSql);
    }

    @Test
    public void testTopk() {
        //topk(10, http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .topk(10)
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("topk(10, http_requests_total)", promSql);
    }

    @Test
    public void testBottomk() {
        //bottomk(10, http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .bottomk(10)
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("bottomk(10, http_requests_total)", promSql);
    }

    @Test
    public void testOffset() {
        //sum(http_requests_total{method="GET"} offset 5m)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .eq("method", "GET")
                .offset("5m")
                .sum()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum(http_requests_total{method=\"GET\"} offset 5m)", promSql);

        //rate(http_requests_total[5m] offset 1w)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .rate("5m")
                .offset("1w")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("rate(http_requests_total[5m] offset 1w)", promSql);

        //rate(http_requests_total[5m] offset -1w)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .rate("5m")
                .offset("-1w")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("rate(http_requests_total[5m] offset -1w)", promSql);
    }

    @Test
    public void testDelta() {
        //delta(cpu_temp_celsius{host="zeus"}[2h])
        String promSql = PromHelper.sqlBuilder()
                .metric("cpu_temp_celsius")
                .delta("2h")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("delta(cpu_temp_celsius[2h])", promSql);
    }

    @Test
    public void testMax() {
        //max(cpu_temp_celsius{host="zeus"})
        String promSql = PromHelper.sqlBuilder()
                .metric("cpu_temp_celsius")
                .eq("host", "zeus")
                .max()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("max(cpu_temp_celsius{host=\"zeus\"})", promSql);

        //max by(host)(cpu_temp_celsius{host!="zeus"})
        promSql = PromHelper.sqlBuilder()
                .metric("cpu_temp_celsius")
                .notEq("host", "zeus")
                .max("host")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("max by(host)(cpu_temp_celsius{host!=\"zeus\"})", promSql);

        //avg without(cpu)(rate(node_cpu_seconds_total{mode="idle"}[5m]))
        promSql = PromHelper.sqlBuilder()
                .metric("node_cpu_seconds_total")
                .eq("mode", "idle")
                .rate("5m")
                .avg(false, "cpu")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("avg without(cpu)(rate(node_cpu_seconds_total{mode=\"idle\"}[5m]))", promSql);
    }

    @Test
    public void testIRate() {
        //irate(http_requests_total{job="api-server"}[5m])
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .eq("job", "api-server")
                .irate("5m")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("irate(http_requests_total{job=\"api-server\"}[5m])", promSql);
    }

    @Test
    public void testMath() {
        //abs(http_requests_total)
        String promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .abs()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("abs(http_requests_total)", promSql);

        //ceil(http_requests_total)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .ceil()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("ceil(http_requests_total)", promSql);

        //floor(http_requests_total)
        promSql = PromHelper.sqlBuilder()
                .metric("http_requests_total")
                .floor()
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("floor(http_requests_total)", promSql);
    }

    @Test
    public void testLast() {
        //instance_memory_usage_bytes / 1024 / 1024
        String promSql = PromHelper.sqlBuilder()
                .metric("instance_memory_usage_bytes")
                .last(" / 1024 / 1024")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("instance_memory_usage_bytes / 1024 / 1024", promSql);

        //sum by (app, proc)(instance_memory_usage_bytes) / 1024 / 1024
        promSql = PromHelper.sqlBuilder()
                .metric("instance_memory_usage_bytes")
                .sum("app", "proc")
                .last(" / 1024 / 1024")
                .build();
        log.info("promSql = {}", promSql);
        Assert.assertEquals("sum by(app, proc)(instance_memory_usage_bytes) / 1024 / 1024", promSql);
    }

}