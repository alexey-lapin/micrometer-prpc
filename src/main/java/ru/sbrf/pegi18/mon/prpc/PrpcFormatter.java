package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by sp00x on 24-Nov-18.
 * Project: pg-micrometer
 */
public class PrpcFormatter {

//    private MeterRegistry registry;
//
//    public PrpcFormatter(MeterRegistry registry) {
//        this.registry = registry;
//    }
//
//    public String promify(PrpcSource source) {
//
//        StringWriter writer = new StringWriter();
//        writer.write("# HELP ");
//        writer.write(namify(source.id()));
//        writer.write("\n");
//        writer.write("# TYPE ");
//        writer.write(namify(source.id()));
//        writer.write(32);
//        writer.write("gauge");
//        writer.write("\n");
//
//        ClipboardProperty prop = source.collect();
//        if (prop != null) {
//            ((Iterator<ClipboardProperty>)source.collect().iterator()).forEachRemaining(o -> {
//                writer.write(seriefy(source.id(), source, o.getPageValue()));
//                writer.write("\n");
//            });
//        }
//
//        return writer.toString();
//    }
//
//    public String promify(ClipboardProperty prop) {
//        return "zz";
//    }
//
//    private String namify(Meter.Id meterId) {
//        return registry.config()
//                .namingConvention()
//                .name(meterId.getName(), meterId.getType(), meterId.getBaseUnit());
//    }
//
//    private String typify(Meter.Id meterId) {
//        return registry.config()
//                .namingConvention()
//                .name(meterId.getName(), meterId.getType(), meterId.getBaseUnit());
//    }
//
//    private String tagify(Meter.Id meterId) {
//        String tags = meterId.getTags().stream()
//                .map(t -> t.getKey() + "=" + t.getValue())
//                .collect(Collectors.joining(","));
//        return  "{" + tags + "}";
//    }
//
//    private String tagify(Iterable<Tag> tagIterable) {
//        String tags = StreamSupport.stream(tagIterable.spliterator(), false)
//                .map(t -> t.getKey() + "=" + t.getValue())
//                .collect(Collectors.joining(","));
//        return  "{" + tags + "}";
//    }
//
//    private String seriefy(Meter.Id meterId, PrpcSource source, ClipboardPage page) {
//        StringWriter writer = new StringWriter();
//        writer.write(namify(meterId));
//
//        String tagsPropName = source.tagsProp();
//        ClipboardProperty tagsProp = page.getProperty(tagsPropName);
//        writer.write(tagify(meterId.getTags())); //get tags from page
//        writer.write(32);
//
//        String valueProp = source.valueProp();
//        String stringValue = page.getProperty(valueProp).getStringValue();
//        writer.write(stringValue);
//        return writer.toString();
//    }
//
//    private Iterable<Tag> tagsFromProperty(ClipboardProperty prop) {
//        Tags tags = Tags.empty();
//        Iterator<ClipboardProperty> iterator =  prop.iterator();
//        while(iterator.hasNext()) {
//            ClipboardProperty current = iterator.next();
//            tags.and(current.getName(), current.getStringValue());
//        }
//        return tags;
//    }
}
