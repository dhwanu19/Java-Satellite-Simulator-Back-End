package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;
import unsw.response.models.EntityInfoResponse;
import unsw.utils.Angle;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public class BlackoutController {
    // List of Entites
    private List<Entity> entityList = new ArrayList<Entity>();
    private List<Slope> slopeList = new ArrayList<Slope>();

    // Create a device and add to EntityList
    public void createDevice(String deviceId, String type, Angle position, boolean isMoving) {
        Entity entity = null;
        switch (type) {
        case "LaptopDevice":
            entity = new LaptopDevice(deviceId, type, RADIUS_OF_JUPITER, position, isMoving);
            break;
        case "HandheldDevice":
            entity = new HandheldDevice(deviceId, type, RADIUS_OF_JUPITER, position, isMoving);
            break;
        case "DesktopDevice":
            entity = new DesktopDevice(deviceId, type, RADIUS_OF_JUPITER, position, isMoving);
            break;
        default:
            return;
        }

        entityList.add(entity);
    }

    // Creates a device that cannot move and add to EntityList
    public void createDevice(String deviceId, String type, Angle position) {
        createDevice(deviceId, type, position, false);
    }

    // Create a satellite and add to EntityList
    public void createSatellite(String satelliteId, String type, double height, Angle position) {
        Entity entity = null;

        switch (type) {
        case "StandardSatellite":
            entity = new StandardSatellite(satelliteId, type, height, position);
            break;
        case "TeleportingSatellite":
            entity = new TeleportingSatellite(satelliteId, type, height, position);
            break;
        case "RelaySatellite":
            entity = new RelaySatellite(satelliteId, type, height, position);
            break;
        default:
            return;
        }

        entityList.add(entity);
    }

    // Iterates through list of Entites and removes entity
    // of input id. Entity may be a Device or Satellite
    public void removeEntity(String id) {
        for (Entity entity : entityList) {
            if (entity.getId().equals(id)) {
                entityList.remove(entity);
                break;
            }
        }
    }

    // Remove a device from EntityList
    public void removeDevice(String deviceId) {
        removeEntity(deviceId);
    }

    // Remove a satellite from EntityList
    public void removeSatellite(String satelliteId) {
        removeEntity(satelliteId);
    }

    // List all DeviceIds
    public List<String> listDeviceIds() {
        List<String> entityIds = new ArrayList<String>();

        for (Entity entity : entityList) {
            if (entity instanceof Device) {
                entityIds.add(entity.getId());
            }
        }
        return entityIds;
    }

    // List all SatelliteIds
    public List<String> listSatelliteIds() {
        List<String> entityIds = new ArrayList<String>();

        for (Entity entity : entityList) {
            if (entity instanceof Satellite) {
                entityIds.add(entity.getId());
            }
        }
        return entityIds;
    }

    // Add a file to Device's FilesList
    public void addFileToDevice(String deviceId, String filename, String content) {
        for (Entity entity : entityList) {
            if (entity.getId().equals(deviceId)) {
                File file = new File(filename, content, entity, entity, content.length());
                entity.addToFilesList(file);
            }
        }
    }

    // Get the EntityInfoResponse of a specific Entity
    public EntityInfoResponse getInfo(String id) {
        for (Entity entity : entityList) {
            if (entity.getId().equals(id)) {
                return entity.getEntityInfoResponse();
            }
        }

        return null;
    }

    // Simulate the Movement of Entities and File Transfer system for 1 tick
    public void simulate() {
        // Move all Satellites
        for (Entity entity : entityList) {
            if (entity instanceof Satellite) {
                ((Satellite) entity).move();
            } else if (entity instanceof Device && entity.isMovingDevice()) {
                Slope slope = findSlope(entity);
                ((Device) entity).move(slope);

            }
        }

        // Delete/ modify partially downloaded files that are no longer in range of
        // transmission
        for (Entity entity : entityList) {
            if (!(entity instanceof RelaySatellite)) {
                List<String> communicablesList = communicableEntitiesInRange(entity.getId());
                entity.checkPartialFiles(communicablesList);
            }
        }

        // Update progress of remaining files
        for (Entity entity : entityList) {
            if (!(entity instanceof RelaySatellite)) {
                entity.updateFileProgress();
            }
        }
    }

    /**
     * Simulate for the specified number of minutes. You shouldn't need to modify
     * this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    // Find all communicableEntitiesInRange of input Entity
    public List<String> communicableEntitiesInRange(String id) {
        List<String> communicablesList = new ArrayList<String>();

        for (Entity entity : entityList) {
            if (entity.getId().equals(id))
                communicablesList = getCommunicablesList(entity, entity, communicablesList);
        }
        communicablesList.remove(id);
        return communicablesList;
    }

    // DFS algorithm for communicableEntitiesInRange method
    public List<String> getCommunicablesList(Entity origin, Entity currEntity, List<String> communicablesList) {
        for (Entity entity : entityList) {

            boolean isDuplicate = communicablesList.contains(entity.getId());
            boolean isDifferentEntity = !(entity.getId().equals(currEntity.getId()));
            boolean isSupported = origin.isSupportedEntity(origin, entity);
            boolean isInRangeAndVisible = currEntity.isInRangeAndVisible(entity);

            if (isDifferentEntity && isSupported && isInRangeAndVisible && !isDuplicate) {
                communicablesList.add(entity.getId());
                if (entity instanceof RelaySatellite) {
                    // Get entities in range of Relay satellite
                    communicablesList = getCommunicablesList(origin, entity, communicablesList);
                }
            }
        }
        return communicablesList;
    }

    // Find an Entity from EntityList given its id
    public Entity findEntity(String id) {
        for (Entity entity : entityList) {
            if (entity.getId().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    // Find which slope an input device lies on
    // Returns null if there is no such slope
    public Slope findSlope(Entity entity) {
        if (entity instanceof Device) {
            Angle newPosition = entity.getPosition().add(entity.getAngularVelocity());
            for (Slope slope : slopeList) {

                boolean isNewPositionOnSlope = (newPosition.toDegrees() >= slope.getStartAngle()
                        && newPosition.toDegrees() <= slope.getEndAngle());

                if (isNewPositionOnSlope) {
                    return slope;
                }
            }
        }
        return null;
    }

    // Send a file from one Entity to another
    public void sendFile(String fileName, String fromId, String toId) throws FileTransferException {
        Entity toEntity = findEntity(toId);
        Entity fromEntity = findEntity(fromId);
        File file = fromEntity.getFileFromFilesList(fileName);

        if (!fromEntity.doesEntityHaveFullFile(fileName)) {
            throw new VirtualFileNotFoundException(fileName);
        } else if (fromEntity.isSendingBandwidthFull()) {
            throw new VirtualFileNoBandwidthException(fromEntity.getId());
        } else if (toEntity.isReceivingBandwidthFull()) {
            throw new VirtualFileNoBandwidthException(toEntity.getId());
        } else if (toEntity.getFileFromFilesList(fileName) != null) {
            throw new VirtualFileAlreadyExistsException(fileName);
        } else if (toEntity.isMaxFilesReached()) {
            throw new VirtualFileNoStorageSpaceException("Max Files Reached");
        } else if (toEntity.willMaxStorageBeExceeded(file)) {
            throw new VirtualFileNoStorageSpaceException("Max Storage Reached");
        }

        File sendFile = new File(fileName, file.getContent(), fromEntity, toEntity, 0);
        fromEntity.addToSendingFiles(sendFile);
        toEntity.addToFilesList(sendFile);
    }

    // Create a slope and add to SlopeList
    public void createSlope(int startAngle, int endAngle, int gradient) {
        Slope newSlope = new Slope(startAngle, endAngle, gradient);
        slopeList.add(newSlope);
    }
}
