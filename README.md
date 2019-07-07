# micrometer-prpc
Pega PRPC + Micrometer = :heart:

Expose your business and technical metrics from Pega to any monitoring system.

This small library aims to simplify usage of [Micrometer](https://micrometer.io) for collecting metrics in [Pega PRPC](https://www.pega.com/products/pega-platform) environment.

### Build
|version|  compatibility   |
|:-----:|:----------------:|
| 7.3.0 |:heavy_check_mark:|

#### Prerequisites
In order to build project locally you have to satisfy dependency prerequisites.

This project does not include any dependencies or any proprietary code. 
It is intended to use by authorized Pegasystems Inc clients in their Pega PRPC environments.

This library relies on some internal prpc jars which usually could be found in 
`<pega-distributive>.zip/archives/pegadbinstall-classes.zip/lib`.

Libs:
- prpublic
- prprivate
- prenginext
- pricu2jdk
- prbootstrap
- prbootstrap-api

The following command will help to install required jars to local maven repository:
```
mvn install:install-file -Dfile=<path-to-prpc-libs>/<lib>-<version.prpc>.jar -DgroupId=com.pega.prpc -DartifactId=<lib> -Dversion=<version.prpc> -Dpackaging=jar
```

#### Package
After required jars are installed use the following command to build project:
```
mvn package
```

### Install
Deploy following jars to PRPC instance as usual (from UI or service):
- micrometer-core-\<version.micrometer>.jar
- micrometer-registry-\<registry>-\<version.micrometer>.jar
- micrometer-prpc-\<version>.jar

### Use
Create sources:
```java
// Sql source
PrpcSource source = SqlSource.builder()
        .queryString("select ... from {CLASS:Some-Data-Class}")
        .groupPropName("pxPages")
        .expirationDuration(2)
        .expirationTimeUnit(TimeUnit.MINUTES)
        .build();

// Data Page source
PrpcSource source = DataPageSource.builder()
        .ruleName("D_SomeDataPage")
        .accessGroupName("Some:AccessGroup")
        .resultsPropName("pxResults")
        .groupPropName("pxPages")
        .expirationDuration(5)
        .expirationTimeUnit(TimeUnit.MINUTES)
        .build();

// Activity source
PrpcSource source = DataPageSource.builder()
        .ruleName("SomeActivity")
        .ruleClass("Some-Class")
        .accessGroupName("Some:AccessGroup")
        .resultsPropName("pxResults")
        .groupPropName("pxPages")
        .expirationDuration(10)
        .expirationTimeUnit(TimeUnit.MINUTES)
        .build();
```

Register metrics:
```java
// Create registry
MeterRegistry registry = ...

// Gauge - register single metric
registry.gauge("gauge.metric.name", source, PrpcCallback.strong("PropertyReference"));

// MultiGauge - register multiple metrics with unique tags
MultiGauge mg = MultiGauge.builder("gauge.metric.name").register(registry);
Iterable<MultiGauge.Row> rows = MultiMeter.rows(source, "PropertyReference");
mg.register(rows);

// Counter
registry.more().counter("counter.metric.name", Tags.empty(), source, PrpcCallback.strong("PropertyReference"));
```

For more information about micrometer features visit micrometer [docs](https://micrometer.io/docs) page.