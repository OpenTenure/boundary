package org.sola.opentenure.services.boundary.beans.sorters;

import java.util.Comparator;
import org.sola.cs.services.ejbs.claim.entities.AttachmentChunk;

/**
 * Helps to sort in ascending order {@link AttachmentChunk} objects
 */
public class AttchmentChunkSorter implements Comparator<AttachmentChunk>{

    @Override
    public int compare(AttachmentChunk s1, AttachmentChunk s2) {
        if(s1.getStartPosition()<s2.getStartPosition())
            return -1;
        else if(s1.getStartPosition()>s2.getStartPosition())
            return 1;
        else
            return 0;
    }
}
