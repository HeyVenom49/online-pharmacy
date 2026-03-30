package com.pharmacy.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrescriptionApprovedEvent extends BaseEvent {
    private Long prescriptionId;
    private Long userId;
    private Long medicineId;
    private String medicineName;
}
