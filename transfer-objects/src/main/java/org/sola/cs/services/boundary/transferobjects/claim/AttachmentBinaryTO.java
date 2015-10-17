package org.sola.cs.services.boundary.transferobjects.claim;

public class AttachmentBinaryTO extends AttachmentTO {
    
    private byte[] body;
    
    public AttachmentBinaryTO(){
        super();
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
