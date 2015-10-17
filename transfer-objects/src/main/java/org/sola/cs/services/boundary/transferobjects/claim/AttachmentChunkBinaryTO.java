package org.sola.cs.services.boundary.transferobjects.claim;

public class AttachmentChunkBinaryTO extends AttachmentChunkTO {
    
    private byte[] body;
    
    public AttachmentChunkBinaryTO(){
        super();
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
