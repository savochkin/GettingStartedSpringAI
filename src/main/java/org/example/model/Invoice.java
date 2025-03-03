package org.example.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;

@Value
@Builder
public class Invoice {
    String invoiceId;
    BigDecimal invoiceAmount;
    String invoiceCurrency;
}
