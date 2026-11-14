package com.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoordenadaResultadoDTO(@JsonProperty("lat") String lat,
                                     @JsonProperty("lon") String lon) {

}
