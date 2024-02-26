package unsw.blackout;

public class FileTransferRestrictions {
    private int maxFiles;
    private int maxBytes;
    private int receivingBandwidth;
    private int sendingBandwidth;

    public FileTransferRestrictions(int maxFiles, int maxBytes, int receivingBandwidth, int sendingBandwidth) {
        this.maxFiles = maxFiles;
        this.maxBytes = maxBytes;
        this.receivingBandwidth = receivingBandwidth;
        this.sendingBandwidth = sendingBandwidth;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public int getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    public int getReceivingBandwidth() {
        return receivingBandwidth;
    }

    public void setReceivingBandwidth(int receivingBandwidth) {
        this.receivingBandwidth = receivingBandwidth;
    }

    public int getSendingBandwidth() {
        return sendingBandwidth;
    }

    public void setSendingBandwidth(int sendingBandwidth) {
        this.sendingBandwidth = sendingBandwidth;
    }
}
