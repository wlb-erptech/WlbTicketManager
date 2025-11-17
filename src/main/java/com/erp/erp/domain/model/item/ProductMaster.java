//package com.erp.erp.domain.model.item;
//
//import com.erp.erp.domain.model.shared.AbstractEntity;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import jakarta.persistence.TableGenerator;
//import lombok.AccessLevel;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Table(name = "PRODUCT_MASTER")
//@Getter
//@Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class ProductMaster extends AbstractEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ticketSeqGen")
//    @TableGenerator(
//        name           = "ticketSeqGen",
//        table          = "global_sequence",
//        pkColumnName   = "seq_name",
//        valueColumnName= "next_val",
//        pkColumnValue  = "ticket_seq",
//        initialValue   = 100000,
//        allocationSize = 1
//    )
//    @Column(name = "product_master_id")
//    private Long productMasterId;
//
//    @Column(name = "bands_2g", length = 255)
//    private String bands2g;
//
//    @Column(name = "bands_3g", length = 255)
//    private String bands3g;
//
//    @Column(name = "battery_type", length = 100)
//    private String batteryType;
//
//    @Column(name = "BRAND", length = 100)
//    private String brand;
//
//    @Column(name = "product_link", length = 500)
//    private String productLink;
//
//    @Column(name = "display_size", length = 50)
//    private String displaySize;
//
//    @Column(name = "product_description", columnDefinition = "TEXT")
//    private String productDescription;
//
//    @Column(name = "item_name", length = 100)
//    private String itemName;
//
//    @Column(name = "DIMENSIONS", length = 100)
//    private String dimensions;
//
//    @Column(name = "product_name", length = 100)
//    private String productName;
//
//    @Column(name = "WEIGHT", length = 50)
//    private String weight;
//
//    @Column(name = "PRICE", length = 200)
//    private String price;
//
//    @Column(name = "internal_memory", length = 100)
//    private String internalMemory;
//
//    @Column(name = "talk_time", length = 50)
//    private String talkTime;
//
//    @Column(name = "main_camera", length = 100)
//    private String mainCamera;
//
//    @Column(name = "main_camera_features", columnDefinition = "TEXT")
//    private String mainCameraFeatures;
//
//    @Column(name = "STATUS", length = 50)
//    private String status;
//
//    @Column(name = "display_resolution", length = 100)
//    private String displayResolution;
//
//    @Column(name = "COLORS", length = 100)
//    private String colors;
//
//    @Column(name = "release_date", length = 200)
//    private String releaseDate;
//}