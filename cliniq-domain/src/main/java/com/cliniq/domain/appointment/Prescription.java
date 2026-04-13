package com.cliniq.domain.appointment;

import com.cliniq.shared.validation.Preconditions;

public class Prescription {

    private final PrescriptionId id;
    private final MedicationReference medication;
    private final String dosage;
    private final String instructions;

    Prescription(PrescriptionId id, MedicationReference medication, String dosage, String instructions) {
        this.id = id;
        this.medication = medication;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    public static Prescription create(MedicationReference medication, String dosage, String instructions) {
        Preconditions.requireNonNull(medication, "medication");
        Preconditions.requireNotBlank(dosage, "dosage");
        Preconditions.requireNotBlank(instructions, "instructions");
        return new Prescription(PrescriptionId.generate(), medication, dosage, instructions);
    }

    public PrescriptionId getId() {
        return id;
    }

    public MedicationReference getMedication() {
        return medication;
    }

    public String getDosage() {
        return dosage;
    }

    public String getInstructions() {
        return instructions;
    }
}