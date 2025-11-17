package com.erp.erp.domain.model.item;

import com.erp.erp.application.dto.ProductDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMasterRepository extends JpaRepository<GsmProductMaster, Long> {

  Optional<GsmProductMaster> findByGsmProductMasterId(Long productMasterId);

  Page<ProductDto> findByBrandIgnoreCase(String brand, Pageable pageable);

  @Query(
      value = """
      SELECT t
        FROM GsmProductMaster t
       WHERE 
         (
           :brand IS NULL 
           OR :brand = '' 
           OR LOWER(t.brand) = LOWER(:brand)
         )
         AND 
         (
           :productName IS NULL 
           OR :productName = '' 
           OR LOWER(t.phoneName) LIKE LOWER(CONCAT('%', :productName, '%'))
         )
         AND 
         NOT (
           (:brand IS NULL OR :brand = '') 
           AND (:productName IS NULL OR :productName = '')
         )
    """,
      countQuery = """
      SELECT COUNT(t)
        FROM GsmProductMaster t
       WHERE 
         (
           :brand IS NULL 
           OR :brand = '' 
           OR LOWER(t.brand) = LOWER(:brand)
         )
         AND 
         (
           :productName IS NULL 
           OR :productName = '' 
           OR LOWER(t.phoneName) LIKE LOWER(CONCAT('%', :productName, '%'))
         )
         AND 
         NOT (
           (:brand IS NULL OR :brand = '') 
           AND (:productName IS NULL OR :productName = '')
         )
    """
  )
  Page<GsmProductMaster> findByBrandAndFuzzyName(
      @Param("brand")       String brand,
      @Param("productName") String productName,
      Pageable pageable
  );

}
