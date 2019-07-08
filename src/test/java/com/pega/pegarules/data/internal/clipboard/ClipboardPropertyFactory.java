package com.pega.pegarules.data.internal.clipboard;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;

public class ClipboardPropertyFactory {

    private static ClipboardPropertyFactory instance;

    public static ClipboardPropertyFactory getInstance() {
        return instance;
    }

    public static void setInstance(ClipboardPropertyFactory instance) {
        ClipboardPropertyFactory.instance = instance;
    }

    public static ClipboardProperty getMostSuitableClipboardObject(String aReference, char aMode, ClipboardPage aProspectiveParent) {
        if (getInstance() != null) {
            return getInstance().getMostSuitableClipboardObjectMocked(aReference, aMode, aProspectiveParent);
        }
        return null;
    }

    public ClipboardProperty getMostSuitableClipboardObjectMocked(String aReference, char aMode, ClipboardPage aProspectiveParent) {
        return null;
    }

}
