package unsw.blackout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;
import unsw.utils.MathsHelper;

public abstract class Entity {
    private String id;
    private String type;
    private double height;
    private Angle position;
    private int range;
    private double linearV;
    private int direction;
    private boolean isMovingDevice;
    private ArrayList<File> filesList = new ArrayList<File>();
    private ArrayList<File> sendingFiles = new ArrayList<File>();
    private FileTransferRestrictions restrictions;

    public Entity(String id, String type, double height, Angle position) {
        this.id = id;
        this.type = type;
        this.height = height;
        this.position = position;
        this.filesList = new ArrayList<File>();
    }

    public void addToFilesList(File file) {
        this.filesList.add(file);
    }

    public void removeFromFilesList(File file) {
        this.filesList.remove(file);
    }

    public void addToSendingFiles(File file) {
        this.sendingFiles.add(file);
    }

    public void removeFromSendingFiles(File file) {
        this.sendingFiles.remove(file);
    }

    public Angle getAngularVelocity() {
        // AngularVelocity = linearVelocity / height
        return (Angle.fromDegrees(Math.toDegrees(this.getLinearV() / this.getHeight())));
    }

    // Returns the EntityInfoResponse for a specific entity
    public EntityInfoResponse getEntityInfoResponse() {
        Map<String, FileInfoResponse> map = new HashMap<String, FileInfoResponse>();

        for (File file : filesList) {
            map.put(file.getFilename(), file.getFileInfoResponse());
        }

        return new EntityInfoResponse(id, position, height, type, map);
    }

    // Chekcs if destination entity is supported (communicable) by this entity
    public boolean isSupportedEntity(Entity origin, Entity dest) {
        if (origin instanceof Device && dest instanceof Device) {
            return false;
        } else if (origin instanceof StandardSatellite && dest instanceof DesktopDevice) {
            return false;
        } else if (origin instanceof DesktopDevice && dest instanceof StandardSatellite) {
            return false;
        }
        return true;
    }

    // Checks if another entity is in range and visible to this entity
    public boolean isInRangeAndVisible(Entity dest) {
        boolean isInRange = (MathsHelper.getDistance(this.height, this.position, dest.getHeight(),
                dest.getPosition()) <= this.getRange());
        boolean isVisible = MathsHelper.isVisible(this.height, this.position, dest.getHeight(), dest.getPosition());

        return (isInRange && isVisible);
    }

