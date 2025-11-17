package com.erp.erp.application.imports;

import com.erp.erp.domain.model.item.GsmProductMaster;
import com.erp.erp.domain.model.item.GsmProductMasterRepository;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GsmProductImportService {

    @Value("classpath:data/pivoted_batch26102025.csv")  // adjust if needed
    private Resource csvResource;

    private final GsmProductMasterRepository repository;

    @Transactional
    public void importCsv() throws IOException, CsvValidationException {
        log.info("Starting GSM_PRODUCT_MASTER import from CSV...");

        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8));
                CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader)
        ) {
            Map<String, String> row;
            List<GsmProductMaster> batch = new ArrayList<>();
            int count = 0;

            while ((row = csvReader.readMap()) != null) {
                GsmProductMaster entity = mapRowToEntity(row);
                batch.add(entity);
                count++;

                if (batch.size() >= 1000) {
                    repository.saveAll(batch);
                    log.info("Inserted {} rows so far...", count);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }

            log.info("Finished GSM_PRODUCT_MASTER import. Total rows inserted: {}", count);
        }
    }

    private GsmProductMaster mapRowToEntity(Map<String, String> row) {
        // Column 0 ("") is the unnamed index column -> ignore
        return GsmProductMaster.builder()
                .brand(row.get("Brand"))
                .phoneName(row.get("Phone Name"))
                .phoneLink(row.get("Phone Link"))

                .batteryCharging(row.get("Battery Charging"))
                .batteryMusicPlay(row.get("Battery Music play"))
                .batteryStandBy(row.get("Battery Stand-by"))
                .batteryTalkTime(row.get("Battery Talk time"))
                .batteryType(row.get("Battery Type"))

                .bodyBuild(row.get("Body Build"))
                .bodyDimensions(row.get("Body Dimensions"))
                .bodyKeyboard(row.get("Body Keyboard"))
                .bodySim(row.get("Body SIM"))
                .bodyWeight(row.get("Body Weight"))

                .commsBluetooth(row.get("Comms Bluetooth"))
                .commsInfraredPort(row.get("Comms Infrared port"))
                .commsNfc(row.get("Comms NFC"))
                .commsPositioning(row.get("Comms Positioning"))
                .commsRadio(row.get("Comms Radio"))
                .commsUsb(row.get("Comms USB"))
                .commsWlan(row.get("Comms WLAN"))

                .displayProtection(row.get("Display Protection"))
                .displayResolution(row.get("Display Resolution"))
                .displaySize(row.get("Display Size"))
                .displayType(row.get("Display Type"))

                .euLabelBattery(row.get("EU LABEL Battery"))
                .euLabelEnergy(row.get("EU LABEL Energy"))
                .euLabelFreeFall(row.get("EU LABEL Free fall"))
                .euLabelRepairability(row.get("EU LABEL Repairability"))

                .featuresAlarm(row.get("Features Alarm"))
                .featuresBrowser(row.get("Features Browser"))
                .featuresClock(row.get("Features Clock"))
                .featuresGames(row.get("Features Games"))
                .featuresJava(row.get("Features Java"))
                .featuresLanguages(row.get("Features Languages"))
                .featuresMessaging(row.get("Features Messaging"))
                .featuresSensors(row.get("Features Sensors"))

                .launchAnnounced(row.get("Launch Announced"))
                .launchStatus(row.get("Launch Status"))

                .mainCameraDual(row.get("Main Camera Dual"))
                .mainCameraDualOrTriple(row.get("Main Camera Dual or Triple"))
                .mainCameraFeatures(row.get("Main Camera Features"))
                .mainCameraFive(row.get("Main Camera Five"))
                .mainCameraPenta(row.get("Main Camera Penta"))
                .mainCameraQuad(row.get("Main Camera Quad"))
                .mainCameraSingle(row.get("Main Camera Single"))
                .mainCameraTriple(row.get("Main Camera Triple"))
                .mainCameraVideo(row.get("Main Camera Video"))

                .memoryCallRecords(row.get("Memory Call records"))
                .memoryCardSlot(row.get("Memory Card slot"))
                .memoryInternal(row.get("Memory Internal"))
                .memoryPhonebook(row.get("Memory Phonebook"))

                .miscColors(row.get("Misc Colors"))
                .miscModels(row.get("Misc Models"))
                .miscPrice(row.get("Misc Price"))
                .miscSar(row.get("Misc SAR"))
                .miscSarEu(row.get("Misc SAR EU"))

                .network2gBands(row.get("Network 2G bands"))
                .network3gBands(row.get("Network 3G bands"))
                .network4gBands(row.get("Network 4G bands"))
                .network5gBands(row.get("Network 5G bands"))
                .networkEdge(row.get("Network EDGE"))
                .networkGprs(row.get("Network GPRS"))
                .networkSpeed(row.get("Network Speed"))
                .networkTechnology(row.get("Network Technology"))

                .ourTestsAudioQuality(row.get("Our Tests Audio quality"))
                .ourTestsBattery(row.get("Our Tests Battery"))
                .ourTestsBatteryOld(row.get("Our Tests Battery (old)"))
                .ourTestsCamera(row.get("Our Tests Camera"))
                .ourTestsDisplay(row.get("Our Tests Display"))
                .ourTestsLoudspeaker(row.get("Our Tests Loudspeaker"))
                .ourTestsPerformance(row.get("Our Tests Performance"))

                .platformCpu(row.get("Platform CPU"))
                .platformChipset(row.get("Platform Chipset"))
                .platformGpu(row.get("Platform GPU"))
                .platformOs(row.get("Platform OS"))

                .selfieCameraDual(row.get("Selfie camera Dual"))
                .selfieCameraFeatures(row.get("Selfie camera Features"))
                .selfieCameraNo(row.get("Selfie camera No"))
                .selfieCameraSingle(row.get("Selfie camera Single"))
                .selfieCameraTriple(row.get("Selfie camera Triple"))
                .selfieCameraVideo(row.get("Selfie camera Video"))

                .sound35mmJack(row.get("Sound 3.5mm jack"))
                .soundAlertTypes(row.get("Sound Alert types"))
                .soundLoudspeaker(row.get("Sound Loudspeaker"))
                .build();
    }
}
