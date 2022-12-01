package io.github.leeyxq.promsql;

import io.github.leeyxq.promsql.util.Asserts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.leeyxq.promsql.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * prom查询辅助类
 *
 * @author lixiangqian
 * @since 2022/11/22 14:34
 */
@Slf4j
@UtilityClass
public class PromHelper {
    public static final String DURATION = "${duration}";

    /**
     * 获取一个PromSqlBuilder实例
     *
     * @return PromSqlBuilder实例
     */
    PromSqlBuilder sqlBuilder() {
        return new PromSqlBuilder();
    }

    public static final class PromSqlBuilder {
        /*
        <aggr-op> [without|by (<label list>)] ([parameter,] <vector expression>)

        Aggregation operators
        sum (calculate sum over dimensions)
        min (select minimum over dimensions)
        max (select maximum over dimensions)
        avg (calculate the average over dimensions)
        group (all values in the resulting vector are 1)
        stddev (calculate population standard deviation over dimensions)
        stdvar (calculate population standard variance over dimensions)
        count (count number of elements in the vector)
        count_values (count number of elements with the same value)
        bottomk (smallest k elements by sample value)
        topk (largest k elements by sample value)
        quantile (calculate φ-quantile (0 ≤ φ ≤ 1) over dimensions)
        */
        private final List<BaseOp> opCaches = new ArrayList<>();

        private PromSqlBuilder() {
        }

        public PromSqlBuilder metric(final String... metrics) {
            Asserts.notEmpty(metrics, "metrics must not be empty");
            opCaches.add(new MetricOp(metrics));
            return this;
        }

        public PromSqlBuilder offset(final String offset) {
            Asserts.notBlank(offset, "offset must not be empty");
            opCaches.add(new OffsetOp(offset));
            return this;
        }

        //==================查询条件-开始==================
        public PromSqlBuilder eq(final String label, final String value) {
            return eq(true, label, value);
        }

        public PromSqlBuilder eq(boolean condition, final String label, final String value) {
            Asserts.notBlank(label, "label must not be empty");
            if (!condition) {
                return this;
            }
            opCaches.add(new LabelOp("=", label, value));
            return this;
        }

        public PromSqlBuilder notEq(String label, String value) {
            return notEq(true, label, value);
        }

        public PromSqlBuilder notEq(boolean condition, final String label, final String value) {
            Asserts.notBlank(label, "label must not be empty");
            if (!condition) {
                return this;
            }
            opCaches.add(new LabelOp("!=", label, value));
            return this;
        }

        public PromSqlBuilder empty(String label) {
            return eq(label, StrUtil.EMPTY);
        }

        public PromSqlBuilder empty(boolean condition, String label) {
            return eq(condition, label, StrUtil.EMPTY);
        }

        public PromSqlBuilder notEmpty(String label) {
            return notEq(label, StrUtil.EMPTY);
        }

        public PromSqlBuilder notEmpty(boolean condition, String label) {
            return notEq(condition, label, StrUtil.EMPTY);
        }

        public PromSqlBuilder regex(String label, String value) {
            return regex(true, label, value);
        }

        public PromSqlBuilder regex(boolean condition, String label, String value) {
            Asserts.notBlank(label, "label must not be empty");
            if (!condition) {
                return this;
            }
            opCaches.add(new LabelOp("=~", label, value));
            return this;
        }

        public PromSqlBuilder notRegex(String label, String value) {
            return notRegex(true, label, value);
        }

        public PromSqlBuilder notRegex(boolean condition, String label, String value) {
            Asserts.notBlank(label, "label must not be empty");
            if (!condition) {
                return this;
            }
            opCaches.add(new LabelOp("!~", label, value));
            return this;
        }

        //==================查询条件-结束==================

        //==================聚合操作-开始==================

        public PromSqlBuilder sum(String... labels) {
            return sum(true, labels);
        }

        public PromSqlBuilder sum(boolean isBy, String... labels) {
            return agg("sum", isBy, labels);
        }

        public PromSqlBuilder min(String... labels) {
            return min(true, labels);
        }

        public PromSqlBuilder min(boolean isBy, String... labels) {
            return agg("min", isBy, labels);
        }

        public PromSqlBuilder max(String... labels) {
            return max(true, labels);
        }

        public PromSqlBuilder max(boolean isBy, String... labels) {
            return agg("max", isBy, labels);
        }

        public PromSqlBuilder avg(String... labels) {
            return avg(true, labels);
        }

        public PromSqlBuilder avg(boolean isBy, String... labels) {
            return agg("avg", isBy, labels);
        }


        public PromSqlBuilder count(String... labels) {
            return count(true, labels);
        }

        public PromSqlBuilder count(boolean isBy, String... labels) {
            return agg("count", isBy, labels);
        }

        public PromSqlBuilder countValues(String label) {
            //count_values("version", build_version)
            Asserts.notBlank(label, "label is required");
            opCaches.add(new AggOp("count_values(\"" + label + "\", %s)"));
            return this;
        }

        public PromSqlBuilder topk(Integer top) {
            //topk(3, sum by (app, proc) (rate(instance_cpu_time_ns[5m])))
            Asserts.notNull(top, "top is required");
            opCaches.add(new AggOp("topk(" + top + ", %s)"));
            return this;
        }

        public PromSqlBuilder bottomk(Integer bottom) {
            //topk(3, sum by (app, proc) (rate(instance_cpu_time_ns[5m])))
            Asserts.notNull(bottom, "bottom is required");
            opCaches.add(new AggOp("bottomk(" + bottom + ", %s)"));
            return this;
        }

