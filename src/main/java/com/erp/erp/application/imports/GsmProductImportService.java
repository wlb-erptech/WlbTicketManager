package com.erp.erp.application.imports;

import com.erp.erp.domain.model.item.GsmProductMaster;
import com.erp.erp.domain.model.item.ProductMasterRepository;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service to import / upsert GSM product master data from an uploaded CSV/Excel-export.
 *
 * Behaviour:
 *  - Uses (brand + phoneName) as natural key.
 *  - If a row with same key exists => UPDATE its fields.
 *  - If not => INSERT a new GsmProductMaster using Lombok builder.
 *  - Duplicate rows (same brand+phoneName) inside the same file are effectively collapsed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GsmProductImportService {

    private final ProductMasterRepository repository;

    /**
     * Upsert import from an uploaded CSV file.
     * Expects headers: "Brand", "Phone Name", "Phone Link", etc. (same as your Excel export).
     */
    @Transactional
    public void upsertFromCsv(MultipartFile file) throws IOException, CsvValidationException {
        log.info("Starting GSM_PRODUCT_MASTER upsert import from file: {}", file.getOriginalFilename());

        // 1) Load all existing records into a Map keyed by (brand+phoneName)
        List<GsmProductMaster> existing = repository.findAll();
        Map<String, GsmProductMaster> existingByKey = new HashMap<>();

        for (GsmProductMaster e : existing) {
            String key = buildKey(e.getBrand(), e.getPhoneName());
            if (!key.isEmpty()) {
                existingByKey.put(key, e);
            }
        }

        int processed = 0;
        int inserted = 0;
        int updated = 0;

        try (
            Reader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );
            CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader)
        ) {
            Map<String, String> row;
            List<GsmProductMaster> batchToSave = new ArrayList<>();

            while ((row = csvReader.readMap()) != null) {
                processed++;

                String brand    = trimOrNull(row.get("Brand"));
                String phoneName = trimOrNull(row.get("Phone Name"));

                // Skip rows without a key
                if (brand == null || phoneName == null) {
                    log.debug("Skipping row {}: missing brand or phone name", processed);
                    continue;
                }

                String key = buildKey(brand, phoneName);
                GsmProductMaster entity = existingByKey.get(key);

                if (entity == null) {
                    // NEW entity -> create using Builder
                    entity = createEntityFromRow(row);
                    existingByKey.put(key, entity);  // in case same key appears again in same file
                    inserted++;
                } else {
                    // EXISTING entity -> update its fields
                    updateEntityFromRow(entity, row);
                    updated++;
                }

                batchToSave.add(entity);

                if (batchToSave.size() >= 1000) {
                    repository.saveAll(batchToSave);
                    batchToSave.clear();
                    log.info("Upserted {} rows so far (inserted={}, updated={})",
                        processed, inserted, updated);
                }
            }

            if (!batchToSave.isEmpty()) {
                repository.saveAll(batchToSave);
            }
        }

        log.info(
            "Finished GSM_PRODUCT_MASTER upsert import. Processed={}, inserted={}, updated={}",
            processed, inserted, updated
        );
    }

    /**
     * Build a natural key (case-insensitive, trimmed) from brand + phoneName.
     */
    private String buildKey(String brand, String phoneName) {
        String b = brand == null ? "" : brand.trim().toLowerCase();
        String p = phoneName == null ? "" : phoneName.trim().toLowerCase();
        return b + "||" + p;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Create a NEW GsmProductMaster using Lombok Builder.
     * This uses the same mapping logic as updateEntityFromRow, but in builder style.
     */
    private GsmProductMaster createEntityFromRow(Map<String, String> row) {
        return GsmProductMaster.builder()
            .brand(trimOrNull(row.get("Brand")))
            .phoneName(trimOrNull(row.get("Phone Name")))
            .phoneLink(trimOrNull(row.get("Phone Link")))

            .batteryCharging(trimOrNull(row.get("Battery Charging")))
            .batteryMusicPlay(trimOrNull(row.get("Battery Music play")))
            .batteryStandBy(trimOrNull(row.get("Battery Stand-by")))
            .batteryTalkTime(trimOrNull(row.get("Battery Talk time")))
            .batteryType(trimOrNull(row.get("Battery Type")))

            .bodyBuild(trimOrNull(row.get("Body Build")))
            .bodyDimensions(trimOrNull(row.get("Body Dimensions")))
            .bodyKeyboard(trimOrNull(row.get("Body Keyboard")))
            .bodySim(trimOrNull(row.get("Body SIM")))
            .bodyWeight(trimOrNull(row.get("Body Weight")))

            .commsBluetooth(trimOrNull(row.get("Comms Bluetooth")))
            .commsInfraredPort(trimOrNull(row.get("Comms Infrared port")))
            .commsNfc(trimOrNull(row.get("Comms NFC")))
            .commsPositioning(trimOrNull(row.get("Comms Positioning")))
            .commsRadio(trimOrNull(row.get("Comms Radio")))
            .commsUsb(trimOrNull(row.get("Comms USB")))
            .commsWlan(trimOrNull(row.get("Comms WLAN")))

            .displayProtection(trimOrNull(row.get("Display Protection")))
            .displayResolution(trimOrNull(row.get("Display Resolution")))
            .displaySize(trimOrNull(row.get("Display Size")))
            .displayType(trimOrNull(row.get("Display Type")))

            .euLabelBattery(trimOrNull(row.get("EU LABEL Battery")))
            .euLabelEnergy(trimOrNull(row.get("EU LABEL Energy")))
            .euLabelFreeFall(trimOrNull(row.get("EU LABEL Free fall")))
            .euLabelRepairability(trimOrNull(row.get("EU LABEL Repairability")))

            .featuresAlarm(trimOrNull(row.get("Features Alarm")))
            .featuresBrowser(trimOrNull(row.get("Features Browser")))
            .featuresClock(trimOrNull(row.get("Features Clock")))
            .featuresGames(trimOrNull(row.get("Features Games")))
            .featuresJava(trimOrNull(row.get("Features Java")))
            .featuresLanguages(trimOrNull(row.get("Features Languages")))
            .featuresMessaging(trimOrNull(row.get("Features Messaging")))
            .featuresSensors(trimOrNull(row.get("Features Sensors")))

            .launchAnnounced(trimOrNull(row.get("Launch Announced")))
            .launchStatus(trimOrNull(row.get("Launch Status")))

            .mainCameraDual(trimOrNull(row.get("Main Camera Dual")))
            .mainCameraDualOrTriple(trimOrNull(row.get("Main Camera Dual or Triple")))
            .mainCameraFeatures(trimOrNull(row.get("Main Camera Features")))
            .mainCameraFive(trimOrNull(row.get("Main Camera Five")))
            .mainCameraPenta(trimOrNull(row.get("Main Camera Penta")))
            .mainCameraQuad(trimOrNull(row.get("Main Camera Quad")))
            .mainCameraSingle(trimOrNull(row.get("Main Camera Single")))
            .mainCameraTriple(trimOrNull(row.get("Main Camera Triple")))
            .mainCameraVideo(trimOrNull(row.get("Main Camera Video")))

            .memoryCallRecords(trimOrNull(row.get("Memory Call records")))
            .memoryCardSlot(trimOrNull(row.get("Memory Card slot")))
            .memoryInternal(trimOrNull(row.get("Memory Internal")))
            .memoryPhonebook(trimOrNull(row.get("Memory Phonebook")))

            .miscColors(trimOrNull(row.get("Misc Colors")))
            .miscModels(trimOrNull(row.get("Misc Models")))
            .miscPrice(trimOrNull(row.get("Misc Price")))
            .miscSar(trimOrNull(row.get("Misc SAR")))
            .miscSarEu(trimOrNull(row.get("Misc SAR EU")))

            .network2gBands(trimOrNull(row.get("Network 2G bands")))
            .network3gBands(trimOrNull(row.get("Network 3G bands")))
            .network4gBands(trimOrNull(row.get("Network 4G bands")))
            .network5gBands(trimOrNull(row.get("Network 5G bands")))
            .networkEdge(trimOrNull(row.get("Network EDGE")))
            .networkGprs(trimOrNull(row.get("Network GPRS")))
            .networkSpeed(trimOrNull(row.get("Network Speed")))
            .networkTechnology(trimOrNull(row.get("Network Technology")))

            .ourTestsAudioQuality(trimOrNull(row.get("Our Tests Audio quality")))
            .ourTestsBattery(trimOrNull(row.get("Our Tests Battery")))
            .ourTestsBatteryOld(trimOrNull(row.get("Our Tests Battery (old)")))
            .ourTestsCamera(trimOrNull(row.get("Our Tests Camera")))
            .ourTestsDisplay(trimOrNull(row.get("Our Tests Display")))
            .ourTestsLoudspeaker(trimOrNull(row.get("Our Tests Loudspeaker")))
            .ourTestsPerformance(trimOrNull(row.get("Our Tests Performance")))

            .platformCpu(trimOrNull(row.get("Platform CPU")))
            .platformChipset(trimOrNull(row.get("Platform Chipset")))
            .platformGpu(trimOrNull(row.get("Platform GPU")))
            .platformOs(trimOrNull(row.get("Platform OS")))

            .selfieCameraDual(trimOrNull(row.get("Selfie camera Dual")))
            .selfieCameraFeatures(trimOrNull(row.get("Selfie camera Features")))
            .selfieCameraNo(trimOrNull(row.get("Selfie camera No")))
            .selfieCameraSingle(trimOrNull(row.get("Selfie camera Single")))
            .selfieCameraTriple(trimOrNull(row.get("Selfie camera Triple")))
            .selfieCameraVideo(trimOrNull(row.get("Selfie camera Video")))

            .sound35mmJack(trimOrNull(row.get("Sound 3.5mm jack")))
            .soundAlertTypes(trimOrNull(row.get("Sound Alert types")))
            .soundLoudspeaker(trimOrNull(row.get("Sound Loudspeaker")))
            .build();
    }

    /**
     * Update an existing entity from CSV row.
     * Uses same mapping as createEntityFromRow, but via setters.
     */
    private void updateEntityFromRow(GsmProductMaster e, Map<String, String> row) {
        e.setBrand(trimOrNull(row.get("Brand")));
        e.setPhoneName(trimOrNull(row.get("Phone Name")));
        e.setPhoneLink(trimOrNull(row.get("Phone Link")));

        e.setBatteryCharging(trimOrNull(row.get("Battery Charging")));
        e.setBatteryMusicPlay(trimOrNull(row.get("Battery Music play")));
        e.setBatteryStandBy(trimOrNull(row.get("Battery Stand-by")));
        e.setBatteryTalkTime(trimOrNull(row.get("Battery Talk time")));
        e.setBatteryType(trimOrNull(row.get("Battery Type")));

        e.setBodyBuild(trimOrNull(row.get("Body Build")));
        e.setBodyDimensions(trimOrNull(row.get("Body Dimensions")));
        e.setBodyKeyboard(trimOrNull(row.get("Body Keyboard")));
        e.setBodySim(trimOrNull(row.get("Body SIM")));
        e.setBodyWeight(trimOrNull(row.get("Body Weight")));

        e.setCommsBluetooth(trimOrNull(row.get("Comms Bluetooth")));
        e.setCommsInfraredPort(trimOrNull(row.get("Comms Infrared port")));
        e.setCommsNfc(trimOrNull(row.get("Comms NFC")));
        e.setCommsPositioning(trimOrNull(row.get("Comms Positioning")));
        e.setCommsRadio(trimOrNull(row.get("Comms Radio")));
        e.setCommsUsb(trimOrNull(row.get("Comms USB")));
        e.setCommsWlan(trimOrNull(row.get("Comms WLAN")));

        e.setDisplayProtection(trimOrNull(row.get("Display Protection")));
        e.setDisplayResolution(trimOrNull(row.get("Display Resolution")));
        e.setDisplaySize(trimOrNull(row.get("Display Size")));
        e.setDisplayType(trimOrNull(row.get("Display Type")));

        e.setEuLabelBattery(trimOrNull(row.get("EU LABEL Battery")));
        e.setEuLabelEnergy(trimOrNull(row.get("EU LABEL Energy")));
        e.setEuLabelFreeFall(trimOrNull(row.get("EU LABEL Free fall")));
        e.setEuLabelRepairability(trimOrNull(row.get("EU LABEL Repairability")));

        e.setFeaturesAlarm(trimOrNull(row.get("Features Alarm")));
        e.setFeaturesBrowser(trimOrNull(row.get("Features Browser")));
        e.setFeaturesClock(trimOrNull(row.get("Features Clock")));
        e.setFeaturesGames(trimOrNull(row.get("Features Games")));
        e.setFeaturesJava(trimOrNull(row.get("Features Java")));
        e.setFeaturesLanguages(trimOrNull(row.get("Features Languages")));
        e.setFeaturesMessaging(trimOrNull(row.get("Features Messaging")));
        e.setFeaturesSensors(trimOrNull(row.get("Features Sensors")));

        e.setLaunchAnnounced(trimOrNull(row.get("Launch Announced")));
        e.setLaunchStatus(trimOrNull(row.get("Launch Status")));

        e.setMainCameraDual(trimOrNull(row.get("Main Camera Dual")));
        e.setMainCameraDualOrTriple(trimOrNull(row.get("Main Camera Dual or Triple")));
        e.setMainCameraFeatures(trimOrNull(row.get("Main Camera Features")));
        e.setMainCameraFive(trimOrNull(row.get("Main Camera Five")));
        e.setMainCameraPenta(trimOrNull(row.get("Main Camera Penta")));
        e.setMainCameraQuad(trimOrNull(row.get("Main Camera Quad")));
        e.setMainCameraSingle(trimOrNull(row.get("Main Camera Single")));
        e.setMainCameraTriple(trimOrNull(row.get("Main Camera Triple")));
        e.setMainCameraVideo(trimOrNull(row.get("Main Camera Video")));

        e.setMemoryCallRecords(trimOrNull(row.get("Memory Call records")));
        e.setMemoryCardSlot(trimOrNull(row.get("Memory Card slot")));
        e.setMemoryInternal(trimOrNull(row.get("Memory Internal")));
        e.setMemoryPhonebook(trimOrNull(row.get("Memory Phonebook")));

        e.setMiscColors(trimOrNull(row.get("Misc Colors")));
        e.setMiscModels(trimOrNull(row.get("Misc Models")));
        e.setMiscPrice(trimOrNull(row.get("Misc Price")));
        e.setMiscSar(trimOrNull(row.get("Misc SAR")));
        e.setMiscSarEu(trimOrNull(row.get("Misc SAR EU")));

        e.setNetwork2gBands(trimOrNull(row.get("Network 2G bands")));
        e.setNetwork3gBands(trimOrNull(row.get("Network 3G bands")));
        e.setNetwork4gBands(trimOrNull(row.get("Network 4G bands")));
        e.setNetwork5gBands(trimOrNull(row.get("Network 5G bands")));
        e.setNetworkEdge(trimOrNull(row.get("Network EDGE")));
        e.setNetworkGprs(trimOrNull(row.get("Network GPRS")));
        e.setNetworkSpeed(trimOrNull(row.get("Network Speed")));
        e.setNetworkTechnology(trimOrNull(row.get("Network Technology")));

        e.setOurTestsAudioQuality(trimOrNull(row.get("Our Tests Audio quality")));
        e.setOurTestsBattery(trimOrNull(row.get("Our Tests Battery")));
        e.setOurTestsBatteryOld(trimOrNull(row.get("Our Tests Battery (old)")));
        e.setOurTestsCamera(trimOrNull(row.get("Our Tests Camera")));
        e.setOurTestsDisplay(trimOrNull(row.get("Our Tests Display")));
        e.setOurTestsLoudspeaker(trimOrNull(row.get("Our Tests Loudspeaker")));
        e.setOurTestsPerformance(trimOrNull(row.get("Our Tests Performance")));

        e.setPlatformCpu(trimOrNull(row.get("Platform CPU")));
        e.setPlatformChipset(trimOrNull(row.get("Platform Chipset")));
        e.setPlatformGpu(trimOrNull(row.get("Platform GPU")));
        e.setPlatformOs(trimOrNull(row.get("Platform OS")));

        e.setSelfieCameraDual(trimOrNull(row.get("Selfie camera Dual")));
        e.setSelfieCameraFeatures(trimOrNull(row.get("Selfie camera Features")));
        e.setSelfieCameraNo(trimOrNull(row.get("Selfie camera No")));
        e.setSelfieCameraSingle(trimOrNull(row.get("Selfie camera Single")));
        e.setSelfieCameraTriple(trimOrNull(row.get("Selfie camera Triple")));
        e.setSelfieCameraVideo(trimOrNull(row.get("Selfie camera Video")));

        e.setSound35mmJack(trimOrNull(row.get("Sound 3.5mm jack")));
        e.setSoundAlertTypes(trimOrNull(row.get("Sound Alert types")));
        e.setSoundLoudspeaker(trimOrNull(row.get("Sound Loudspeaker")));
    }
}
