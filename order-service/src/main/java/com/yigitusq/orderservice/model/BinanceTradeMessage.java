package com.yigitusq.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // Jackson için ŞART!
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceTradeMessage {

    @JsonProperty("s") // Binance JSON'unda sembol "s" harfiyle geliyor
    private String symbol;

    @JsonProperty("p") // Fiyat "p" harfiyle geliyor
    private String price;

    @JsonProperty("q") // Miktar "q" ile geliyor
    private String quantity;

    @JsonProperty("E") // Olay zamanı "E" ile geliyor
    private Long eventTime;
}