package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tags;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TagsUtils {

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private static final Map<Tags, String> cache = Collections.synchronizedMap(new HashMap<>());

    @SuppressWarnings("unchecked")
    public static Tags propToTags(ClipboardProperty property) {
        Tags tags = Tags.empty();
        if (property != null) {
            Iterator<ClipboardProperty> iter = (Iterator<ClipboardProperty>) property.iterator();
            while (iter.hasNext()) {
                ClipboardProperty e = iter.next();
                tags = tags.and(e.getName(), e.getStringValue());
            }
        }
        return tags;
    }

    public static String id(Tags tags) {
        return cache.computeIfAbsent(tags, t -> "tags" + NEXT_ID.incrementAndGet());
    }

    public static String id(ClipboardProperty prop) {
        return id(propToTags(prop));
    }
}
