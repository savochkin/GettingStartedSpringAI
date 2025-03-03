package org.example;

import org.example.model.Invoice;
import org.example.model.PaymentMethod;
import org.example.model.Property;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BookingTools {
    private final Map<String, Property> properties = new HashMap<>();
    private final Map<String, List<Invoice>> propertyInvoices = new HashMap<>();

    public BookingTools() {
        initializeProperties();
        initializeInvoices();
    }

    private void initializeProperties() {
        properties.put("P1", Property.builder()
                .id("P1")
                .name("Grand Hotel Milano")
                .country("Italy")
                .city("Milan")
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build());

        properties.put("P2", Property.builder()
                .id("P2")
                .name("NYC Downtown Suites")
                .country("US")
                .city("New York")
                .paymentMethod(PaymentMethod.ADYEN)
                .build());

        properties.put("P3", Property.builder()
                .id("P3")
                .name("Canal House")
                .country("Netherlands")
                .city("Amsterdam")
                .paymentMethod(PaymentMethod.DIRECT_DEBIT)
                .build());

        properties.put("P4", Property.builder()
                .id("P4")
                .name("Copacabana Palace")
                .country("Brazil")
                .city("Rio de Janeiro")
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build());

        properties.put("P5", Property.builder()
                .id("P5")
                .name("Bogota Business Hotel")
                .country("Colombia")
                .city("Bogota")
                .paymentMethod(PaymentMethod.ADYEN)
                .build());

        // Add more properties...
    }

    private void initializeInvoices() {
        propertyInvoices.put("P1", Arrays.asList(
                Invoice.builder().invoiceId("INV-001").invoiceAmount(new BigDecimal("1500.00")).invoiceCurrency("EUR").build(),
                Invoice.builder().invoiceId("INV-002").invoiceAmount(new BigDecimal("2300.00")).invoiceCurrency("EUR").build()
        ));

        propertyInvoices.put("P3", Arrays.asList(
                Invoice.builder().invoiceId("INV-003").invoiceAmount(new BigDecimal("1750.00")).invoiceCurrency("EUR").build()
        ));

        propertyInvoices.put("P4", Arrays.asList(
                Invoice.builder().invoiceId("INV-004").invoiceAmount(new BigDecimal("3200.00")).invoiceCurrency("BRL").build(),
                Invoice.builder().invoiceId("INV-005").invoiceAmount(new BigDecimal("1800.00")).invoiceCurrency("BRL").build()
        ));
    }

    @Tool(description = "get payment method for a specific property")
    public PaymentMethod getPaymentMethodForProperty(String propertyId) {
        Property property = properties.get(propertyId);
        if (property == null) {
            return null;
        }
        return property.getPaymentMethod();
    }

    @Tool(description = "get country where the property is located")
    public String getPropertyLocation(String propertyId) {
        Property property = properties.get(propertyId);
        if (property == null) {
            return "Property not found with ID: " + propertyId;
        }
        return property.getCountry();
    }

    @Tool(description = "get outstanding invoices for a property")
    public List<Invoice> getOutstandingInvoices(String propertyId) {
        Property property = properties.get(propertyId);
        if (property == null) {
            return List.of();
        }

        return propertyInvoices.getOrDefault(propertyId, Collections.emptyList());
    }
}
