package com.erp.erp.domain.model.item;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GsmProductMasterRepository extends JpaRepository<GsmProductMaster, Long> {

  Optional<GsmProductMaster> findByBrandIgnoreCaseAndPhoneNameIgnoreCase(
      String brand,
      String phoneName
  );

}
