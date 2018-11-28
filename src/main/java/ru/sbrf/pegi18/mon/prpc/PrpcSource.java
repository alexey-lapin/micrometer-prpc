package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;

/**
 *
 */
public interface PrpcSource {

    String resultsPropName();

    ClipboardProperty collect();

}
