package com.zonainmueble.reports.maps;

import lombok.Data;
import java.util.List;

@Data
public class Geometry {
    private List<List<List<Double>>> coordinates;
}