    // Returns a file of input fileName from entitys filesList
    public File getFileFromFilesList(String fileName) {
        for (File file : this.getFilesList()) {
            if (file.getFilename().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    // Returns a file of input fileName from entitys sendingList
    public File getFileFromSendingList(String fileName) {
        for (File file : this.getSendingFiles()) {
            if (file.getFilename().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    // Checks if an entity has a full downloaded version of the input file
    public boolean doesEntityHaveFullFile(String fileName) {
        File file = this.getFileFromFilesList(fileName);
        return ((file != null) && (file.isFileComplete()));
    }

    public boolean isSendingBandwidthFull() {
        return (this.getSendingFiles().size() >= this.getRestrictions().getSendingBandwidth());
    }

    // Returns the number of files in entitys filesList that are not completed
    public int getNumUnfinishedFiles() {
        int num = 0;
        for (File file : this.getFilesList()) {
            if (!file.isFileComplete())
                num++;
        }
        return num;
    }

    // Checks if the receiving bandwidth of the entity is currently full
    public boolean isReceivingBandwidthFull() {
        return (this.getNumUnfinishedFiles() >= this.getRestrictions().getReceivingBandwidth());
    }

    // Checks if the entity has reached its max number of files
    public boolean isMaxFilesReached() {
        return (this.getFilesList().size() >= this.getRestrictions().getMaxFiles());
    }

    // Checks if the max storage of an entity will be exceeded by adding a new file
    public boolean willMaxStorageBeExceeded(File file) {
        int totalSize = file.getSize();

        for (File downloadedFile : this.getFilesList()) {
            totalSize += downloadedFile.getSize();
        }

        return (totalSize > this.getRestrictions().getMaxBytes());
    }

    // Scrolls through an Entitys partial files. If sender and receiver of file are
    // no longer in a transmittable range, then updatePartialFiles is called to
    // update the files accordingly
    public void checkPartialFiles(List<String> communicablesList) {
        ArrayList<File> sendingFiles = this.getSendingFiles();
        ArrayList<File> newSendingFiles = new ArrayList<File>();

        for (File file : sendingFiles) {
            Entity toEntity = file.getToEntity();
            String toId = toEntity.getId();

            if (!communicablesList.contains(toId)) {
                toEntity.updatePartialFiles(file);
            } else {
                newSendingFiles.add(file);
            }
        }
        this.setSendingFiles(newSendingFiles);
    }

    // Updates partially downloaded files senders and receivers that are no longer
    // transmittable range according to cases
    public void updatePartialFiles(File file) {

        Entity fromEntity = file.getFromEntity();

        if (fromEntity instanceof TeleportingSatellite) {
            if (((TeleportingSatellite) fromEntity).isJustTeleported()) {
                file.removeRemainingTs();
            } else {
                this.removeFromFilesList(file);
            }
        } else if (this instanceof TeleportingSatellite) {
            if (((TeleportingSatellite) this).isJustTeleported()) {
                if (fromEntity instanceof Device) {
                    fromEntity.getFileFromFilesList(file.getFilename()).removeAllTs();
                    this.removeFromFilesList(file);
                } else {
                    file.removeRemainingTs();
                }
            }
        } else {
            this.removeFromFilesList(file);
        }
    }

    // Updates the progress of files in entities filesList
    public void updateFileProgress() {
        ArrayList<File> filesList = this.getFilesList();

        for (File file : filesList) {
            if (!file.isFileComplete()) {
                Entity fromEntity = file.getFromEntity();
                int tickBandwidth = Math.min(fromEntity.getCurrSendingBandwidth(), this.getCurrReceivingBandwidth());
                file.setProgress(file.getProgress() + tickBandwidth);

                if (file.getProgress() >= file.getSize()) {
                    fromEntity.removeFromSendingFiles(file);
                    file.setProgress(file.getSize());
                }
            }
        }

    }

    // Calculates the current sending bandwidth of the entity
    public int getCurrSendingBandwidth() {
        if (!(this instanceof Device)) {
            if (this.getSendingFiles().size() == 0) {
                return this.getRestrictions().getSendingBandwidth();
            }
            return ((int) (Math
                    .floor((double) this.getRestrictions().getSendingBandwidth() / this.getSendingFiles().size())));
        }
        return Integer.MAX_VALUE;
    }

    // Calculates the current receiving bandwidth of the entity
    public int getCurrReceivingBandwidth() {
        if (!(this instanceof Device)) {
            if (this.getNumUnfinishedFiles() == 0) {
                return this.getRestrictions().getReceivingBandwidth();
            }
            return ((int) (Math
                    .floor((double) this.getRestrictions().getReceivingBandwidth() / this.getNumUnfinishedFiles())));
        }
        return Integer.MAX_VALUE;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Angle getPosition() {
        return position;
    }

    public void setPosition(Angle position) {
        this.position = position;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public ArrayList<File> getFilesList() {
        return filesList;
    }

    public void setFilesList(ArrayList<File> filesList) {
        this.filesList = filesList;
    }

    public ArrayList<File> getSendingFiles() {
        return sendingFiles;
    }

    public void setSendingFiles(ArrayList<File> sendingFiles) {
        this.sendingFiles = sendingFiles;
    }

    public FileTransferRestrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(FileTransferRestrictions restrictions) {
        this.restrictions = restrictions;
    }

    public double getLinearV() {
        return linearV;
    }

    public void setLinearV(double linearV) {
        this.linearV = linearV;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isMovingDevice() {
        return isMovingDevice;
    }

    public void setMovingDevice(boolean isMovingDevice) {
        this.isMovingDevice = isMovingDevice;
    }
}
