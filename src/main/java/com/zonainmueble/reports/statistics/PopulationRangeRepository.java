package com.zonainmueble.reports.statistics;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PopulationRangeRepository extends JpaRepository<PopulationRange, Integer> {
  @Query(value = "SELECT * FROM ___zi_reporte_basico_grupos_edad(?1)", nativeQuery = true)
  List<PopulationRange> findPopulationRanges(String wkt);
}