        /**
         * 聚合操作
         *
         * @param agg    聚合函数
         * @param isBy   是否使用by，还是用without
         * @param labels label列表-可选
         * @return PromSqlBuilder
         */
        public PromSqlBuilder agg(String agg, boolean isBy, String... labels) {
            //<aggr-op> [without|by (<label list>)] ([parameter,] <vector expression>) or  <aggr-op>([parameter,] <vector expression>) [without|by (<label list>)]
            if (labels.length == 0) {
                opCaches.add(new AggOp(agg + "(%s)"));
                return this;
            }
            opCaches.add(new AggOp(agg + (isBy ? " by" : " without") + "(" + String.join(", ", labels) + ")(%s)"));
            return this;
        }
        //==================聚合操作-结束==================

        //==================函数操作-开始==================
        public PromSqlBuilder rate(String duration) {
            //rate(http_requests_total[5m])
            return fn("rate", duration);
        }

        public PromSqlBuilder irate(String duration) {
            //irate(http_requests_total{job="api-server"}[5m])
            return fn("irate", duration);
        }

        public PromSqlBuilder abs() {
            //abs(v instant-vector)
            return fn("abs");
        }

        public PromSqlBuilder absent() {
            //absent(nonexistent{job="myjob"})
            return fn("absent");
        }

        public PromSqlBuilder ceil() {
            //ceil(v instant-vector)
            return fn("ceil");
        }

        public PromSqlBuilder floor() {
            //floor(v instant-vector)
            return fn("floor");
        }

        public PromSqlBuilder delta(String duration) {
            //delta(cpu_temp_celsius{host="zeus"}[2h])
            return fn("delta", duration);
        }

        /**
         * 函数 如数学函数abs、max、min之类的
         *
         * @param functionName 函数名
         * @return PromSqlBuilder
         */
        public PromSqlBuilder fn(String functionName) {
            //abs(v instant-vector)
            opCaches.add(new AggOp(functionName + "(%s)"));
            return this;
        }

        /**
         * 函数-带有时间段参数
         *
         * @param functionName 函数名：如ceil、floor、abs
         * @param duration     时间区间，如5m、1h
         * @return PromSqlBuilder
         */
        public PromSqlBuilder fn(String functionName, String duration) {
            //delta(cpu_temp_celsius{host="zeus"}[2h])
            Asserts.notBlank(duration, "duration is required");
            opCaches.add(new DurationAggOp(functionName + "(%s)", duration));
            return this;
        }
        //==================函数操作-结束==================

        public PromSqlBuilder last(String last) {
            opCaches.add(new LastOp(last));
            return this;
        }

        public String build() {
            String promSql = StrUtil.EMPTY;
            //1.指标名处理
            List<String> metrics = opCaches.stream().filter(MetricOp.class::isInstance).flatMap(op -> Arrays.stream(((MetricOp) op).metric)).collect(Collectors.toList());
            if (metrics.size() == 1) {
                promSql = metrics.get(0);
            } else {
                opCaches.add(0, new LabelOp("=~", "__name__", String.join("|", metrics)));
            }

            //2.label条件处理
            List<String> labels = opCaches.stream().filter(LabelOp.class::isInstance).map(LabelOp.class::cast).map(labelOp -> String.format("%s%s\"%s\"", labelOp.label, labelOp.operator, labelOp.values.length != 0 ? labelOp.values[0] : "")).collect(Collectors.toList());
            if (!labels.isEmpty()) {
                promSql += "{%s}";
                promSql = String.format(promSql, String.join(", ", labels));
            }

            promSql += DURATION;

            Optional<OffsetOp> offsetOp = opCaches.stream().filter(OffsetOp.class::isInstance).map(OffsetOp.class::cast).findFirst();
            if (offsetOp.isPresent()) {
                promSql += " offset " + offsetOp.get().offset;
            }

            //3.聚合操作处理
            for (BaseOp baseOp : opCaches) {
                if (baseOp instanceof AggOp) {
                    AggOp agg = (AggOp) baseOp;
                    promSql = String.format(agg.operator, promSql);
                    if (agg instanceof DurationAggOp) {
                        promSql = promSql.replace(DURATION, "[" + ((DurationAggOp) agg).duration + "]");
                    }
                }
            }
            promSql = promSql.replace(DURATION, StrUtil.EMPTY);

            //4.last操作处理
            Optional<LastOp> lastOpList = opCaches.stream().filter(LastOp.class::isInstance).map(LastOp.class::cast).findFirst();
            if (lastOpList.isPresent()) {
                promSql += lastOpList.get().operator;
            }

            return promSql;
        }

        @Data
        @AllArgsConstructor
        private abstract static class BaseOp {
            @Getter
            public final String operator;
        }

        private static class MetricOp extends BaseOp {
            @Getter
            public final String[] metric;

            public MetricOp(String... metric) {
                super(null);
                this.metric = metric;
            }
        }

        private static class LabelOp extends BaseOp {
            @Getter
            public final String label;
            @Getter
            public final String[] values;

            LabelOp(String operator, String label, String... values) {
                super(operator);
                this.label = label;
                this.values = values;
            }
        }

        private static class AggOp extends BaseOp {
            AggOp(String operator) {
                super(operator);
            }
        }

        private static class DurationAggOp extends AggOp {
            @Getter
            private final String duration;

            DurationAggOp(String operator, String duration) {
                super(operator);
                this.duration = duration;
            }
        }

        private static class OffsetOp extends BaseOp {
            @Getter
            public final String offset;

            OffsetOp(String offset) {
                super(null);
                this.offset = offset;
            }
        }

        private static class LastOp extends BaseOp {
            LastOp(String operator) {
                super(operator);
            }
        }
    }
}
