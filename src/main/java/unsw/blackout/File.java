package unsw.blackout;

import unsw.response.models.FileInfoResponse;

public class File {
    private String filename;
    private String content;
    private int size;
    private Entity fromEntity;
    private Entity toEntity;
    private int progress;

    public File(String filename, String content, Entity fromEntity, Entity toEntity, int progress) {
        this.filename = filename;
        this.content = content;
        this.size = content.length();
        this.fromEntity = fromEntity;
        this.toEntity = toEntity;
        this.progress = progress;
    }

    // Returns FileInfoResponse
    public FileInfoResponse getFileInfoResponse() {
        String data = this.content.substring(0, this.progress);
        return new FileInfoResponse(filename, data, size, this.isFileComplete());
    }

    // Removes all "t" bytes from file
    public void removeAllTs() {
        String allContent = this.getContent();
        String removeT = allContent.replace("t", "");
        this.setContent(removeT);
        this.setSize(removeT.length());
        this.setProgress(removeT.length());
    }

    // Removes remaining "t" bytes of undownlaoded content
    public void removeRemainingTs() {
        String downloaded = this.getContent().substring(0, this.progress);
        String unDownloaded = this.getContent().substring(this.progress + 1, this.size);
        String removeT = unDownloaded.replace("t", "");
        downloaded += removeT;
        this.setContent(downloaded);
        this.setProgress(downloaded.length());
        this.setSize(downloaded.length());
    }

    // Getters and Setters for File Class
    public boolean isFileComplete() {
        return (this.size == this.progress);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Entity getFromEntity() {
        return fromEntity;
    }

    public void setFromEntity(Entity fromEntity) {
        this.fromEntity = fromEntity;
    }

    public Entity getToEntity() {
        return toEntity;
    }

    public void setToEntity(Entity toEntity) {
        this.toEntity = toEntity;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
