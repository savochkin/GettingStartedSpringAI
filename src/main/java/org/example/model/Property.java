package org.example.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Property {
    String id;
    String name;
    String country;
    String city;
    String address;
    PaymentMethod paymentMethod;
}
