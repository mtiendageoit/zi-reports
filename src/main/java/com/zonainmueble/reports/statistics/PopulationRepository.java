package com.zonainmueble.reports.statistics;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PopulationRepository extends JpaRepository<Population, Integer> {
  @Query(value = "SELECT * FROM ___zi_reporte_basico_habitantes(?1)", nativeQuery = true)
  List<Population> findPopulationFrom(String wkt);

  @Query(value = "SELECT ___zi_reporte_basico_anios_estudio(?1)", nativeQuery = true)
  Double findYearsOfStudy(String wkt);
}
