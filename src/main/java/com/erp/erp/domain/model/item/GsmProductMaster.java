package com.erp.erp.domain.model.item;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GSM_PRODUCT_MASTER")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GsmProductMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ticketSeqGen")
    @TableGenerator(
        name            = "ticketSeqGen",
        table           = "global_sequence",
        pkColumnName    = "seq_name",
        valueColumnName = "next_val",
        pkColumnValue   = "ticket_seq",
        initialValue    = 100,
        allocationSize  = 1
    )
    @Column(name = "gsm_product_master_id")
    private Long gsmProductMasterId;

    @Column(name = "brand", length = 30)
    private String brand;              // Brand (max 15 in file)

    @Column(name = "phone_name", length = 100)
    private String phoneName;          // Phone Name (max 35)

    @Column(name = "phone_link", length = 500)
    private String phoneLink;          // Phone Link (max 75)

    @Column(name = "battery_charging", length = 150)   // max 140
    private String batteryCharging;    // Battery Charging

    @Column(name = "battery_music_play", length = 50)  // max 42
    private String batteryMusicPlay;   // Battery Music play

    @Column(name = "battery_stand_by", length = 50)    // max 48
    private String batteryStandBy;     // Battery Stand-by

    @Column(name = "battery_talk_time", length = 60)   // max 53
    private String batteryTalkTime;    // Battery Talk time

    @Column(name = "battery_type", length = 150)       // max 108
    private String batteryType;        // Battery Type

    // Long, descriptive text -> TEXT
    @Lob
    @Column(name = "body_build", columnDefinition = "TEXT") // max 161
    private String bodyBuild;          // Body Build

    @Column(name = "body_dimensions", length = 100)    // max 84
    private String bodyDimensions;     // Body Dimensions

    @Column(name = "body_keyboard", length = 50)       // max 11
    private String bodyKeyboard;       // Body Keyboard

    // Long, descriptive text -> TEXT
    @Lob
    @Column(name = "body_sim", columnDefinition = "TEXT")   // max 135
    private String bodySim;            // Body SIM

    @Column(name = "body_weight", length = 60)         // max 56
    private String bodyWeight;         // Body Weight

    @Column(name = "comms_bluetooth", length = 100)    // max 87
    private String commsBluetooth;     // Comms Bluetooth

    @Column(name = "comms_infrared_port", length = 50) // max 3
    private String commsInfraredPort;  // Comms Infrared port

    @Column(name = "comms_nfc", length = 100)          // max 68
    private String commsNfc;           // Comms NFC

    @Column(name = "comms_positioning", length = 120)  // max 99
    private String commsPositioning;   // Comms Positioning

    @Column(name = "comms_radio", length = 80)         // max 65
    private String commsRadio;         // Comms Radio

    @Column(name = "comms_usb", length = 100)          // max 89
    private String commsUsb;           // Comms USB

    // Long, list-like -> TEXT
    @Lob
    @Column(name = "comms_wlan", columnDefinition = "TEXT") // max 129
    private String commsWlan;          // Comms WLAN

    @Column(name = "display_protection", length = 100) // max 83
    private String displayProtection;  // Display Protection

    // Long, detailed -> TEXT
    @Lob
    @Column(name = "display_resolution", columnDefinition = "TEXT") // max 125
    private String displayResolution;  // Display Resolution

    @Column(name = "display_size", length = 80)        // max 67
    private String displaySize;        // Display Size

    // Long, enum-like list -> TEXT
    @Lob
    @Column(name = "display_type", columnDefinition = "TEXT")       // max 129
    private String displayType;        // Display Type

    @Column(name = "eu_label_battery", length = 50)    // max 30
    private String euLabelBattery;     // EU LABEL Battery

    @Column(name = "eu_label_energy", length = 50)     // max 7
    private String euLabelEnergy;      // EU LABEL Energy

    @Column(name = "eu_label_free_fall", length = 50)  // max 19
    private String euLabelFreeFall;    // EU LABEL Free fall

    @Column(name = "eu_label_repairability", length = 50) // max 7
    private String euLabelRepairability; // EU LABEL Repairability

    @Column(name = "features_alarm", length = 50)      // max 3
    private String featuresAlarm;      // Features Alarm

    @Column(name = "features_browser", length = 50)    // max 48
    private String featuresBrowser;    // Features Browser

    @Column(name = "features_clock", length = 50)      // max 3
    private String featuresClock;      // Features Clock

    @Column(name = "features_games", length = 120)     // max 92
    private String featuresGames;      // Features Games

    @Column(name = "features_java")
    private String featuresJava;       // Features Java (max 27)

    @Column(name = "features_languages")
    private String featuresLanguages;  // Features Languages

    @Column(name = "features_messaging")
    private String featuresMessaging;  // Features Messaging (max 96)

    @Column(name = "features_sensors")
    private String featuresSensors;    // Features Sensors (max 144, fits in VARCHAR(255))

    @Column(name = "launch_announced")
    private String launchAnnounced;    // Launch Announced

    @Column(name = "launch_status")
    private String launchStatus;       // Launch Status

    @Column(name = "main_camera_dual")
    private String mainCameraDual;     // Main Camera Dual (max 154, fits in 255)

    // VERY LONG -> TEXT (max 321)
    @Lob
    @Column(name = "main_camera_dual_or_triple", columnDefinition = "TEXT")
    private String mainCameraDualOrTriple; // Main Camera Dual or Triple

    @Column(name = "main_camera_features")
    private String mainCameraFeatures; // Main Camera Features (max 122)

    // VERY LONG -> TEXT (max 306)
    @Lob
    @Column(name = "main_camera_five", columnDefinition = "TEXT")
    private String mainCameraFive;     // Main Camera Five

    @Column(name = "main_camera_penta")
    private String mainCameraPenta;    // Main Camera Penta (max 110)

    // VERY LONG -> TEXT (max 364)
    @Lob
    @Column(name = "main_camera_quad", columnDefinition = "TEXT")
    private String mainCameraQuad;     // Main Camera Quad

    @Column(name = "main_camera_single")
    private String mainCameraSingle;   // Main Camera Single (max 118)

    // VERY LONG -> TEXT (max 365)
    @Lob
    @Column(name = "main_camera_triple", columnDefinition = "TEXT")
    private String mainCameraTriple;   // Main Camera Triple

    @Column(name = "main_camera_video")
    private String mainCameraVideo;    // Main Camera Video (max 190)

    @Column(name = "memory_call_records")
    private String memoryCallRecords;  // Memory Call records (max 78)

    @Column(name = "memory_card_slot")
    private String memoryCardSlot;     // Memory Card slot (max 95)

    @Column(name = "memory_internal")
    private String memoryInternal;     // Memory Internal (max 129)

    @Column(name = "memory_phonebook")
    private String memoryPhonebook;    // Memory Phonebook

    @Column(name = "misc_colors")
    private String miscColors;         // Misc Colors (max 229)

    @Column(name = "misc_models")
    private String miscModels;         // Misc Models (max 239)

    @Column(name = "misc_price")
    private String miscPrice;          // Misc Price (max 76)

    @Column(name = "misc_sar")
    private String miscSar;            // Misc SAR

    @Column(name = "misc_sar_eu")
    private String miscSarEu;          // Misc SAR EU

    @Column(name = "network_2g_bands")
    private String network2gBands;     // Network 2G bands (max 97)

    @Column(name = "network_3g_bands")
    private String network3gBands;     // Network 3G bands (max 83)

    @Column(name = "network_4g_bands")
    private String network4gBands;     // Network 4G bands (max 145)

    @Column(name = "network_5g_bands")
    private String network5gBands;     // Network 5G bands (max 147)

    @Column(name = "network_edge")
    private String networkEdge;        // Network EDGE (max 28)

    @Column(name = "network_gprs")
    private String networkGprs;        // Network GPRS (max 18)

    @Column(name = "network_speed")
    private String networkSpeed;       // Network Speed (max 143)

    @Column(name = "network_technology")
    private String networkTechnology;  // Network Technology (max 33)

    @Column(name = "our_tests_audio_quality")
    private String ourTestsAudioQuality; // Our Tests Audio quality

    @Column(name = "our_tests_battery")
    private String ourTestsBattery;    // Our Tests Battery (max 23)

    @Column(name = "our_tests_battery_old")
    private String ourTestsBatteryOld; // Our Tests Battery (old) (max 21)

    @Column(name = "our_tests_camera")
    private String ourTestsCamera;     // Our Tests Camera (max 13)

    @Column(name = "our_tests_display")
    private String ourTestsDisplay;    // Our Tests Display (max 58)

    @Column(name = "our_tests_loudspeaker")
    private String ourTestsLoudspeaker; // Our Tests Loudspeaker (max 35)

    @Column(name = "our_tests_performance")
    private String ourTestsPerformance; // Our Tests Performance (max 118)

    @Column(name = "platform_cpu")
    private String platformCpu;        // Platform CPU (max 196)

    @Column(name = "platform_chipset")
    private String platformChipset;    // Platform Chipset (max 121)

    @Column(name = "platform_gpu")
    private String platformGpu;        // Platform GPU (max 65)

    @Column(name = "platform_os")
    private String platformOs;         // Platform OS (max 143)

    @Column(name = "selfie_camera_dual")
    private String selfieCameraDual;   // Selfie camera Dual (max 143)

    @Column(name = "selfie_camera_features")
    private String selfieCameraFeatures; // Selfie camera Features (max 93)

    @Column(name = "selfie_camera_no")
    private String selfieCameraNo;     // Selfie camera No (max 6)

    @Column(name = "selfie_camera_single")
    private String selfieCameraSingle; // Selfie camera Single (max 115)

    @Column(name = "selfie_camera_triple")
    private String selfieCameraTriple; // Selfie camera Triple (max 135)

    @Column(name = "selfie_camera_video")
    private String selfieCameraVideo;  // Selfie camera Video (max 75)

    @Column(name = "sound_3_5mm_jack", length = 50)   // max 30
    private String sound35mmJack;      // Sound 3.5mm jack

    @Column(name = "sound_alert_types")
    private String soundAlertTypes;    // Sound Alert types (max 85)

    @Column(name = "sound_loudspeaker")
    private String soundLoudspeaker;   // Sound Loudspeaker (max 64)
}
